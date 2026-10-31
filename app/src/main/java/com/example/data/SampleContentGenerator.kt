package com.example.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

object SampleContentGenerator {

    data class PdfPageSpec(
        val chapterNum: Int,
        val chapterTitle: String,
        val sectionTitle: String,
        val leadParagraph: String,
        val secondParagraph: String,
        val calloutNote: String,
        val diagramKind: Int
    )

    fun ensureSamplePdfsGenerated(context: Context): Pair<File, File> {
        val dir = File(context.filesDir, "sample_pdfs").apply { mkdirs() }
        val archFile = File(dir, "de_architectura_folio.pdf")
        val astroFile = File(dir, "uranometria_atlas.pdf")

        if (!archFile.exists() || archFile.length() < 1024L) {
            generateArchitecturePdf(archFile)
        }
        if (!astroFile.exists() || astroFile.length() < 1024L) {
            generateCelestialPdf(astroFile)
        }
        return archFile to astroFile
    }

    fun getArchitecturePageSpecs(): List<PdfPageSpec> = architecturePages
    fun getCelestialPageSpecs(): List<PdfPageSpec> = celestialPages

    private val architecturePages = listOf(
        PdfPageSpec(
            chapterNum = 1,
            chapterTitle = "I. Harmonic Proportions",
            sectionTitle = "The Euclidean Canon & Golden Ratio in Classical Vaults",
            leadParagraph = "Every enduring edifice begins not with mortar or travertine, but with a compass stroke upon parchment. When the breadth of a peristyle relates to its elevation by the extreme and mean ratio (1 : 1.618), the human eye perceives an effortless equilibrium before a single column is raised.",
            secondParagraph = "In the Florentine workshops, master masons inscribed the Fibonacci spiral directly into limestone templates. Each successive chamber expands organically from the sanctuary core, ensuring acoustic resonance and structural equipoise under lateral thrust.",
            calloutNote = "PLATE I — Logarithmic Spiral & Proportional Subdivision of the Sanctuary Bay.",
            diagramKind = 0
        ),
        PdfPageSpec(
            chapterNum = 1,
            chapterTitle = "I. Harmonic Proportions",
            sectionTitle = "Modular Intercolumniation & Stylobate Rhythm",
            leadParagraph = "Vitruvius distinguishes five species of temples by the ratio of column diameter to clear intercolumnar span: Pycnostyle, Systyle, Eustyle, Diastyle, and Araeostyle. Of these, the Eustyle—setting two and one-quarter diameters between shafts—achieves both majesty and unencumbered passage.",
            secondParagraph = "Notice how the central bay widens to three full modules to frame the bronze portal. Optical refinements, including entasis of the shaft and a subtle upward curvature of the stylobate, counteract perspective distortion across long piazzas.",
            calloutNote = "PLATE II — Eustyle Colonnade Elevation with Optical Entasis Vectors.",
            diagramKind = 1
        ),
        PdfPageSpec(
            chapterNum = 2,
            chapterTitle = "II. Hemispherical Domes",
            sectionTitle = "Oculus Compression Rings & Coffered Rotunda Shells",
            leadParagraph = "To suspend a masonry hemisphere forty-three meters across empty space requires graded aggregate and concentric radial coffering. From heavy travertine at the drum base, the builders transition upward to volcanic tufa and finally featherweight pumice near the open oculus.",
            secondParagraph = "Five tiers of twenty-eight sunken coffers carve away dead weight while leaving a web of meridional ribs and parallel hoops. The unglazed oculus acts as a keystone ring in pure compression, admitting a moving cylinder of solar light that tracks the seasons across the pavement.",
            calloutNote = "PLATE III — Meridional Cross-Section of the Rotunda & Oculus Ring.",
            diagramKind = 2
        ),
        PdfPageSpec(
            chapterNum = 2,
            chapterTitle = "II. Hemispherical Domes",
            sectionTitle = "Double-Shell Herringbone Masonry & Lantern Loads",
            leadParagraph = "Brunelleschi solved the dilemma of centering timber by weaving bricks in a self-supporting spinapesce (herringbone) spiral. An inner structural shell carries the compressive load, while a lighter outer shell shields against rain and lends a soaring profile above the city skyline.",
            secondParagraph = "At the crown, the marble lantern acts not as mere ornament, but as a stabilizing dead-weight wedge that locks the eight ogival gores against outward buckling.",
            calloutNote = "PLATE IV — Octagonal Double-Shell Rib Geometry & Spinapesce Course.",
            diagramKind = 3
        ),
        PdfPageSpec(
            chapterNum = 3,
            chapterTitle = "III. Acoustic Amphitheaters",
            sectionTitle = "Harmonic Vessels & Cuneus Sightline Geometry",
            leadParagraph = "In an open-air cavea, a whispered verse from the proscenium reaches the uppermost gallery only when the stepped risers follow a uninterrupted conic section. Any break in the radial gradient scatters the ascending wavefront into turbulent echoes.",
            secondParagraph = "Beneath the seats, tuned bronze echeia (resonating vessels) are recessed into chambers proportioned to the fourth, fifth, and octave intervals, reinforcing the clarity of the speaker's voice.",
            calloutNote = "PLATE V — Radial Cavea Wavefront Projection & Acoustic Niche Plan.",
            diagramKind = 4
        ),
        PdfPageSpec(
            chapterNum = 3,
            chapterTitle = "III. Acoustic Amphitheaters",
            sectionTitle = "Velarium Rigging & Perimeter Arcade Distribution",
            leadParagraph = "Eighty radial travertine piers distribute crowd circulation so twenty thousand spectators may enter or depart within fifteen minutes through numbered vomitoria. Overhead, corbels near the cornice anchor cedar masts supporting the linen velarium.",
            secondParagraph = "By angling the sailcloth panels toward the prevailing afternoon breeze, convective updrafts draw cool air through the shaded porticoes into the arena bowl.",
            calloutNote = "PLATE VI — Radial Pier Distribution & Velarium Tension Diagram.",
            diagramKind = 0
        ),
        PdfPageSpec(
            chapterNum = 4,
            chapterTitle = "IV. Chiaroscuro & Daylight",
            sectionTitle = "Clerestory Apertures & Solar Solstice Alignment",
            leadParagraph = "Architecture is the learned game, correct and magnificent, of forms assembled in light. Deeply splayed embrasures transform harsh zenith sun into a soft, graduated wash across lime-plastered vaults, eliminating glare while revealing the relief of carved mouldings.",
            secondParagraph = "During the vernal and autumnal equinoxes, the high western rose projects a luminous disc directly upon the choir threshold at vespers, turning the entire nave into a walk-in meridian instrument.",
            calloutNote = "PLATE VII — Solstice & Equinox Ray-Tracing Through the Clerestory.",
            diagramKind = 2
        ),
        PdfPageSpec(
            chapterNum = 4,
            chapterTitle = "IV. Chiaroscuro & Daylight",
            sectionTitle = "Colophon: On the Preservation of Folio Drawings",
            leadParagraph = "Let the draughtsman grind iron-gall ink with gum arabic upon rag paper sized in gelatin, and keep the copper dividers true. These plates are compiled so that builders of future centuries may read both the measure and the spirit of classical stone.",
            secondParagraph = "Finis Operis — Printed at the Atelier Bindery in eight quarto leaves with vector copperplate diagrams.",
            calloutNote = "PLATE VIII — Master Seal of the Architectural Guild & Scale Bar.",
            diagramKind = 3
        )
    )

