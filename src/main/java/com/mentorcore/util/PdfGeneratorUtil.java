package com.mentorcore.util;

import com.mentorcore.dto.InformeDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;

/**
 * Utilidad para generar PDFs de informes de seguimiento.
 * RF8
 */
@Component
@Slf4j
public class PdfGeneratorUtil {

    // Colores corporativos MentorCore
    private static final Color COLOR_PRINCIPAL = new Color(50, 155, 156);  // verde #329b9c
    private static final Color COLOR_TEXTO     = new Color(60, 60, 60);

    // Fuentes
    private static final Font FUENTE_TITULO    = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_SECCION   = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRINCIPAL);
    private static final Font FUENTE_ETIQUETA  = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_VALOR     = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_TEXTO);

    /**
     * Genera el PDF del informe de seguimiento de un alumno.
     * Devuelve los bytes del PDF para enviarlo como descarga. RF8
     */
    public byte[] generarInformeAlumno(InformeDTO informe) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            //CABECERA
            agregarCabecera(doc);

            // DATOS DEL ALUMNO 
            agregarSeccion(doc, "Datos del Alumno");
            agregarFila(doc, "Nombre:", informe.getNombreAlumno() + " " + informe.getApellidosAlumno());
            agregarFila(doc, "Grupo:", informe.getGrupo());
            agregarFila(doc, "Tutor Centro:", informe.getNombreTutorCentro());
            doc.add(Chunk.NEWLINE);

            //DATOS DE LA EMPRESA 
            agregarSeccion(doc, "Empresa de Prácticas");
            agregarFila(doc, "Empresa:", informe.getNombreEmpresa());
            agregarFila(doc, "Tutor Empresa:", informe.getNombreTutorEmpresa());
            if (informe.getFechaInicio() != null) {
                agregarFila(doc, "Periodo:", informe.getFechaInicio() + " → " + informe.getFechaFin());
            }
            doc.add(Chunk.NEWLINE);

            //RESUMEN DE HORAS
            agregarSeccion(doc, "Seguimiento de Horas");
            agregarFila(doc, "Horas completadas:",
                    informe.getHorasCompletadas() + " / " + informe.getHorasTotales() + " h");
            double porcentaje = informe.getHorasTotales() > 0
                    ? informe.getHorasCompletadas().doubleValue() * 100 / informe.getHorasTotales()
                    : 0;
            agregarFila(doc, "Progreso:", String.format("%.1f%%", porcentaje));
            doc.add(Chunk.NEWLINE);

            //RESUMEN DE TAREAS
            agregarSeccion(doc, "Resumen de Tareas");
            agregarFila(doc, "Validadas:",   String.valueOf(informe.getTareasValidadas()));
            agregarFila(doc, "Pendientes:",  String.valueOf(informe.getTareasPendientes()));
            agregarFila(doc, "Rechazadas:",  String.valueOf(informe.getTareasRechazadas()));
            doc.add(Chunk.NEWLINE);

            //RESUMEN DE FALTAS
            agregarSeccion(doc, "Faltas de Asistencia");
            agregarFila(doc, "Justificadas:",   String.valueOf(informe.getFaltasJustificadas()));
            agregarFila(doc, "Injustificadas:", String.valueOf(informe.getFaltasInjustificadas()));
            doc.add(Chunk.NEWLINE);

            //VALORACIÓN FINAL
            agregarSeccion(doc, "Valoración Final");
            agregarFila(doc, "Resultado Tutor Centro:",
                    informe.getResultadoTutorCentro() != null
                            ? informe.getResultadoTutorCentro() : "Pendiente");
            agregarFila(doc, "Resultado Tutor Empresa:",
                    informe.getResultadoTutorEmpresa() != null
                            ? informe.getResultadoTutorEmpresa() : "Pendiente");

            if (informe.getObservacionesTutorCentro() != null) {
                agregarFila(doc, "Observaciones Tutor Centro:",
                        informe.getObservacionesTutorCentro());
            }
            if (informe.getObservacionesTutorEmpresa() != null) {
                agregarFila(doc, "Observaciones Tutor Empresa:",
                        informe.getObservacionesTutorEmpresa());
            }

            //PIE
            doc.add(Chunk.NEWLINE);
            Paragraph pie = new Paragraph(
                    "Documento generado automáticamente por MentorCore",
                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);

            doc.close();
            log.info("PDF generado para alumno: {}", informe.getNombreAlumno());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage());
            throw new RuntimeException("Error al generar el informe PDF", e);
        }
    }


    //HELPERS PRIVADOS

    private void agregarCabecera(Document doc) throws DocumentException {
        Paragraph cabecera = new Paragraph("MentorCore — Informe de Seguimiento FE",
                FUENTE_TITULO);
        cabecera.setAlignment(Element.ALIGN_CENTER);
        cabecera.setSpacingAfter(20);

        // Fondo de color usando una tabla de 1 celda
        com.lowagie.text.pdf.PdfPTable tabla = new com.lowagie.text.pdf.PdfPTable(1);
        tabla.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell celda = new com.lowagie.text.pdf.PdfPCell(cabecera);
        celda.setBackgroundColor(COLOR_PRINCIPAL);
        celda.setPadding(15);
        celda.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        tabla.addCell(celda);
        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarSeccion(Document doc, String titulo) throws DocumentException {
        Paragraph p = new Paragraph(titulo, FUENTE_SECCION);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        doc.add(p);

        // Línea separadora
        com.lowagie.text.pdf.PdfPTable linea = new com.lowagie.text.pdf.PdfPTable(1);
        linea.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell celda = new com.lowagie.text.pdf.PdfPCell();
        celda.setBackgroundColor(COLOR_PRINCIPAL);
        celda.setFixedHeight(2);
        celda.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        linea.addCell(celda);
        doc.add(linea);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarFila(Document doc, String etiqueta,
                              String valor) throws DocumentException {
        com.lowagie.text.pdf.PdfPTable tabla = new com.lowagie.text.pdf.PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{35, 65});

        com.lowagie.text.pdf.PdfPCell celdaEtiqueta =
                new com.lowagie.text.pdf.PdfPCell(new Phrase(etiqueta, FUENTE_ETIQUETA));
        celdaEtiqueta.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        celdaEtiqueta.setPaddingBottom(4);

        com.lowagie.text.pdf.PdfPCell celdaValor =
                new com.lowagie.text.pdf.PdfPCell(
                        new Phrase(valor != null ? valor : "—", FUENTE_VALOR));
        celdaValor.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        celdaValor.setPaddingBottom(4);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
        doc.add(tabla);
    }
}
