package com.medicalstore.pos.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.medicalstore.pos.dto.response.BillItemResponse;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.dto.response.PaymentResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfBillService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public byte[] generateBillPdf(BillResponse bill) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        try {
            // Fonts
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Header
            Paragraph header = new Paragraph("MEDICAL STORE")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(header);
            
            Paragraph subHeader = new Paragraph("TAX INVOICE")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(subHeader);
            
            // Bill Info Table
            Table billInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);
            
            billInfoTable.addCell(createCell("Bill No:", boldFont, 10, false));
            billInfoTable.addCell(createCell(bill.getBillNumber(), normalFont, 10, false));
            
            billInfoTable.addCell(createCell("Date:", boldFont, 10, false));
            String dateTime = bill.getBillDate().format(DATE_FORMATTER) + " " + 
                             bill.getBillDate().format(TIME_FORMATTER);
            billInfoTable.addCell(createCell(dateTime, normalFont, 10, false));
            
            billInfoTable.addCell(createCell("Cashier:", boldFont, 10, false));
            billInfoTable.addCell(createCell(bill.getCashierName(), normalFont, 10, false));
            
            if (bill.getCustomerName() != null && !bill.getCustomerName().trim().isEmpty()) {
                billInfoTable.addCell(createCell("Customer:", boldFont, 10, false));
                billInfoTable.addCell(createCell(bill.getCustomerName(), normalFont, 10, false));
            }
            
            if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().trim().isEmpty()) {
                billInfoTable.addCell(createCell("Phone:", boldFont, 10, false));
                billInfoTable.addCell(createCell(bill.getCustomerPhone(), normalFont, 10, false));
            }
            
            document.add(billInfoTable);
            
            // Items Table Header
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);
            
            itemsTable.addHeaderCell(createCell("Item", boldFont, 10, true));
            itemsTable.addHeaderCell(createCell("Qty", boldFont, 10, true));
            itemsTable.addHeaderCell(createCell("Price", boldFont, 10, true));
            itemsTable.addHeaderCell(createCell("GST", boldFont, 10, true));
            itemsTable.addHeaderCell(createCell("Total", boldFont, 10, true));
            
            // Items
            BigDecimal totalItemsSubtotal = BigDecimal.ZERO;
            BigDecimal totalGst = BigDecimal.ZERO;
            
            for (BillItemResponse item : bill.getItems()) {
                itemsTable.addCell(createCell(item.getMedicineName(), normalFont, 9, false));
                itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), normalFont, 9, false));
                itemsTable.addCell(createCell(formatCurrency(item.getUnitPrice()), normalFont, 9, false));
                itemsTable.addCell(createCell(formatCurrency(item.getGstAmount()), normalFont, 9, false));
                itemsTable.addCell(createCell(formatCurrency(item.getTotalAmount()), normalFont, 9, false));
                
                BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalItemsSubtotal = totalItemsSubtotal.add(itemSubtotal);
                totalGst = totalGst.add(item.getGstAmount());
            }
            
            document.add(itemsTable);
            
            // Summary Table
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(10)
                    .setMarginBottom(10);
            
            summaryTable.addCell(createCell("Subtotal:", boldFont, 10, false));
            summaryTable.addCell(createCell(formatCurrency(bill.getSubtotal()), normalFont, 10, false)
                    .setTextAlignment(TextAlignment.RIGHT));
            
            summaryTable.addCell(createCell("GST:", boldFont, 10, false));
            summaryTable.addCell(createCell(formatCurrency(bill.getTotalGst()), normalFont, 10, false)
                    .setTextAlignment(TextAlignment.RIGHT));
            
            summaryTable.addCell(createCell("TOTAL:", boldFont, 12, false));
            summaryTable.addCell(createCell(formatCurrency(bill.getTotalAmount()), boldFont, 12, false)
                    .setTextAlignment(TextAlignment.RIGHT));
            
            document.add(summaryTable);
            
            // Payments
            if (bill.getPayments() != null && !bill.getPayments().isEmpty()) {
                Paragraph paymentHeader = new Paragraph("PAYMENT DETAILS")
                        .setFont(boldFont)
                        .setFontSize(11)
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(paymentHeader);
                
                Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth()
                        .setMarginBottom(10);
                
                for (PaymentResponse payment : bill.getPayments()) {
                    paymentTable.addCell(createCell(payment.getMode().toString(), normalFont, 9, false));
                    paymentTable.addCell(createCell(formatCurrency(payment.getAmount()), normalFont, 9, false)
                            .setTextAlignment(TextAlignment.RIGHT));
                }
                
                document.add(paymentTable);
            }
            
            // Footer
            Paragraph footer = new Paragraph("Thank you for your business!")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15);
            document.add(footer);
            
            Paragraph footer2 = new Paragraph("This is a computer generated invoice")
                    .setFont(smallFont)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5);
            document.add(footer2);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    private Cell createCell(String text, PdfFont font, float fontSize, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font).setFontSize(fontSize));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setBold();
        }
        cell.setPadding(5);
        return cell;
    }
    
    private String formatCurrency(BigDecimal amount) {
        return "₹" + String.format("%.2f", amount);
    }
}

