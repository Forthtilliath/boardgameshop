package com.bgs.boardgameshop.order;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Génère une facture PDF simple pour une commande (dessin direct via PDFBox, pas de
 * moteur de templating HTML — aucun Thymeleaf dans ce projet).
 */
@Service
public class InvoiceService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE).withZone(ZoneId.systemDefault());
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 18;

    public byte[] generateInvoice(Order order) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(content, bold, 20, MARGIN, y, "BGS — Facture");
                y -= LINE_HEIGHT / 2;
                y = writeLine(content, regular, 11, MARGIN, y, "Commande n°" + order.getId());
                y = writeLine(content, regular, 11, MARGIN, y, "Date : " + DATE_FORMAT.format(order.getCreatedAt()));
                y = writeLine(content, regular, 11, MARGIN, y,
                        "Client : " + order.getUser().getFirstName() + " " + order.getUser().getLastName()
                                + " (" + order.getUser().getEmail() + ")");
                y -= LINE_HEIGHT;

                float col1 = MARGIN;
                float col2 = 300;
                float col3 = 380;
                float col4 = 460;

                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(col1, y);
                content.showText("Article");
                content.endText();
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(col2, y);
                content.showText("Qté");
                content.endText();
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(col3, y);
                content.showText("Prix unit.");
                content.endText();
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(col4, y);
                content.showText("Total");
                content.endText();
                y -= LINE_HEIGHT / 2;
                content.moveTo(MARGIN, y);
                content.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
                content.stroke();
                y -= LINE_HEIGHT;

                for (OrderLine line : order.getLines()) {
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(col1, y);
                    content.showText(truncate(line.getGameName(), 40));
                    content.endText();
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(col2, y);
                    content.showText(String.valueOf(line.getQuantity()));
                    content.endText();
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(col3, y);
                    content.showText(formatAmount(line.getUnitPrice().doubleValue()));
                    content.endText();
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(col4, y);
                    content.showText(formatAmount(line.getLineTotal().doubleValue()));
                    content.endText();
                    y -= LINE_HEIGHT;
                }

                y -= LINE_HEIGHT / 2;
                content.moveTo(MARGIN, y);
                content.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
                content.stroke();
                y -= LINE_HEIGHT;

                writeLine(content, bold, 13, col3, y, "Total : " + formatAmount(order.getTotalAmount().doubleValue()) + " €");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de générer la facture PDF", e);
        }
    }

    private float writeLine(PDPageContentStream content, PDType1Font font, float size, float x, float y, String text)
            throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - LINE_HEIGHT;
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    private String formatAmount(double amount) {
        return String.format(Locale.FRANCE, "%.2f", amount);
    }
}
