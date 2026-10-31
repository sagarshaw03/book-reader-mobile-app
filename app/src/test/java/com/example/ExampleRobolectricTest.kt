package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleContentGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and verify sample PDFs and books`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Folio 3D", appName)

        val (archPdf, astroPdf) = SampleContentGenerator.ensureSamplePdfsGenerated(context)
        assertTrue("Architecture sample PDF should exist", archPdf.exists() && archPdf.length() > 0L)
        assertTrue("Celestial sample PDF should exist", astroPdf.exists() && astroPdf.length() > 0L)

        val botanicaPages = SampleContentGenerator.getBookPages("BOOK_BOTANICA")
        assertTrue("Botanica literary book should have multiple pages", botanicaPages.size >= 8)

        val database = com.example.data.FolioDatabase.getDatabase(context)
        val repository = com.example.data.FolioRepository(context, database.folioDao())
        kotlinx.coroutines.runBlocking {
            repository.ensureSeeded()
            val chapters = database.folioDao().getChaptersForFolio("folio_arch_pdf")
            val firstList = chapters.first()
            println("ARCH CHAPTERS COUNT: " + firstList.size + " -> " + firstList.map { it.title })
            assertTrue("Expected at least 8 chapters for architecture, but got: " + firstList.size, firstList.size >= 8)
        }
    }
}