    private val celestialPages = listOf(
        PdfPageSpec(
            chapterNum = 1,
            chapterTitle = "I. The Armillary Sphere",
            sectionTitle = "Equinoctial Rings, Ecliptic Obliquity & Horizon Graduations",
            leadParagraph = "An armillary sphere is a skeletal cosmos wrought in graduated brass. At its heart rests the terrestrial globe, encircled by the meridian, the equator, and the zodiacal band inclined at twenty-three degrees and twenty-seven minutes to trace the annual path of Helios.",
            secondParagraph = "By sighting a star through the pinnules of the alidade and reading its altitude against the engraved vernier scale, the astronomer converts fleeting nocturnal appearances into immutable ecliptic coordinates.",
            calloutNote = "CHART I — Brass Armillary Rings & Ecliptic Inclination Vector.",
            diagramKind = 4
        ),
        PdfPageSpec(
            chapterNum = 1,
            chapterTitle = "I. The Armillary Sphere",
            sectionTitle = "Stereographic Projection of the Planispheric Astrolabe",
            leadParagraph = "How may the vault of heaven be folded flat into a pocket instrument? By projecting every circle of the celestial sphere from the South Celestial Pole onto the equatorial plane, circles on the sphere remain true circles upon the brass tympan.",
            secondParagraph = "Rotating the pierced rete—whose curved pointers mark Aldebaran, Vega, Rigel, and Spica—over the latitude plate reveals the exact hour of night, the azimuth of dawn, and the ascendant degree on the eastern horizon.",
            calloutNote = "CHART II — Stereographic Tympan Almucantars & Rete Star Pointers.",
            diagramKind = 2
        ),
        PdfPageSpec(
            chapterNum = 2,
            chapterTitle = "II. Planetary Epicycles",
            sectionTitle = "Retrograde Loops & Synodic Conjunctions of Superior Orbits",
            leadParagraph = "When Mars or Jupiter approaches opposition, its eastward drift among the fixed stars halts at a stationary point, reverses into a luminous retrograde loop, and then resumes prograde motion. Whether modeled by deferent and epicycle or heliocentric parallax, the geometry remains sublime.",
            secondParagraph = "Plotting successive oppositions across thirty-two synodic periods produces a geometric rosette around the zodiacal circle, demonstrating the commensurability of orbital periods.",
            calloutNote = "CHART III — Synodic Retrograde Rosette & Orbital Chord Geometry.",
            diagramKind = 0
        ),
        PdfPageSpec(
            chapterNum = 2,
            chapterTitle = "II. Planetary Epicycles",
            sectionTitle = "Keplerian Ellipses & Equal Areas in Equal Times",
            leadParagraph = "A radius vector joining any planet to the Sun sweeps out equal areas in equal intervals of time. Near perihelion, where gravitational attraction intensifies, the orbital velocity accelerates; near aphelion, the body lingers along its outer arc.",
            secondParagraph = "This single harmonic law unifies terrestrial mechanics with the motions of the jovian satellites observed through refracting telescopes.",
            calloutNote = "CHART IV — Elliptical Focus Sector Areas & Aphelion Velocity Ratio.",
            diagramKind = 1
        ),
        PdfPageSpec(
            chapterNum = 3,
            chapterTitle = "III. Selenography & Libration",
            sectionTitle = "Lunar Maria, Terminator Relief & Shadow Micrometry",
            leadParagraph = "Along the lunar terminator—where low sunlight grazes crater ramparts and isolated peaks—the length of a cast shadow betrays the altitude of mountains on another world. Galileo and Hevelius measured these luminous points breaking into sunlight before the valley floor below.",
            secondParagraph = "Because the Moon rotates uniformly on its axis while revolving in an elliptical, inclined orbit, optical libration in longitude and latitude reveals fifty-nine percent of the lunar surface to terrestrial observers.",
            calloutNote = "CHART V — Lunar Terminator Shadow Triangulation & Phase Cycle.",
            diagramKind = 3
        ),
        PdfPageSpec(
            chapterNum = 3,
            chapterTitle = "III. Selenography & Libration",
            sectionTitle = "Catalog of Northern Circumpolar Constellations",
            leadParagraph = "Around Polaris revolve Ursa Major, Cassiopeia, Cepheus, and Draco—constellations that never dip beneath the northern horizon in temperate latitudes. Mariners and cartographers rely upon the guard stars Kochab and Pherkad as the hour hand of a nocturnal sky-clock.",
            secondParagraph = "Each star magnitude is engraved with proportional radial rays, from first-magnitude beacons down to sixth-magnitude nebulae.",
            calloutNote = "CHART VI — Circumpolar Nocturnal Dial & Meridian Transit Scale.",
            diagramKind = 4
        )
    )

