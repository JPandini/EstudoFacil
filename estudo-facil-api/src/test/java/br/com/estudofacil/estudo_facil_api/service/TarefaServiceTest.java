package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.TarefaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResponseDTO;
import br.com.estudofacil.estudo_facil_api.enums.TaskStatus;
import br.com.estudofacil.estudo_facil_api.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarefaServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private MateriaService materiaService;

    @Test
    void criarTarefa_dataNoPassado_deveLancarBusinessException() {
        String email = "tarefadata@test.com";
        registrarEObterToken("Data User", email, "senha123");

        MateriaResponseDTO materia = materiaService.criarMateria(email,
                new MateriaRequestDTO("História", null, "2026-1", null));

        TarefaRequestDTO dto = new TarefaRequestDTO(
                "Tarefa atrasada", null, LocalDate.now().minusDays(1),
                TaskStatus.PENDENTE, materia.id());

        assertThatThrownBy(() -> tarefaService.criarTarefa(email, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passado");
    }
}
