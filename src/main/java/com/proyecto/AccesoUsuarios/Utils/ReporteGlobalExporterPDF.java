package com.proyecto.AccesoUsuarios.Utils;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;

public class ReporteGlobalExporterPDF {

    private long totalUsuarios;
    private long totalInstituciones;
    private long totalConvocatorias;
    private long totalInscripciones;
    private long pendientes;
    private long aceptadas;
    private long rechazadas;

    public ReporteGlobalExporterPDF(long totalUsuarios, long totalInstituciones, long totalConvocatorias,
                                     long totalInscripciones, long pendientes, long aceptadas, long rechazadas) {
        this.totalUsuarios = totalUsuarios;
        this.totalInstituciones = totalInstituciones;
        this.totalConvocatorias = totalConvocatorias;
        this.totalInscripciones = totalInscripciones;
        this.pendientes = pendientes;
        this.aceptadas = aceptadas;
        this.rechazadas = rechazadas;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Color verde = new Color(5, 150, 105);
        Color grisOscuro = new Color(31, 41, 55);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, verde);
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, grisOscuro);
        Font fFecha = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font fMetrica = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, verde);

        Paragraph titulo = new Paragraph("REPORTE EJECUTIVO GLOBAL", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph fecha = new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " | Admin: manny | PortalEdu v1.0", fFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(20);
        doc.add(fecha);

        doc.add(new Paragraph(" "));

        // Tarjetas de metricas
        PdfPTable cards = new PdfPTable(4);
        cards.setWidthPercentage(100);
        cards.setSpacingAfter(20);

        agregarCard(cards, "Usuarios", String.valueOf(totalUsuarios), verde);
        agregarCard(cards, "Instituciones", String.valueOf(totalInstituciones), new Color(13, 148, 136));
        agregarCard(cards, "Convocatorias", String.valueOf(totalConvocatorias), new Color(217, 119, 6));
        agregarCard(cards, "Inscripciones", String.valueOf(totalInscripciones), grisOscuro);
        doc.add(cards);

        // Tabla de inscripciones por estado
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 2});

        agregarSeccion(tabla, "ESTADO DE INSCRIPCIONES", fSubtitulo, grisOscuro);
        agregarFila(tabla, "Pendientes", String.valueOf(pendientes), fNegrita, fNormal);
        agregarFila(tabla, "Aceptadas", String.valueOf(aceptadas), fNegrita, fNormal);
        agregarFila(tabla, "Rechazadas", String.valueOf(rechazadas), fNegrita, fNormal);
        agregarFila(tabla, "Total", String.valueOf(totalInscripciones), fNegrita, fNegrita);

        double tasa = totalInscripciones > 0 ? (double) aceptadas / totalInscripciones * 100 : 0;
        agregarFila(tabla, "Tasa de insercion", String.format("%.1f%%", tasa), fNegrita, fNegrita);
        doc.add(tabla);

        Paragraph pie = new Paragraph("\n\nPortalEdu - Reporte confidencial generado automaticamente.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);

        doc.close();
    }

    private void agregarCard(PdfPTable tabla, String label, String valor, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(new Color(229, 231, 235));

        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
        pLabel.setSpacingAfter(4);
        Paragraph pValor = new Paragraph(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, color));

        cell.addElement(pLabel);
        cell.addElement(pValor);
        tabla.addCell(cell);
    }

    private void agregarSeccion(PdfPTable tabla, String texto, Font fuente, Color fondo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setColspan(2);
        cell.setBackgroundColor(fondo);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cell);
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font f1, Font f2) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, f1));
        c1.setPadding(6);
        c1.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, f2));
        c2.setPadding(6);
        c2.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(c2);
    }
}
