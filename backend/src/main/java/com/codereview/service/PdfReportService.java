package com.codereview.service;

import com.codereview.dto.ReviewDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class PdfReportService {

    private static final BaseColor PRIMARY   = new BaseColor(99, 102, 241);   // Indigo
    private static final BaseColor SUCCESS   = new BaseColor(34, 197, 94);    // Green
    private static final BaseColor DANGER    = new BaseColor(239, 68, 68);    // Red
    private static final BaseColor WARNING   = new BaseColor(245, 158, 11);   // Amber
    private static final BaseColor DARK      = new BaseColor(15, 23, 42);
    private static final BaseColor LIGHT_BG  = new BaseColor(248, 250, 252);
    private static final BaseColor MUTED     = new BaseColor(100, 116, 139);

    public byte[] generateReport(ReviewDto.ReviewResponse review) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, review);
            addScores(document, review);
            addSection(document, "🐛 Issues Found", review.getIssues(), DANGER);
            addSection(document, "💡 Suggestions", review.getSuggestions(), PRIMARY);
            addSection(document, "🔒 Security Warnings", review.getSecurityWarnings(), WARNING);
            addSection(document, "⚡ Performance Notes", review.getPerformanceNotes(), SUCCESS);
            addCodeSection(document, review);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }

    private void addHeader(Document doc, ReviewDto.ReviewResponse review) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PRIMARY);
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 11, MUTED);

        Paragraph title = new Paragraph("AI Code Review Report", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(4);
        doc.add(title);

        doc.add(new Paragraph("Language: " + review.getLanguage().toUpperCase() +
                " | Date: " + review.getCreatedAt() +
                " | Status: " + review.getStatus(), subFont));

        addSeparator(doc);

        if (review.getSummary() != null) {
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA, 11, DARK);
            Paragraph summary = new Paragraph("Summary: " + review.getSummary(), summaryFont);
            summary.setSpacingBefore(6);
            summary.setSpacingAfter(6);
            doc.add(summary);
        }

        addSeparator(doc);
    }

    private void addScores(Document doc, ReviewDto.ReviewResponse review) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, DARK);
        doc.add(new Paragraph("Scores", sectionFont));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        addScoreCell(table, "Quality", review.getQualityScore());
        addScoreCell(table, "Readability", review.getReadabilityScore());
        addScoreCell(table, "Security", review.getSecurityScore());
        addScoreCell(table, "Performance", review.getPerformanceScore());

        doc.add(table);
    }

    private void addScoreCell(PdfPTable table, String label, Integer score) {
        int s = score == null ? 0 : score;
        BaseColor color = s >= 80 ? SUCCESS : s >= 60 ? WARNING : DANGER;

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
        Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, color);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        cell.setPadding(12);
        cell.setBackgroundColor(LIGHT_BG);

        Paragraph content = new Paragraph();
        content.add(new Chunk(s + "/100\n", scoreFont));
        content.add(new Chunk(label, labelFont));
        cell.addElement(content);
        table.addCell(cell);
    }

    private void addSection(Document doc, String title, List<String> items, BaseColor color)
            throws DocumentException {
        if (items == null || items.isEmpty()) return;

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, color);
        Font itemFont    = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK);

        Paragraph heading = new Paragraph(title, sectionFont);
        heading.setSpacingBefore(12);
        heading.setSpacingAfter(6);
        doc.add(heading);

        for (String item : items) {
            Paragraph p = new Paragraph("• " + item, itemFont);
            p.setIndentationLeft(15);
            p.setSpacingAfter(3);
            doc.add(p);
        }
    }

    private void addCodeSection(Document doc, ReviewDto.ReviewResponse review)
            throws DocumentException {
        if (review.getOriginalCode() == null) return;

        addSeparator(doc);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, DARK);
        Font codeFont    = FontFactory.getFont(FontFactory.COURIER, 8, new BaseColor(51, 65, 85));

        doc.add(new Paragraph("Original Code", sectionFont));

        PdfPCell codeCell = new PdfPCell();
        codeCell.setBorder(Rectangle.BOX);
        codeCell.setBorderColor(new BaseColor(203, 213, 225));
        codeCell.setBackgroundColor(new BaseColor(241, 245, 249));
        codeCell.setPadding(10);

        String code = review.getOriginalCode();
        if (code.length() > 3000) code = code.substring(0, 3000) + "\n... [truncated]";
        codeCell.addElement(new Phrase(code, codeFont));

        PdfPTable codeTable = new PdfPTable(1);
        codeTable.setWidthPercentage(100);
        codeTable.setSpacingBefore(8);
        codeTable.addCell(codeCell);
        doc.add(codeTable);
    }

    private void addSeparator(Document doc) throws DocumentException {
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(new BaseColor(226, 232, 240));
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);
    }
}
