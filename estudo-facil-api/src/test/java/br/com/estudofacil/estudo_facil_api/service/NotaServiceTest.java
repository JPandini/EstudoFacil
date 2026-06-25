package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.NotaResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class NotaServiceTest extends AbstractIntegrationTest {

    @Autowired
    private NotaService notaService;

    @Autowired
    private MateriaService materiaService;

    @Test
    void listarNotas_semMateriaId_deveRetornarApenasNotasDoUsuario() {
        String emailA = "notausera@test.com";
        String emailB = "notauserb@test.com";
        registrarEObterToken("Nota User A", emailA, "senha123");
        registrarEObterToken("Nota User B", emailB, "senha123");

        MateriaResponseDTO materiaA = materiaService.criarMateria(emailA,
                new MateriaRequestDTO("Mat A", null, "2026-1", null));
        MateriaResponseDTO materiaB = materiaService.criarMateria(emailB,
                new MateriaRequestDTO("Mat B", null, "2026-1", null));

        NotaResponseDTO notaA = notaService.criarNota(emailA,
                new NotaRequestDTO(8.0, 2.0, "Prova A", materiaA.id()));
        notaService.criarNota(emailB,
                new NotaRequestDTO(9.0, 3.0, "Prova B", materiaB.id()));

        Page<NotaResponseDTO> notasUserA = notaService.listarNotas(emailA, null, PageRequest.of(0, 20));

        assertThat(notasUserA.getContent()).hasSize(1);
        assertThat(notasUserA.getContent().get(0).id()).isEqualTo(notaA.id());
    }
}
