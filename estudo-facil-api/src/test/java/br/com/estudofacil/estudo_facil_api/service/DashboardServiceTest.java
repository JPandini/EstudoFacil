package br.com.estudofacil.estudo_facil_api.service;

import br.com.estudofacil.estudo_facil_api.AbstractIntegrationTest;
import br.com.estudofacil.estudo_facil_api.dto.request.MateriaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.request.NotaRequestDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.DashboardResponseDTO;
import br.com.estudofacil.estudo_facil_api.dto.response.MateriaResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest extends AbstractIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MateriaService materiaService;

    @Autowired
    private NotaService notaService;

    @Test
    void buscarDashboard_deveCalcularMediaGeral() {
        String email = "dashboard@test.com";
        registrarEObterToken("Dashboard User", email, "senha123");

        MateriaResponseDTO materia = materiaService.criarMateria(email,
                new MateriaRequestDTO("Química", null, "2026-1", null));

        notaService.criarNota(email, new NotaRequestDTO(8.0, 2.0, null, materia.id()));
        notaService.criarNota(email, new NotaRequestDTO(6.0, 2.0, null, materia.id()));

        DashboardResponseDTO dashboard = dashboardService.buscarDashboard(email);

        assertThat(dashboard.totalMaterias()).isEqualTo(1);
        assertThat(dashboard.mediaGeral()).isEqualTo(7.0);
        assertThat(dashboard.materiasComMedia()).hasSize(1);
        assertThat(dashboard.materiasComMedia().get(0).media()).isEqualTo(7.0);
        assertThat(dashboard.proximasTarefas()).isNotNull();
    }
}
