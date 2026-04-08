package com.mentorcore.service;

import com.mentorcore.dto.InformeDTO;
import com.mentorcore.model.Alumno;

import com.mentorcore.model.enums.TipoEvaluadorEnum;

import com.mentorcore.util.PdfGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




/**
 * Servicio de generación de informes de seguimiento.
 * RF8, RF14
 */
@Service
@RequiredArgsConstructor
public class InformeService {

    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final TareaService tareaService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final ValoracionService valoracionService;
    private final AsignacionService asignacionService;


    /**
     * Construye el DTO con todos los datos del alumno
     * y genera el PDF de su informe de seguimiento. RF8
     */
    @Transactional(readOnly = true)
    public byte[] generarInformePdf(Alumno alumno) {
        InformeDTO dto = construirInforme(alumno);
        return pdfGeneratorUtil.generarInformeAlumno(dto);
    }

    /**
     * Construye el InformeDTO con los datos del alumno. RF8
     * Reutilizable para mostrar vista previa en pantalla.
     */
    @Transactional(readOnly = true)
    public InformeDTO construirInforme(Alumno alumno) {
        InformeDTO dto = new InformeDTO();

        //Datos del alumno
        dto.setNombreAlumno(alumno.getNombre());
        dto.setApellidosAlumno(alumno.getApellidos());
        dto.setGrupo(alumno.getGrupo());
        dto.setHorasCompletadas(alumno.getHorasCompletadas());
        dto.setHorasTotales(alumno.getHorasTotalesFe());

        //Tutor centro
        if (alumno.getTutorCentro() != null) {
            dto.setNombreTutorCentro(
                    alumno.getTutorCentro().getNombre() + " " +
                    alumno.getTutorCentro().getApellidos());
        }

        //Asignación activa: empresa y tutor empresa
        asignacionService.findAsignacionActiva(alumno).ifPresent(a -> {
            dto.setNombreEmpresa(a.getEmpresa().getNombre());
            dto.setNombreTutorEmpresa(
                    a.getTutorEmpresa().getNombre() + " " +
                    a.getTutorEmpresa().getApellidos());
            dto.setFechaInicio(a.getFechaInicio());
            dto.setFechaFin(a.getFechaFin());
        });

        //Resumen de tareas
        dto.setTareasValidadas(tareaService.contarValidadas(alumno));
        dto.setTareasPendientes(tareaService.contarPendientes(alumno));
        dto.setTareasRechazadas(tareaService.contarRechazadas(alumno));

        //Resumen de faltas
        dto.setFaltasJustificadas(
                faltaAsistenciaService.contarJustificadas(alumno));
        dto.setFaltasInjustificadas(
                faltaAsistenciaService.contarInjustificadas(alumno));

        //Valoraciones finales
        valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_CENTRO)
                .ifPresent(v -> {
                    dto.setResultadoTutorCentro(v.getResultado().name());
                    dto.setObservacionesTutorCentro(v.getObservaciones());
                });

        valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_EMPRESA)
                .ifPresent(v -> {
                    dto.setResultadoTutorEmpresa(v.getResultado().name());
                    dto.setObservacionesTutorEmpresa(v.getObservaciones());
                });

        return dto;
    }
}
