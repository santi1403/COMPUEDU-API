package com.proyecto.AccesoUsuarios.Utils;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Usuario;

import jakarta.servlet.http.HttpServletResponse;

public class ComprobanteExporterPDF {

    private Inscripcion inscripcion;

    public ComprobanteExporterPDF(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, response.getOutputStream());
        documento.open();

        Color verdeEsmeralda = new Color(5, 150, 105);
        Color grisOscuro = new Color(31, 41, 55);
        Color grisClaro = new Color(243, 244, 246);

        String codigoUUID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Usuario creador = inscripcion.getConvocatoria().getCreador();

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, verdeEsmeralda);
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, grisOscuro);
        Font fUUID = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell cellTitulo = new PdfPCell(new Phrase("COMPROBANTE DE INSCRIPCION", fTitulo));
        cellTitulo.setBorder(Rectangle.NO_BORDER);
        cellTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitulo.setPaddingBottom(5);
        header.addCell(cellTitulo);

        PdfPCell cellUUID = new PdfPCell(new Phrase("Codigo de verificacion: " + codigoUUID, fUUID));
        cellUUID.setBorder(Rectangle.NO_BORDER);
        cellUUID.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellUUID.setPaddingBottom(18);
        header.addCell(cellUUID);
        documento.add(header);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5);
        tabla.setWidths(new float[]{1, 2});

        agregarSeccion(tabla, "DATOS DEL ESTUDIANTE", fSubtitulo, grisOscuro);
        String nombreCompleto = inscripcion.getUsuario().getNombre() != null ? inscripcion.getUsuario().getNombre() : inscripcion.getUsuario().getUserName();
        if (inscripcion.getUsuario().getApellido() != null && !inscripcion.getUsuario().getApellido().isEmpty()) {
            nombreCompleto += " " + inscripcion.getUsuario().getApellido();
        }
        agregarFila(tabla, "Nombre:", nombreCompleto, fNegrita, fNormal);
        agregarFila(tabla, "Usuario:", inscripcion.getUsuario().getUserName(), fNegrita, fNormal);
        agregarFila(tabla, "Cedula:", inscripcion.getUsuario().getCedula() != null ? inscripcion.getUsuario().getCedula() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Correo:", inscripcion.getUsuario().getEmail() != null ? inscripcion.getUsuario().getEmail() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Telefono:", inscripcion.getUsuario().getTelefono() != null ? inscripcion.getUsuario().getTelefono() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Direccion:", inscripcion.getUsuario().getDireccion() != null ? inscripcion.getUsuario().getDireccion() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Nivel Educativo:", inscripcion.getUsuario().getNivelEducativo() != null ? inscripcion.getUsuario().getNivelEducativo() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "ID Registro:", String.valueOf(inscripcion.getId()), fNegrita, fNormal);
        agregarFila(tabla, "Estado:", inscripcion.getEstado(), fNegrita, fNormal);

        agregarSeccion(tabla, "CONVOCATORIA", fSubtitulo, grisOscuro);
        agregarFila(tabla, "Titulo:", inscripcion.getConvocatoria().getTitulo(), fNegrita, fNormal);
        agregarFila(tabla, "Categoria:", inscripcion.getConvocatoria().getCategoria() != null ? inscripcion.getConvocatoria().getCategoria() : "N/A", fNegrita, fNormal);
        String area = inscripcion.getConvocatoria().getAreaConocimiento() != null ? inscripcion.getConvocatoria().getAreaConocimiento() : "N/A";
        agregarFila(tabla, "Area de Conocimiento:", area, fNegrita, fNormal);
        agregarFila(tabla, "Modalidad:", inscripcion.getConvocatoria().getModalidad() != null ? inscripcion.getConvocatoria().getModalidad() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Apoyo Financiero:", inscripcion.getConvocatoria().getTipoApoyo() != null ? inscripcion.getConvocatoria().getTipoApoyo() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Precio Semestre:", inscripcion.getConvocatoria().getPrecioSemestre() != null ? inscripcion.getConvocatoria().getPrecioSemestre() : "N/A", fNegrita, fNormal);
        agregarFila(tabla, "Cupos totales:", String.valueOf(inscripcion.getConvocatoria().getCupos()), fNegrita, fNormal);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        agregarFila(tabla, "Fecha Registro:", inscripcion.getFechaInscripcion().format(fmt), fNegrita, fNormal);

        if (creador != null) {
            agregarSeccion(tabla, "INSTITUCION", fSubtitulo, grisOscuro);
            String nombreInst = creador.getNombreInstitucion() != null ? creador.getNombreInstitucion() : creador.getUserName();
            agregarFila(tabla, "Nombre:", nombreInst, fNegrita, fNormal);
            if (creador.getNit() != null) agregarFila(tabla, "NIT:", creador.getNit(), fNegrita, fNormal);
            if (creador.getDescripcionInstitucion() != null) agregarFila(tabla, "Descripcion:", creador.getDescripcionInstitucion(), fNegrita, fNormal);
        }

        String docs = "";
        if (inscripcion.getPdfIcfes() != null) docs += "ICFES: " + inscripcion.getPdfIcfes();
        if (inscripcion.getPdfIdentidad() != null) docs += (docs.isEmpty() ? "" : " | ") + "Identidad: " + inscripcion.getPdfIdentidad();
        if (!docs.isEmpty()) {
            agregarSeccion(tabla, "DOCUMENTOS ADJUNTOS", fSubtitulo, grisOscuro);
            agregarFila(tabla, "Archivos:", docs, fNegrita, fNormal);
        }

        documento.add(tabla);

        Paragraph pie = new Paragraph("\n\nDocumento oficial generado por PortalEdu. Codigo: " + codigoUUID,
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);

        documento.close();
    }

    private void agregarSeccion(PdfPTable tabla, String texto, Font fuente, Color colorFondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setColspan(2);
        celda.setBackgroundColor(colorFondo);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(7);
        celda.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(celda);
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font fNegrita, Font fNormal) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fNegrita));
        c1.setPadding(7);
        c1.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, fNormal));
        c2.setPadding(7);
        c2.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(c2);
    }
}