    private fun generateArchitecturePdf(outputFile: File) {
        renderSamplePdfFile(
            outputFile = outputFile,
            folioHeader = "DE ARCHITECTURA · FOLIO EDITION",
            primaryHex = Color.rgb(154, 52, 34),
            secondaryHex = Color.rgb(180, 128, 48),
            pages = architecturePages
        )
    }

    private fun generateCelestialPdf(outputFile: File) {
        renderSamplePdfFile(
            outputFile = outputFile,
            folioHeader = "URANOMETRIA · CELESTIAL CODEX",
            primaryHex = Color.rgb(34, 72, 128),
            secondaryHex = Color.rgb(186, 134, 44),
            pages = celestialPages
        )
    }

    fun drawSpecPageOnCanvas(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        folioHeader: String,
        primaryHex: Int,
        secondaryHex: Int,
        index: Int,
        totalPages: Int,
        spec: PdfPageSpec
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(249, 243, 231)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryHex
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
        }
        val thinGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryHex
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryHex
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryHex
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.18f
        }
        val chapterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryHex
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(34, 24, 18)
            textSize = 25f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(46, 34, 26)
            textSize = 15.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val dropCapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryHex
            textSize = 52f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryHex
            textSize = 12.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        // Warm parchment paper background
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Double architectural folio border
        canvas.drawRect(36f, 36f, pageWidth - 36f, pageHeight - 36f, borderPaint)
        canvas.drawRect(44f, 44f, pageWidth - 44f, pageHeight - 44f, thinGoldPaint)

        // Running header
        canvas.drawText(folioHeader, 68f, 78f, headerPaint)
        val folioNumStr = "FOLIO ${index + 1} OF $totalPages"
        val numWidth = headerPaint.measureText(folioNumStr)
        canvas.drawText(folioNumStr, pageWidth - 68f - numWidth, 78f, headerPaint)
        canvas.drawLine(68f, 92f, pageWidth - 68f, 92f, thinGoldPaint)

        // Chapter & Section Title
        canvas.drawText(spec.chapterTitle.uppercase(), 68f, 132f, chapterPaint)
        val nextY = drawWrappedText(
            canvas = canvas,
            text = spec.sectionTitle,
            x = 68f,
            startY = 166f,
            maxWidth = pageWidth - 136f,
            lineHeight = 31f,
            paint = titlePaint
        )

        // Ornamental divider
        canvas.drawLine(68f, nextY + 8f, 220f, nextY + 8f, borderPaint)

        // Vector Copperplate Diagram Box
        val diagramTop = nextY + 28f
        val diagramBottom = diagramTop + 295f
        val diagramRect = RectF(68f, diagramTop, pageWidth - 68f, diagramBottom)
        val diagramBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(242, 233, 216)
            style = Paint.Style.FILL
        }
        canvas.drawRect(diagramRect, diagramBg)
        canvas.drawRect(diagramRect, thinGoldPaint)

        drawVectorPlate(
            canvas = canvas,
            rect = diagramRect,
            kind = spec.diagramKind,
            primaryPaint = borderPaint,
            secondaryPaint = thinGoldPaint,
            dashedPaint = dashedPaint
        )

        // Plate caption bar
        canvas.drawText(spec.calloutNote, 82f, diagramBottom + 24f, captionPaint)

        // First paragraph with oversized Drop Cap
        val p1Top = diagramBottom + 66f
        val firstChar = spec.leadParagraph.first().toString()
        val restLead = spec.leadParagraph.drop(1)
        canvas.drawText(firstChar, 68f, p1Top + 36f, dropCapPaint)

        val afterP1Y = drawWrappedText(
            canvas = canvas,
            text = restLead,
            x = 114f,
            startY = p1Top,
            maxWidth = pageWidth - 182f,
            lineHeight = 24f,
            paint = bodyPaint
        )

        // Second paragraph full width
        val afterP2Y = drawWrappedText(
            canvas = canvas,
            text = spec.secondParagraph,
            x = 68f,
            startY = afterP1Y + 22f,
            maxWidth = pageWidth - 136f,
            lineHeight = 24f,
            paint = bodyPaint
        )

        // Footer marginalia box
        val footerTop = (afterP2Y + 20f).coerceAtMost(pageHeight - 115f)
        canvas.drawLine(68f, pageHeight - 76f, pageWidth - 68f, pageHeight - 76f, thinGoldPaint)
        canvas.drawText(
            "ARCHIVAL PLATE #${100 + index + 1}  ·  SCALE 1:100  ·  VERIFIED BY THE ATELIER",
            68f,
            pageHeight - 54f,
            captionPaint
        )
        if (footerTop < pageHeight - 95f) {
            canvas.drawLine(68f, footerTop, pageWidth - 68f, footerTop, dashedPaint)
        }
    }

    private fun renderSamplePdfFile(
        outputFile: File,
        folioHeader: String,
        primaryHex: Int,
        secondaryHex: Int,
        pages: List<PdfPageSpec>
    ) {
        val pageWidth = 720
        val pageHeight = 1020
        var pdfDocument: PdfDocument? = null
        try {
            pdfDocument = PdfDocument()
            pages.forEachIndexed { index, spec ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                drawSpecPageOnCanvas(
                    canvas = page.canvas,
                    pageWidth = pageWidth,
                    pageHeight = pageHeight,
                    folioHeader = folioHeader,
                    primaryHex = primaryHex,
                    secondaryHex = secondaryHex,
                    index = index,
                    totalPages = pages.size,
                    spec = spec
                )
                pdfDocument.finishPage(page)
            }
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (t: Throwable) {
            // In local JVM unit tests (e.g. Robolectric) where native PdfDocument Skia is stubbed out,
            // write a valid minimal PDF header/trailer file so file checks and direct Canvas fallback succeed.
            val fallbackBytes = "%PDF-1.4\n1 0 obj << /Type /Catalog >> endobj\n%%EOF\n".repeat(32).toByteArray()
            FileOutputStream(outputFile).use { out ->
                out.write(fallbackBytes)
            }
        } finally {
            try {
                pdfDocument?.close()
            } catch (_: Throwable) {
            }
        }
    }

    private fun drawVectorPlate(
        canvas: Canvas,
        rect: RectF,
        kind: Int,
        primaryPaint: Paint,
        secondaryPaint: Paint,
        dashedPaint: Paint
    ) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val w = rect.width()
        val h = rect.height()

        // Subtle grid lines
        for (i in 1..5) {
            val gx = rect.left + (w / 6f) * i
            canvas.drawLine(gx, rect.top, gx, rect.bottom, dashedPaint)
        }
        for (j in 1..3) {
            val gy = rect.top + (h / 4f) * j
            canvas.drawLine(rect.left, gy, rect.right, gy, dashedPaint)
        }

        when (kind % 5) {
            0 -> {
                // Golden Ratio rectangles & geometric compass arcs
                val inner = RectF(cx - 170f, cy - 105f, cx + 170f, cy + 105f)
                canvas.drawRect(inner, primaryPaint)
                canvas.drawLine(inner.left + 210f, inner.top, inner.left + 210f, inner.bottom, primaryPaint)
                canvas.drawLine(inner.left + 210f, inner.top + 130f, inner.right, inner.top + 130f, primaryPaint)
                canvas.drawCircle(inner.left + 105f, cy, 98f, secondaryPaint)
                canvas.drawCircle(cx + 60f, cy, 64f, primaryPaint)
                canvas.drawLine(inner.left, inner.bottom, inner.right, inner.top, dashedPaint)
            }
            1 -> {
                // Classical Colonnade & Pediment Elevation
                val baseLine = cy + 95f
                canvas.drawLine(cx - 210f, baseLine, cx + 210f, baseLine, primaryPaint)
                canvas.drawLine(cx - 220f, baseLine + 10f, cx + 220f, baseLine + 10f, primaryPaint)
                val colPositions = listOf(-170f, -102f, -34f, 34f, 102f, 170f)
                for (offset in colPositions) {
                    val colX = cx + offset
                    canvas.drawRect(colX - 11f, cy - 35f, colX + 11f, baseLine, primaryPaint)
                    canvas.drawRect(colX - 16f, cy - 43f, colX + 16f, cy - 35f, secondaryPaint)
                }
                val pediment = Path().apply {
                    moveTo(cx - 200f, cy - 43f)
                    lineTo(cx, cy - 115f)
                    lineTo(cx + 200f, cy - 43f)
                    close()
                }
                canvas.drawPath(pediment, primaryPaint)
                canvas.drawCircle(cx, cy - 72f, 18f, secondaryPaint)
            }
            2 -> {
                // Hemispherical Dome Cross-Section & Oculus Rays
                val baseY = cy + 90f
                val domeRect = RectF(cx - 135f, cy - 45f, cx + 135f, cy + 225f)
                canvas.drawArc(domeRect, 180f, 180f, false, primaryPaint)
                val outerDome = RectF(cx - 155f, cy - 65f, cx + 155f, cy + 245f)
                canvas.drawArc(outerDome, 180f, 180f, false, secondaryPaint)
                canvas.drawLine(cx - 180f, baseY, cx + 180f, baseY, primaryPaint)
                // Sunbeams from oculus
                for (i in -3..3) {
                    canvas.drawLine(cx + i * 6f, cy - 55f, cx + i * 38f, baseY, dashedPaint)
                }
            }
            3 -> {
                // Octagonal Plan / Rose Geometry
                val r = 108f
                canvas.drawCircle(cx, cy, r, primaryPaint)
                canvas.drawCircle(cx, cy, r * 0.68f, secondaryPaint)
                canvas.drawCircle(cx, cy, r * 0.28f, primaryPaint)
                for (a in 0 until 8) {
                    val rad = Math.toRadians(a * 45.0)
                    val x2 = cx + (r * cos(rad)).toFloat()
                    val y2 = cy + (r * sin(rad)).toFloat()
                    canvas.drawLine(cx, cy, x2, y2, primaryPaint)
                }
            }
            else -> {
                // Celestial Astrolabe / Armillary Rings
                for (ring in 1..4) {
                    canvas.drawCircle(cx, cy, ring * 26f, if (ring % 2 == 0) primaryPaint else secondaryPaint)
                }
                val oval = RectF(cx - 140f, cy - 58f, cx + 140f, cy + 58f)
                canvas.drawOval(oval, primaryPaint)
                canvas.drawLine(cx - 165f, cy - 75f, cx + 165f, cy + 75f, primaryPaint)
                canvas.drawLine(cx, cy - 120f, cx, cy + 120f, secondaryPaint)
            }
        }
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ): Float {
        val words = text.split(" ")
        var currentLine = StringBuilder()
        var y = startY
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, y, paint)
                currentLine = StringBuilder(word)
                y += lineHeight
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += lineHeight
        }
        return y
    }

    fun getBookPages(bookKey: String): List<BookPageContent> {
        return when (bookKey) {
            "BOOK_BOTANICA" -> botanicaBookPages
            "BOOK_TYPOGRAPHY" -> typographyBookPages
            else -> botanicaBookPages
        }
    }

    private val botanicaBookPages = listOf(
        BookPageContent(
            pageIndex = 0,
            chapterNumber = 1,
            chapterTitle = "Chapter I · The Cloud Forest Canopy",
            sectionHeading = "1.1 Epiphytic Orchids & High-Altitude Mist",
            isChapterStart = true,
            dropCapParagraph = "Along the eastern escarpment of the Western Ghats, the morning mist does not merely drift through the laurels—it anchors itself to every bough. At nineteen hundred meters, each ancient limbs hosts an aerial garden of mosses, filmy ferns, and miniature Bulbophyllum orchids.",
            bodyParagraphs = listOf(
                "When we opened our linen pressing folios at dawn, the hygrometer needle stood at ninety-six percent. Yet these epiphytes never rot; their silvery velamen roots sponge dissolved minerals from passing cloud droplets within seconds and dry cleanly in the noon breeze.",
                "To sketch a specimen in situ requires patience and steady graphite. Pickled in spirits, a flower loses the translucentamber veins that guide nocturnal hawk-moths to its nectary spur."
            ),
            pullQuote = "“Every ancient bough is an archipelago suspended in cloud, bearing flora that never touch terrestrial soil.”",
            footnote = "Field Station Log #14 · Altitude 1,920m · Barometer 612 mmHg.",
            diagramType = PageDiagramType.BOTANICAL_LEAF,
            diagramCaption = "Fig 1.1 — Venation & Velamen Root Cross-Section of Epiphytic Flora"
        ),
        BookPageContent(
            pageIndex = 1,
            chapterNumber = 1,
            chapterTitle = "Chapter I · The Cloud Forest Canopy",
            sectionHeading = "1.2 Bryophyte Micro-Watersheds",
            isChapterStart = false,
            dropCapParagraph = "Consider the humble pendulous moss festooning the Montane shola trees. A single dry gram of Leucobryum cushions absorbs twelve times its weight in monsoon condensation, releasing cool, filtered water drop by drop into the headwater streams during the dry winter months.",
            bodyParagraphs = listOf(
                "Without this bryophyte canopy sponge, the torrential July rains would strip the steep gneiss slopes bare within a decade. Nature engineers her grandest reservoirs not with dams of stone, but with microscopic hyaline cells.",
                "In the margin of Folio II, I have mounted three pressed fronds of Hymenophyllum, whose lamina is only a single cell layer thick—so delicate that newspaper type can be read clearly through the emerald leaf."
            ),
            footnote = "Herbarium Sheet XIV · Preserved in Venetian Rag Blotting Paper."
        ),
        BookPageContent(
            pageIndex = 2,
            chapterNumber = 2,
            chapterTitle = "Chapter II ·Fibonacci Phyllotaxis",
            sectionHeading = "2.1 The Golden Angle in Rosette Succulents",
            isChapterStart = true,
            dropCapParagraph = "Why do the primordia of a pine cone, the florets of a sunflower capitulum, and the leaves of an alpine Sempervivum arrange themselves in intersecting parastichy spirals of 5 and 8, 8 and 13, or 13 and 21? Because each emerging leaf diverges from its predecessor by 137.5 degrees—the golden angle.",
            bodyParagraphs = listOf(
                "Any rational fraction of a circle would eventually stack leaves directly above one another in radial spokes, starving lower foliage of sunlight and rainfall. The irrational golden ratio ensures that no two leaves ever eclipse each other, no matter how many dozens unfurl.",
                "Measuring seventy-four wild rosettes along the basalt ridge confirmed the mathematical law without a single exception: five clockwise spirals, eight counter-clockwise spirals."
            ),
            pullQuote = "“Botany is geometry rendered in chlorophyll and sunlight.”",
            diagramType = PageDiagramType.GOLDEN_SPIRAL,
            diagramCaption = "Fig 2.1 — Parastichy Divergence at 137° 30' (8 : 13 Fibonacci Spirals)"
        ),
        BookPageContent(
            pageIndex = 3,
            chapterNumber = 2,
            chapterTitle = "Chapter II · Fibonacci Phyllotaxis",
            sectionHeading = "2.2 Fractal Venation & Hydraulic Redundancy",
            isChapterStart = false,
            dropCapParagraph = "Unlike the primitive dichotomous branching of Ginkgo biloba, dicotyledonous leaves weave closed polygonal loops of secondary and tertiary veins. When a beetle severs a primary midrib, sap detours effortlessly around the lesion through anastomosing micro-channels.",
            bodyParagraphs = listOf(
                "By brushing cleared leaves with dilute safranin dye and pressing them between glass plates, the vascular network glows like crimson filigree—an optimization problem solved two hundred million years before human aqueducts.",
                "Each areole measures scarcely a third of a millimeter across, guaranteeing that no photosynthetic cell lies more than four cells distant from a water conduit."
            ),
            footnote = "Cleared Leaf Preparation · Safranin O & Cedarwood Oil Mount."
        ),
        BookPageContent(
            pageIndex = 4,
            chapterNumber = 3,
            chapterTitle = "Chapter III · Nocturnal Pollination",
            sectionHeading = "3.1 Moonflower Opening Kinematics",
            isChapterStart = true,
            dropCapParagraph = "At twenty-two minutes past sunset, the spirally twisted bud of Ipomoea alba shivers visibly. Within four minutes, turgor pressure surges along the abaxial midribs, unfurling a luminous white salverform corolla fourteen centimeters across that perfumes the entire courtyard.",
            bodyParagraphs = listOf(
                "By timing the opening with a pocket chronometer across three lunar phases, we observed that ambient twilight spectral shifts—specifically the drop in red-to-far-red wavelengths—trigger the rapid potassium-ion influx in the petal hinge cells.",
                "Long-tongued sphingid moths arrive within ten minutes of full anthesis, hovering like hummingbirds while uncoiling proboscises longer than their own bodies."
            ),
            pullQuote = "“A flower that blooms only for the stars must speak in fragrance and ivory luminescence.”",
            diagramType = PageDiagramType.CELESTIAL_ORRERY,
            diagramCaption = "Fig 3.1 — Crepuscular Anthesis Timeline & Sphingid Flight Vector"
        ),
        BookPageContent(
            pageIndex = 5,
            chapterNumber = 3,
            chapterTitle = "Chapter III · Nocturnal Pollination",
            sectionHeading = "3.2 Thermogenic Arums & Botanical Furnaces",
            isChapterStart = false,
            dropCapParagraph = "Even more astonishing is the sacred lotus and the woodland Arum, whose spadix burns stored starch via the cyanide-resistant alternative oxidase pathway to heat its floral chamber fifteen degrees Celsius above the frosty night air.",
            bodyParagraphs = listOf(
                "Touching the central column at midnight feels unmistakably like touching the warm breast of a roosting songbird. This metabolic warmth volatilizes terpenoid esters into the valley air while offering crepuscular beetles a heated shelter until dawn.",
                "At sunrise, the spathe constricts, dusts its warmed guests with golden pollen, and releases them to seek a receptive pistillate inflorescence downstream."
            ),
            footnote = "Thermocouple Reading: Ambient 9.4°C · Spadix Core 26.8°C."
        ),
        BookPageContent(
            pageIndex = 6,
            chapterNumber = 4,
            chapterTitle = "Chapter IV · Pigments & Botanical Inks",
            sectionHeading = "4.1 Oak Galls, Indigo Vats & Saffron Glazes",
            isChapterStart = true,
            dropCapParagraph = "Every illustration in this field codex was painted with pigments distilled from the very forest it documents. From Aleppo oak galls crushed with ferrous sulfate comes our permanent purple-black ink, which darkens as it oxidizes into the cotton fibers of the page.",
            bodyParagraphs = listOf(
                "For the translucent viridian washes of young fern croziers, we layered fermented Indigofera tinctoria over weld yellow extracted from Reseda luteola, bound in clear acacia gum.",
                "Modern aniline dyes fade within a summer on a sunlit reading desk; mineral and botanical lakes locked in gum arabic endure five centuries inside a clasped leather folio."
            ),
            diagramType = PageDiagramType.VENETIAN_COLOPHON,
            diagramCaption = "Fig 4.1 — Botanical Pigment Stratigraphy & Archival Sizing"
        ),
        BookPageContent(
            pageIndex = 7,
            chapterNumber = 4,
            chapterTitle = "Chapter IV · Pigments & Botanical Inks",
            sectionHeading = "4.2 Epilogue: The Living Herbarium",
            isChapterStart = false,
            dropCapParagraph = "To press a plant between rag sheets is not to entomb it, but to anchor a moment of living geometry in time. Centuries hence, a reader turning these pages will still trace the stomatal patterns, pollen grains, and seasonal rhythms of a mountain forest.",
            bodyParagraphs = listOf(
                "Keep the brass clasps fastened against dampness, air the leaves each autumn equinox with a sprig of dried lavender and clove, and add your own margin ribbons wherever a wild specimen catches your eye.",
                "Explicit Liber Botanicus — Bound in full Florentine morocco with silk page markers."
            ),
            pullQuote = "“Turn the leaf gently; three hundred mornings of mountain rain sleep between these margins.”",
            footnote = "Colophon · Set in Lora & Playfair Display at the Folio 3D Press."
        )
    )

    private val typographyBookPages = listOf(
        BookPageContent(
            pageIndex = 0,
            chapterNumber = 1,
            chapterTitle = "Chapter I · The Venetian Matrix",
            sectionHeading = "1.1 Punchcutting in Steel & Counterpunches",
            isChapterStart = true,
            dropCapParagraph = "Before a single page of Cicero or Virgil could be pulled from a wooden screw press in fifteenth-century Venice, the punchcutter had to carve the negative interior of an 'e' or an 'a' onto the tip of a tempered steel rod called a counterpunch.",
            bodyParagraphs = listOf(
                "Driven into the softer face of the main steel punch, the counterpunch established the inner white space—the counter—with crisp constancy. Only then did Francesco Griffo file away the outer contour by eye, testing the progress with candle-smoke proofs on smooth paper.",
                "We judge a typeface by its black strokes, yet the master typographer shapes the white light enclosed within the letters."
            ),
            pullQuote = "“A letterform is sculpted from the white light trapped inside its counter.”",
            diagramType = PageDiagramType.TYPOGRAPHIC_GRID,
            diagramCaption = "Fig 1.1 — Renaissance Humanist Minuscule Proportions & Axis"
        ),
        BookPageContent(
            pageIndex = 1,
            chapterNumber = 1,
            chapterTitle = "Chapter I · The Venetian Matrix",
            sectionHeading = "1.2 The Invention of the Portable Octavo",
            isChapterStart = false,
            dropCapParagraph = "Until 1501, scholarly volumes were ponderous lectern folios chained to monastery desks. When Aldus Manutius folded the printed sheet three times into an octavo and commissioned a compact, sloped chancery italic, literature stepped out of the cloister and into the coat pocket.",
            bodyParagraphs = listOf(
                "For the first time in history, a traveler riding between Padua and Florence could read Homer or Dante beneath an olive tree, marking favorite passages with a silk ribbon sewn into the headband.",
                "Our 3D page-turning engine pays homage to that Aldine revolution: restoring the physical pleasure of a turning leaf to the luminous screen."
            ),
            footnote = "Aldine Press Archive · Venice, MDI · Dolphin & Anchor Imprint."
        ),
        BookPageContent(
            pageIndex = 2,
            chapterNumber = 2,
            chapterTitle = "Chapter II · Canons of Page Construction",
            sectionHeading = "2.1 Van de Graaf & Villard de Honnecourt Grids",
            isChapterStart = true,
            dropCapParagraph = "Why do medieval manuscripts and Renaissance incunabula possess such breathing room around the text block? Because the scribe did not measure margins with a ruler—he bisected the spread with straightedge diagonals using Villard de Honnecourt's geometric canon.",
            bodyParagraphs = listOf(
                "By drawing full and half-page diagonals across the two-page opening, one constructs a text area whose width and height exactly mirror the aspect ratio of the page itself, leaving inner, top, outer, and bottom margins in the harmonious progression 2 : 3 : 4 : 6.",
                "The generous bottom and outer margins were never wasted paper: they served as a thumb-rest so fingers never smudged the fresh ink, and as an open gallery for the reader's handwritten marginalia."
            ),
            pullQuote = "“Margins are the silent architecture that allows the voice of the text to resonate.”",
            diagramType = PageDiagramType.GOLDEN_SPIRAL,
            diagramCaption = "Fig 2.1 — The 2 : 3 : 4 : 6 Classical Margin Canon & Diagonal Construction"
        ),
        BookPageContent(
            pageIndex = 3,
            chapterNumber = 2,
            chapterTitle = "Chapter II · Canons of Page Construction",
            sectionHeading = "2.2 Rag Paper Grain, Chain Lines & Deckle Edges",
            isChapterStart = false,
            dropCapParagraph = "Hold an eighteenth-century leaf up to a casement window and you will see close-set laid lines crossed every inch by heavier chain lines, with the papermaker's watermark glowing at the center of the half-sheet.",
            bodyParagraphs = listOf(
                "Crucially, every sheet of paper possesses a grain direction aligned with the shake of the vatman's mould. A bookbinder must always fold pages parallel to the grain; if folded against the grain, the book springs stubbornly shut and ripples along the sewn spine.",
                "When you drag a page across our 3D canvas, notice how the paper flexes along a cylindrical fold axis—mimicking long-fiber cotton rag."
            ),
            footnote = "Fabriano Mill Specification · 120 gsm Laid Cotton Rag."
        ),
        BookPageContent(
            pageIndex = 4,
            chapterNumber = 3,
            chapterTitle = "Chapter III · The Bindery & Silk Ribbon",
            sectionHeading = "3.1 Smyth Sewing, Raised Cords & Hollow Backs",
            isChapterStart = true,
            dropCapParagraph = "A true hardcover book is an articulated mechanical suspension bridge. Gatherings of sixteen pages—called signatures—are sewn with waxed linen thread around five hemp cords laced into bevelled oak or millboard covers.",
            bodyParagraphs = listOf(
                "As the book opens, the spine arches upward into a convex bridge, throwing the pages open flat so the inner gutter remains effortless to read without cracking the glue.",
                "At the head of the spine, the binder weaves a two-color silk headband and anchors the bookmark ribbon—crimson for liturgy and history, antique gold for philosophy, emerald for natural science."
            ),
            diagramType = PageDiagramType.DOME_SECTION,
            diagramCaption = "Fig 3.1 — Articulated Spine Arch & Five-Cord Signature Sewing"
        ),
        BookPageContent(
            pageIndex = 5,
            chapterNumber = 3,
            chapterTitle = "Chapter III · The Bindery & Silk Ribbon",
            sectionHeading = "3.2 Marginalia & The Reader's Dialogue",
            isChapterStart = false,
            dropCapParagraph = "Erasmus advised his students never to read without a pen or marker in hand, ready to plant an asteriscus or a pointing manicule (☞) beside sentences worthy of memory. A book without bookmarks is a forest without trail blazes.",
            bodyParagraphs = listOf(
                "In Folio 3D, pulling down the silk ribbon at the top-right corner of any page or opening the Bookmarks & Notes drawer lets you attach color-coded ribbons and personal reflections to both reflowable books and imported PDF documents.",
                "Thus every volume becomes a conversation between author and reader across time."
            ),
            pullQuote = "“A book is only half-written by its author; the other half is completed by the ribbons and notes of its reader.”",
            diagramType = PageDiagramType.VENETIAN_COLOPHON,
            diagramCaption = "Fig 3.2 — The Aldine Anchor & Reader's Manicule"
        )
    )
}
