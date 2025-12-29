package com.renttracker.app

import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.data.model.isPendingPayment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentPendingFilterTest {

    @Test
    fun `isPendingPayment returns true only for PARTIAL with pendingAmount greater than zero`() {
        val base = Payment(
            id = 1L,
            date = 1L,
            amount = 100.0,
            paymentMethod = "Cash",
            paymentType = PaymentStatus.PARTIAL,
            pendingAmount = 50.0,
            tenantId = 1L,
            rentMonth = 1L
        )

        assertTrue(base.isPendingPayment())

        assertFalse(base.copy(pendingAmount = 0.0).isPendingPayment())
        assertFalse(base.copy(pendingAmount = null).isPendingPayment())
        assertFalse(base.copy(paymentType = PaymentStatus.FULL, pendingAmount = 50.0).isPendingPayment())
    }
}
