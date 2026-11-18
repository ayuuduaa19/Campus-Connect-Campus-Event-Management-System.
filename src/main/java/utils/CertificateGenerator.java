package main.java.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class CertificateGenerator {

        public static void generateCertificate(
                        String studentName,
                        String eventName,
                        String eventDate,
                        int registrationId,
                        File outputFile) throws IOException {

                if (studentName == null || eventName == null || outputFile == null) {
                        throw new IllegalArgumentException(
                                        "Certificate details cannot be empty.");
                }

                File parent = outputFile.getAbsoluteFile().getParentFile();

                if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                }

                try (PDDocument document = new PDDocument()) {

                        // Create landscape A4 page
                        PDPage page = new PDPage(
                                        new PDRectangle(
                                                        PDRectangle.A4.getHeight(),
                                                        PDRectangle.A4.getWidth()));

                        document.addPage(page);

                        float pageWidth = page.getMediaBox().getWidth();
                        float pageHeight = page.getMediaBox().getHeight();

                        try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                                // Background
                                content.setNonStrokingColor(new Color(250, 250, 255));
                                content.addRect(0, 0, pageWidth, pageHeight);
                                content.fill();

                                // Outer border
                                content.setStrokingColor(new Color(20, 36, 71));
                                content.setLineWidth(5);
                                content.addRect(25, 25, pageWidth - 50, pageHeight - 50);
                                content.stroke();

                                // Inner border
                                content.setStrokingColor(new Color(117, 81, 184));
                                content.setLineWidth(1.5f);
                                content.addRect(38, 38, pageWidth - 76, pageHeight - 76);
                                content.stroke();

                                // Campus Connect heading
                                drawCenteredText(
                                                content, "CAMPUS CONNECT",
                                                PDType1Font.HELVETICA_BOLD, 25,
                                                pageWidth, pageHeight - 115,
                                                new Color(20, 36, 71));

                                // Certificate title
                                drawCenteredText(
                                                content, "CERTIFICATE OF PARTICIPATION",
                                                PDType1Font.HELVETICA_BOLD, 21,
                                                pageWidth, pageHeight - 175,
                                                new Color(117, 81, 184));

                                // Introductory text
                                drawCenteredText(
                                                content, "This certificate is proudly presented to",
                                                PDType1Font.HELVETICA, 14,
                                                pageWidth, pageHeight - 235,
                                                new Color(71, 85, 105));

                                // Student name
                                drawCenteredText(
                                                content, studentName.toUpperCase(),
                                                PDType1Font.HELVETICA_BOLD, 27,
                                                pageWidth, pageHeight - 290,
                                                new Color(20, 36, 71));

                                // Decorative underline
                                content.setStrokingColor(new Color(117, 81, 184));
                                content.setLineWidth(2);
                                content.moveTo(pageWidth / 2 - 140, pageHeight - 310);
                                content.lineTo(pageWidth / 2 + 140, pageHeight - 310);
                                content.stroke();

                                // Participation message
                                drawCenteredText(
                                                content, "For successfully participating in",
                                                PDType1Font.HELVETICA, 14,
                                                pageWidth, pageHeight - 355,
                                                new Color(71, 85, 105));

                                // Event name
                                drawCenteredText(
                                                content, eventName,
                                                PDType1Font.HELVETICA_BOLD, 19,
                                                pageWidth, pageHeight - 390,
                                                new Color(49, 95, 234));

                                // Event date
                                drawCenteredText(
                                                content, "Event Date: " + safeText(eventDate),
                                                PDType1Font.HELVETICA, 12,
                                                pageWidth, pageHeight - 430,
                                                new Color(71, 85, 105));

                                // Issue date
                                String issueDate = LocalDate.now().format(
                                                DateTimeFormatter.ofPattern("dd MMMM yyyy"));

                                drawCenteredText(
                                                content, "Date of Issue: " + issueDate,
                                                PDType1Font.HELVETICA, 11,
                                                pageWidth, 85,
                                                new Color(71, 85, 105));

                                // Certificate ID
                                drawCenteredText(
                                                content, "Certificate ID: CC-REG-" + registrationId,
                                                PDType1Font.HELVETICA_BOLD, 10,
                                                pageWidth, 65,
                                                new Color(20, 36, 71));

                                // Signature line
                                content.setStrokingColor(new Color(71, 85, 105));
                                content.setLineWidth(1);
                                content.moveTo(pageWidth - 220, 115);
                                content.lineTo(pageWidth - 80, 115);
                                content.stroke();

                                drawTextCenteredAt(
                                                content, "Authorized Signature",
                                                PDType1Font.HELVETICA, 10,
                                                pageWidth - 150, 98,
                                                new Color(71, 85, 105));
                        }

                        document.save(outputFile);
                }
        }

        private static void drawCenteredText(
                        PDPageContentStream content,
                        String text,
                        PDType1Font font,
                        float fontSize,
                        float pageWidth,
                        float y,
                        Color color) throws IOException {

                drawTextCenteredAt(
                                content, text, font, fontSize,
                                pageWidth / 2, y, color);
        }

        private static void drawTextCenteredAt(
                        PDPageContentStream content,
                        String text,
                        PDType1Font font,
                        float fontSize,
                        float centerX,
                        float y,
                        Color color) throws IOException {

                text = safeText(text);

                float textWidth = font.getStringWidth(text) / 1000 * fontSize;
                float x = centerX - textWidth / 2;

                content.beginText();
                content.setFont(font, fontSize);
                content.setNonStrokingColor(color);
                content.newLineAtOffset(x, y);
                content.showText(text);
                content.endText();
        }

        private static String safeText(String text) {

                if (text == null || text.isBlank()) {
                        return "N/A";
                }

                // Standard PDF fonts support WinAnsi characters.
                return text.replaceAll("[^\\x20-\\x7E]", " ");
        }
}