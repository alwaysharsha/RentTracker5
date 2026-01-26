package com.renttracker.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.Owner
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.ui.components.formatCurrency
import com.renttracker.app.ui.components.formatDate
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generatePaymentReportPdf(
        context: Context,
        payments: List<Payment>,
        tenants: List<Tenant>,
        reportTitle: String,
        currency: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (approx)

        var pageNumber = 1
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var paint = Paint()
        var yPosition = 50f

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText(reportTitle, 50f, yPosition, paint)
        yPosition += 30f

        // Date
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 40f

        // Table Headers
        paint.textSize = 12f
        paint.isFakeBoldText = true
        val startX = 50f
        val colWidths = floatArrayOf(100f, 100f, 80f, 80f, 80f) // Date, Tenant, Amount, Status, Pending
        
        val headers = listOf("Date", "Tenant", "Amount", "Status", "Pending")
        var currentX = startX
        
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, currentX, yPosition, paint)
            currentX += colWidths[index]
        }
        
        yPosition += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(startX, yPosition, 595f - 50f, yPosition, paint)
        yPosition += 20f

        // Rows
        paint.isFakeBoldText = false
        val tenantMap = tenants.associateBy { it.id }

        for (payment in payments) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = 50f
                
                // Draw headers again on new page
                paint.isFakeBoldText = true
                currentX = startX
                headers.forEachIndexed { index, header ->
                    canvas.drawText(header, currentX, yPosition, paint)
                    currentX += colWidths[index]
                }
                yPosition += 10f
                canvas.drawLine(startX, yPosition, 595f - 50f, yPosition, paint)
                yPosition += 20f
                paint.isFakeBoldText = false
            }

            currentX = startX
            val tenantName = tenantMap[payment.tenantId]?.name ?: "Unknown"
            
            // Date
            canvas.drawText(formatDate(payment.date), currentX, yPosition, paint)
            currentX += colWidths[0]
            
            // Tenant (truncate if too long)
            val safeName = if (tenantName.length > 15) tenantName.substring(0, 12) + "..." else tenantName
            canvas.drawText(safeName, currentX, yPosition, paint)
            currentX += colWidths[1]
            
            // Amount
            canvas.drawText(formatCurrency(payment.amount, currency), currentX, yPosition, paint)
            currentX += colWidths[2]
            
            // Status
            canvas.drawText(payment.paymentType.name, currentX, yPosition, paint)
            currentX += colWidths[3]
            
            // Pending
            val pending = payment.pendingAmount ?: 0.0
            if (pending > 0) {
                 paint.color = Color.RED
                 canvas.drawText(formatCurrency(pending, currency), currentX, yPosition, paint)
                 paint.color = Color.BLACK
            } else {
                 canvas.drawText("-", currentX, yPosition, paint)
            }
            
            yPosition += 20f
        }

        // Summary
        yPosition += 20f
        if (yPosition > 800f) {
            pdfDocument.finishPage(page)
            pageNumber++
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(newPageInfo)
            canvas = page.canvas
            yPosition = 50f
        }
        
        paint.isFakeBoldText = true
        canvas.drawText("Summary", startX, yPosition, paint)
        yPosition += 20f
        
        paint.isFakeBoldText = false
        val totalAmount = payments.sumOf { it.amount }
        val totalPending = payments.sumOf { it.pendingAmount ?: 0.0 }
        
        canvas.drawText("Total Paid: ${formatCurrency(totalAmount, currency)}", startX, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Total Pending: ${formatCurrency(totalPending, currency)}", startX, yPosition, paint)

        pdfDocument.finishPage(page)

        // Save to temporary file
        val file = File(context.cacheDir, "RentTracker_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        } finally {
            pdfDocument.close()
        }
        
        return file
    }

    fun generateTenantListPdf(
        context: Context,
        tenants: List<Tenant>,
        reportTitle: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 50f

        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(reportTitle, 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 14f
        canvas.drawText("Total Tenants: ${tenants.size}", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Name", 50f, yPosition, paint)
        canvas.drawText("Mobile", 250f, yPosition, paint)
        canvas.drawText("Email", 400f, yPosition, paint)
        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 20f

        paint.isFakeBoldText = false
        for (tenant in tenants) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            val name = if (tenant.name.length > 25) tenant.name.substring(0, 22) + "..." else tenant.name
            canvas.drawText(name, 50f, yPosition, paint)
            canvas.drawText(tenant.mobile, 250f, yPosition, paint)
            canvas.drawText(tenant.email ?: "-", 400f, yPosition, paint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "RentTracker_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    fun generateIncomeByBuildingPdf(
        context: Context,
        buildingIncomeMap: Map<Building, Double>,
        currency: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 50f

        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Income by Building", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 30f

        val totalIncome = buildingIncomeMap.values.sum()
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Total Income: ${formatCurrency(totalIncome, currency)}", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        canvas.drawText("Building", 50f, yPosition, paint)
        canvas.drawText("Address", 250f, yPosition, paint)
        canvas.drawText("Income", 450f, yPosition, paint)
        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 20f

        paint.isFakeBoldText = false
        for ((building, income) in buildingIncomeMap) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            val name = if (building.name.length > 25) building.name.substring(0, 22) + "..." else building.name
            canvas.drawText(name, 50f, yPosition, paint)
            val address = building.address ?: "-"
            val shortAddr = if (address.length > 20) address.substring(0, 17) + "..." else address
            canvas.drawText(shortAddr, 250f, yPosition, paint)
            canvas.drawText(formatCurrency(income, currency), 450f, yPosition, paint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "RentTracker_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    fun generateIncomeByOwnerPdf(
        context: Context,
        ownerIncomeMap: Map<Owner, Double>,
        currency: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 50f

        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Income by Owner", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 30f

        val totalIncome = ownerIncomeMap.values.sum()
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Total Income: ${formatCurrency(totalIncome, currency)}", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        canvas.drawText("Owner", 50f, yPosition, paint)
        canvas.drawText("Mobile", 250f, yPosition, paint)
        canvas.drawText("Income", 450f, yPosition, paint)
        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 20f

        paint.isFakeBoldText = false
        for ((owner, income) in ownerIncomeMap) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            val name = if (owner.name.length > 25) owner.name.substring(0, 22) + "..." else owner.name
            canvas.drawText(name, 50f, yPosition, paint)
            canvas.drawText(owner.mobile, 250f, yPosition, paint)
            canvas.drawText(formatCurrency(income, currency), 450f, yPosition, paint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "RentTracker_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }

    fun generateRentRollPdf(
        context: Context,
        tenants: List<Tenant>,
        buildings: List<Building>,
        currency: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 50f
        val buildingMap = buildings.associateBy { it.id }

        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Rent Roll Report", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 50f, yPosition, paint)
        yPosition += 30f

        val totalRent = tenants.sumOf { it.rent ?: 0.0 }
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Total Monthly Rent: ${formatCurrency(totalRent, currency)}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Active Tenants: ${tenants.size}", 50f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 12f
        canvas.drawText("Tenant", 50f, yPosition, paint)
        canvas.drawText("Building", 200f, yPosition, paint)
        canvas.drawText("Mobile", 350f, yPosition, paint)
        canvas.drawText("Rent", 480f, yPosition, paint)
        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 20f

        paint.isFakeBoldText = false
        for (tenant in tenants) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            val name = if (tenant.name.length > 18) tenant.name.substring(0, 15) + "..." else tenant.name
            canvas.drawText(name, 50f, yPosition, paint)
            
            val building = buildingMap[tenant.buildingId]
            val buildingName = building?.name ?: "-"
            val shortBuilding = if (buildingName.length > 18) buildingName.substring(0, 15) + "..." else buildingName
            canvas.drawText(shortBuilding, 200f, yPosition, paint)
            
            canvas.drawText(tenant.mobile, 350f, yPosition, paint)
            canvas.drawText(formatCurrency(tenant.rent ?: 0.0, currency), 480f, yPosition, paint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "RentTracker_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
    }
}
