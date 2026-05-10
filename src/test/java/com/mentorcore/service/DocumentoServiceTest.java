package com.mentorcore.service;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.TipoDocumento;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import com.mentorcore.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private DocumentoService documentoService;

    private Alumno alumno;

    @BeforeEach
    void setUp() {
        alumno = new Alumno();
        alumno.setId(1L);
    }

    @Test
    void findPersonalesByAlumno_clasificaPorTipoTambienConContextoAntiguo() {
        Documento dni = documento("dni.pdf", "DNI / NIE del Alumno", ContextoDocumentoEnum.EXPEDIENTE);
        Documento seguro = documento("seguro.pdf", "Seguro Escolar", ContextoDocumentoEnum.PERSONAL_ALUMNO);
        Documento plan = documento("plan.pdf", "Anexo I - Plan de Formación", ContextoDocumentoEnum.EXPEDIENTE);

        when(documentoRepository.findByAlumnoOrderByFechaSubidaDesc(alumno))
                .thenReturn(List.of(dni, seguro, plan));

        List<Documento> resultado = documentoService.findPersonalesByAlumno(alumno);

        assertThat(resultado).containsExactly(dni, seguro);
    }

    @Test
    void findFormacionEmpresaByAlumno_clasificaPlanEInforme() {
        Documento informe = documento("informe.pdf", "Informe de Seguimiento Empresa",
                ContextoDocumentoEnum.EXPEDIENTE);
        Documento plan = documento("plan.pdf", "Anexo I - Plan de Formación",
                ContextoDocumentoEnum.FORMACION_EMPRESA);
        Documento dni = documento("dni.pdf", "DNI / NIE del Alumno",
                ContextoDocumentoEnum.PERSONAL_ALUMNO);

        when(documentoRepository.findByAlumnoOrderByFechaSubidaDesc(alumno))
                .thenReturn(List.of(informe, plan, dni));

        List<Documento> resultado = documentoService.findFormacionEmpresaByAlumno(alumno);

        assertThat(resultado).containsExactly(informe, plan);
    }

    @Test
    void validar_documentoPendienteLoMarcaComoValidado() {
        Documento documento = documento("dni.pdf", "DNI / NIE del Alumno", ContextoDocumentoEnum.PERSONAL_ALUMNO);
        documento.setId(99L);
        documento.setEstado(EstadoDocumentoEnum.PENDIENTE);

        when(documentoRepository.findDetalleById(99L)).thenReturn(Optional.of(documento));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        documentoService.validar(99L, "Correcto");

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoDocumentoEnum.VALIDADO);
        assertThat(captor.getValue().getComentarioRevision()).isEqualTo("Correcto");
    }

    @Test
    void eliminar_documentoValidadoLanzaExcepcion() {
        Documento documento = documento("dni.pdf", "DNI / NIE del Alumno", ContextoDocumentoEnum.PERSONAL_ALUMNO);
        documento.setId(77L);
        documento.setEstado(EstadoDocumentoEnum.VALIDADO);

        when(documentoRepository.findDetalleById(77L)).thenReturn(Optional.of(documento));

        assertThrows(IllegalStateException.class, () -> documentoService.eliminar(77L));
    }

    private Documento documento(String nombreArchivo, String tipoNombre, ContextoDocumentoEnum contexto) {
        Documento documento = new Documento();
        documento.setAlumno(alumno);
        documento.setNombreArchivo(nombreArchivo);
        documento.setRutaAlmacenamiento("/tmp/" + nombreArchivo);
        documento.setContexto(contexto);
        documento.setEstado(EstadoDocumentoEnum.PENDIENTE);

        TipoDocumento tipo = new TipoDocumento();
        tipo.setNombre(tipoNombre);
        documento.setTipoDocumento(tipo);
        return documento;
    }
}
