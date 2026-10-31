package com.example.data

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

object PdfOutlineExtractor {

    private const val TAG = "PdfOutlineExtractor"

    data class RawOutlineItem(
        val title: String,
        val targetPageObjNum: Int?,
        val directPageNum: Int?,
        val parentObjNum: Int?,
        val objNum: Int,
        val level: Int = 0
    )

    /**
     * Extracts outline/bookmarks from a PDF file.
     * Returns an empty list if no outlines are present.
     */
    fun extractOutline(
        file: File,
        folioId: String,
        totalPages: Int
    ): List<ChapterEntity> {
        if (!file.exists() || file.length() < 128) return emptyList()

        try {
            val content = readFileAsStringSafely(file)
            val pageObjList = extractPageObjectNumbers(content)
            val pageObjMap = pageObjList.mapIndexed { idx, objNum -> objNum to idx }.toMap()

            val rawItems = extractRawOutlineItems(content)
            if (rawItems.isEmpty()) {
                // Try to scan for embedded text Table of Contents in the first pages of PDF stream
                val textToc = extractTextToc(content, totalPages, folioId)
                if (textToc.isNotEmpty()) {
                    return textToc
                }
                return emptyList()
            }

            // Map targetPageObjNum or directPageNum to 0-based page index
            val resolvedItems = mutableListOf<Pair<RawOutlineItem, Int>>()
            for (item in rawItems) {
                val pageIdx = when {
                    item.directPageNum != null -> item.directPageNum.coerceIn(0, totalPages - 1)
                    item.targetPageObjNum != null && pageObjMap.containsKey(item.targetPageObjNum) -> {
                        pageObjMap[item.targetPageObjNum] ?: 0
                    }
                    else -> null
                }
                if (pageIdx != null) {
                    resolvedItems.add(item to pageIdx)
                }
            }

            if (resolvedItems.isEmpty()) return emptyList()

            // Sort by start page index, preserving outline structure
            resolvedItems.sortBy { it.second }

            val result = mutableListOf<ChapterEntity>()
            for (i in resolvedItems.indices) {
                val (raw, startPage) = resolvedItems[i]
                val endPage = if (i + 1 < resolvedItems.size) {
                    (resolvedItems[i + 1].second - 1).coerceAtLeast(startPage)
                } else {
                    (totalPages - 1).coerceAtLeast(startPage)
                }

                val cleanTitle = raw.title.trim().ifBlank { "Section ${i + 1}" }
                val (title, subtitle) = if (cleanTitle.contains(" - ") || cleanTitle.contains(" — ") || cleanTitle.contains(": ")) {
                    val parts = cleanTitle.split(Regex(" - | — |: "), limit = 2)
                    parts[0].trim() to parts.getOrElse(1) { "" }.trim()
                } else {
                    cleanTitle to "Page ${startPage + 1}–${endPage + 1}"
                }

                result.add(
                    ChapterEntity(
                        folioId = folioId,
                        chapterNumber = i + 1,
                        title = title,
                        subtitle = subtitle,
                        startPage = startPage,
                        endPage = endPage,
                        level = raw.level
                    )
                )
            }
            return result
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse PDF outline: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Reads up to 4MB of the PDF file as a Latin-1 string to search for dictionary structures.
     */
    private fun readFileAsStringSafely(file: File): String {
        val maxLen = minOf(file.length(), 4 * 1024 * 1024L).toInt()
        val bytes = ByteArray(maxLen)
        RandomAccessFile(file, "r").use { raf ->
            raf.readFully(bytes)
        }
        return String(bytes, Charset.forName("ISO-8859-1"))
    }

    /**
     * Scans for `/Type /Page` indirect objects in the order they appear.
     */
    private fun extractPageObjectNumbers(pdfContent: String): List<Int> {
        val pageList = mutableListOf<Int>()
        val pageRegex = Regex("""(\d+)\s+(\d+)\s+obj\s*<<[^>]*?/Type\s*/Page\b""")
        for (match in pageRegex.findAll(pdfContent)) {
            val objNum = match.groupValues[1].toIntOrNull()
            if (objNum != null) {
                pageList.add(objNum)
            }
        }
        return pageList
    }

    /**
     * Extracts outline items with `/Title` and destination from the PDF content.
     */
    private fun extractRawOutlineItems(pdfContent: String): List<RawOutlineItem> {
        val items = mutableListOf<RawOutlineItem>()
        // Match dictionary objects that have a /Title and a destination (/Dest or /A << ... /D ... >>)
        val outlineObjRegex = Regex(
            """(\d+)\s+\d+\s+obj\s*<<([^>]*?/Title\s*(?:\((?:\\.|[^)])*\)|<[0-9a-fA-F\s]+>)[^>]*)>>""",
            RegexOption.DOT_MATCHES_ALL
        )

        for (match in outlineObjRegex.findAll(pdfContent)) {
            val objNum = match.groupValues[1].toIntOrNull() ?: continue
            val dictBody = match.groupValues[2]

            // Extract title
            val rawTitle = extractTitle(dictBody) ?: continue
            val (targetObj, directPage) = extractDestination(dictBody)
            val parentObj = extractParentObj(dictBody)

            val level = if (dictBody.contains("/Parent") && !isRootParent(dictBody, pdfContent)) 1 else 0

            items.add(
                RawOutlineItem(
                    title = rawTitle,
                    targetPageObjNum = targetObj,
                    directPageNum = directPage,
                    parentObjNum = parentObj,
                    objNum = objNum,
                    level = level
                )
            )
        }

        return items
    }

    private fun extractTitle(dictBody: String): String? {
        val titleMatch = Regex("""/Title\s*(\((?:\\.|[^)])*\)|<[0-9a-fA-F\s]+>)""").find(dictBody) ?: return null
        val token = titleMatch.groupValues[1].trim()

        return if (token.startsWith("<") && token.endsWith(">")) {
            decodeHexTitle(token.substring(1, token.length - 1).replace(Regex("\\s+"), ""))
        } else if (token.startsWith("(") && token.endsWith(")")) {
            decodeLiteralString(token.substring(1, token.length - 1))
        } else {
            null
        }
    }

    private fun decodeHexTitle(hex: String): String {
        if (hex.length < 2) return ""
        try {
            // Check for UTF-16BE BOM: FEFF
            if (hex.startsWith("FEFF", ignoreCase = true) || hex.startsWith("feff")) {
                val cleanHex = hex.substring(4)
                val sb = StringBuilder()
                var i = 0
                while (i + 4 <= cleanHex.length) {
                    val code = cleanHex.substring(i, i + 4).toInt(16)
                    sb.append(code.toChar())
                    i += 4
                }
                return sb.toString()
            } else {
                // ASCII / Latin1 Hex
                val sb = StringBuilder()
                var i = 0
                while (i + 2 <= hex.length) {
                    val code = hex.substring(i, i + 2).toInt(16)
                    sb.append(code.toChar())
                    i += 2
                }
                return sb.toString()
            }
        } catch (e: Exception) {
            return hex
        }
    }

    private fun decodeLiteralString(literal: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < literal.length) {
            val c = literal[i]
            if (c == '\\' && i + 1 < literal.length) {
                val next = literal[i + 1]
                when (next) {
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000c')
                    '(', ')', '\\' -> sb.append(next)
                    in '0'..'7' -> {
                        // Octal sequence
                        var octal = ""
                        var j = i + 1
                        while (j < literal.length && j < i + 4 && literal[j] in '0'..'7') {
                            octal += literal[j]
                            j++
                        }
                        sb.append(octal.toInt(8).toChar())
                        i = j - 1
                    }
                    else -> sb.append(next)
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun extractDestination(dictBody: String): Pair<Int?, Int?> {
        // Try /Dest [ 10 0 R ... ] or /D [ 10 0 R ... ]
        val objRefMatch = Regex("""/(?:Dest|D)\s*\[\s*(\d+)\s+\d+\s+R""").find(dictBody)
        if (objRefMatch != null) {
            return objRefMatch.groupValues[1].toIntOrNull() to null
        }

        // Try direct page index /Dest [ 3 ... ] or /Dest 3
        val pageNumMatch = Regex("""/(?:Dest|D)\s*(?:\[\s*(\d+)\b|(\d+)\b)""").find(dictBody)
        if (pageNumMatch != null) {
            val num = pageNumMatch.groupValues[1].ifEmpty { pageNumMatch.groupValues[2] }.toIntOrNull()
            return null to num
        }

        return null to null
    }

    private fun extractParentObj(dictBody: String): Int? {
        val parentMatch = Regex("""/Parent\s+(\d+)\s+\d+\s+R""").find(dictBody)
        return parentMatch?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun isRootParent(dictBody: String, fullContent: String): Boolean {
        val parent = extractParentObj(dictBody) ?: return false
        val parentObjRegex = Regex("""$parent\s+\d+\s+obj\s*<<[^>]*?/Type\s*/Outlines\b""")
        return parentObjRegex.containsMatchIn(fullContent)
    }

    /**
     * Fallback: searches text streams in the first pages for a printed "Table of Contents"
     * with lines like "Chapter 1: The Gathering ... 14"
     */
    private fun extractTextToc(
        pdfContent: String,
        totalPages: Int,
        folioId: String
    ): List<ChapterEntity> {
        val result = mutableListOf<ChapterEntity>()
        // Match lines like: Chapter 1 ... 12 or 1. Introduction ... 5
        val linePattern = Regex(
            """(?im)^\s*(?:chapter\s+(\d+|[ivxlcdm]+)[.:\s\-—]+([^\r\n]{3,60})|(\d+)\.\s+([^\r\n]{3,60}))[\s.·…-]+(\d+)\s*$"""
        )

        val matches = linePattern.findAll(pdfContent.take(500_000)).toList()
        if (matches.size >= 2) {
            var chapterIndex = 1
            for (match in matches) {
                val chapNum = match.groupValues[1].toIntOrNull()
                    ?: match.groupValues[3].toIntOrNull()
                    ?: chapterIndex
                val titlePart = match.groupValues[2].ifBlank { match.groupValues[4] }.trim()
                val pageStr = match.groupValues[5]
                val rawPage = pageStr.toIntOrNull() ?: continue
                // Convert 1-based printed page number to 0-based page index
                val startPage = (rawPage - 1).coerceIn(0, totalPages - 1)

                val fullTitle = if (titlePart.isNotBlank()) {
                    if (titlePart.startsWith("Chapter", ignoreCase = true)) titlePart else "Chapter $chapNum · $titlePart"
                } else {
                    "Chapter $chapNum"
                }

                result.add(
                    ChapterEntity(
                        folioId = folioId,
                        chapterNumber = chapNum,
                        title = fullTitle,
                        subtitle = "Printed Page $rawPage",
                        startPage = startPage,
                        endPage = startPage,
                        level = 0
                    )
                )
                chapterIndex++
            }

            // Adjust end pages
            for (i in result.indices) {
                val current = result[i]
                val nextStart = if (i + 1 < result.size) result[i + 1].startPage else totalPages
                result[i] = current.copy(endPage = (nextStart - 1).coerceAtLeast(current.startPage))
            }
        }

        return result
    }

    /**
     * Parses user-entered or pasted table of contents text into ChapterEntity items.
     * Supports formats like:
     * - Chapter 1: The Beginning - 1
     * - 1. Introduction ... 5
     * - Part I: Foundations (p. 12)
     * - The Open Sea - 24
     * - Or simply one title per line (auto-spaced)
     */
    fun parseCustomTocText(
        text: String,
        folioId: String,
        totalPages: Int
    ): List<ChapterEntity> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        data class ParsedLine(
            val title: String,
            val pageNum: Int?,
            val isSubSection: Boolean
        )

        val parsed = mutableListOf<ParsedLine>()
        val pageExtractRegex = Regex("""(?:[-–—:.\s]+|\s*\(?p(?:age|\.)?\s*)(\d+)\)?$""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val isSub = line.startsWith("-") || line.startsWith("•") || line.startsWith("   ") || line.startsWith("\t")
            val cleanLine = line.trimStart('-', '•', '*', ' ', '\t')

            val pageMatch = pageExtractRegex.find(cleanLine)
            val pageNum = pageMatch?.groupValues?.get(1)?.toIntOrNull()
            val titleOnly = if (pageMatch != null) {
                cleanLine.substring(0, pageMatch.range.first).trim(' ', '-', '–', '—', '.', ':', '·', '…')
            } else {
                cleanLine
            }

            if (titleOnly.isNotBlank()) {
                parsed.add(ParsedLine(title = titleOnly, pageNum = pageNum, isSubSection = isSub))
            }
        }

        if (parsed.isEmpty()) return emptyList()

        val chapters = mutableListOf<ChapterEntity>()
        var chapterCounter = 1

        val hasAnyPageNumbers = parsed.any { it.pageNum != null }

        for (i in parsed.indices) {
            val item = parsed[i]
            val startPage = if (hasAnyPageNumbers) {
                val printedPage = item.pageNum ?: if (i > 0) (chapters.last().startPage + 1) else 1
                (printedPage - 1).coerceIn(0, totalPages - 1)
            } else {
                // Evenly distribute across totalPages
                ((i.toDouble() / parsed.size) * totalPages).toInt().coerceIn(0, totalPages - 1)
            }

            val nextStart = if (i + 1 < parsed.size) {
                val nextItem = parsed[i + 1]
                if (hasAnyPageNumbers && nextItem.pageNum != null) {
                    (nextItem.pageNum - 1).coerceIn(0, totalPages - 1)
                } else {
                    (((i + 1).toDouble() / parsed.size) * totalPages).toInt().coerceIn(0, totalPages - 1)
                }
            } else {
                totalPages
            }

            val endPage = (nextStart - 1).coerceAtLeast(startPage).coerceIn(0, totalPages - 1)

            val level = if (item.isSubSection) 1 else 0

            chapters.add(
                ChapterEntity(
                    folioId = folioId,
                    chapterNumber = if (level == 0) chapterCounter++ else chapterCounter,
                    title = item.title,
                    subtitle = if (startPage == endPage) "Page ${startPage + 1}" else "Pages ${startPage + 1}–${endPage + 1}",
                    startPage = startPage,
                    endPage = endPage,
                    level = level
                )
            )
        }

        return chapters
    }
}
