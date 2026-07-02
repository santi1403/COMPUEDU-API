package com.proyecto.AccesoUsuarios.Utils;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.proyecto.AccesoUsuarios.model.Inscripcion;

import jakarta.servlet.http.HttpServletResponse;

public class HojaVidaExporterPDF {

    private List<Inscripcion> inscripciones;
    private String nombreEstudiante;

    public HojaVidaExporterPDF(List<Inscripcion> inscripciones, String nombreEstudiante) {
        this.inscripciones = inscripciones;
        this.nombreEstudiante = nombreEstudiante;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Color verde = new Color(5, 150, 105);
        Color gris = new Color(31, 41, 55);
        Color grisBg = new Color(243, 244, 246);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, verde);
        Font fSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        Font fN = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font fB = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, gris);

        Paragraph titulo = new Paragraph("RESUMEN ACADEMICO DE POSTULACIONES", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph sub = new Paragraph("Estudiante: " + nombreEstudiante + " | Total: " + inscripciones.size() + " postulaciones",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(18);
        doc.add(sub);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        float[] widths = {2.2f, 1.5f, 1f, 1f, 1.2f, 1f};
        table.setWidths(widths);

        addHeader(table, "Convocatoria", fSec, gris);
        addHeader(table, "Institucion", fSec, gris);
        addHeader(table, "Categoria", fSec, gris);
        addHeader(table, "Fecha", fSec, gris);
        addHeader(table, "Estado", fSec, gris);
        addHeader(table, "Folio", fSec, gris);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Inscripcion ins : inscripciones) {
            String inst = ins.getConvocatoria().getCreador() != null
                    && ins.getConvocatoria().getCreador().getNombreInstitucion() != null
                    ? ins.getConvocatoria().getCreador().getNombreInstitucion()
                    : "N/A";

            addCell(table, ins.getConvocatoria().getTitulo(), fN);
            addCell(table, inst, fN);
            addCell(table, ins.getConvocatoria().getCategoria() != null ? ins.getConvocatoria().getCategoria() : "N/A", fN);
            addCell(table, ins.getFechaInscripcion().format(fmt), fN);
            addCell(table, ins.getEstado(), fN);
            addCell(table, "#" + ins.getId(), fN);
        }

        doc.add(table);

        Paragraph pie = new Paragraph("\n\nPortalEdu - Documento oficial de actividad academica externa.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY));
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);

        doc.close();
    }

    private void addHeader(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }
}
