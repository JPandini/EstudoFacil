package br.com.estudofacil.estudo_facil_api.controller;

import br.com.estudofacil.estudo_facil_api.dto.response.DashboardResponseDTO;
import br.com.estudofacil.estudo_facil_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/painel")
@RequiredArgsConstructor
public class PainelController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> painel(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(dashboardService.buscarDashboard(userDetails.getUsername()));
    }
}
