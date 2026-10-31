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
    }
}
