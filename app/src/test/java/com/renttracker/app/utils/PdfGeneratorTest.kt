package com.renttracker.app.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.data.model.Tenant
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PdfGeneratorTest {

    @Test
    fun generatePaymentReportPdfCreatesNonEmptyFile() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        
        val tenant = Tenant(
            id = 1L,
            name = "John Doe",
            mobile = "1234567890",
            rent = 1000.0
        )
        
        val payment = Payment(
            id = 1L,
            date = System.currentTimeMillis(),
            amount = 500.0,
            paymentMethod = "Cash",
            paymentType = PaymentStatus.PARTIAL,
            pendingAmount = 500.0,
            tenantId = 1L,
            rentMonth = System.currentTimeMillis()
        )
        
        val file = PdfGenerator.generatePaymentReportPdf(
            context = context,
            payments = listOf(payment),
            tenants = listOf(tenant),
            reportTitle = "Test Report",
            currency = "INR"
        )
        
        assertTrue("File should exist", file.exists())
        assertTrue("File should not be empty", file.length() > 0)
        assertTrue("File should be a PDF", file.name.endsWith(".pdf"))
    }
}
