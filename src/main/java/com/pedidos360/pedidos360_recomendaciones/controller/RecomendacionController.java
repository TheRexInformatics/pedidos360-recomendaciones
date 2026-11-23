package com.pedidos360.pedidos360_recomendaciones.controller;

import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/recomendaciones")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/mis-sugerencias")
    public ResponseEntity<Object> obtenerMisRecomendaciones(@AuthenticationPrincipal Jwt jwt) {
        String usuario = jwt != null && jwt.getClaimAsString("preferred_username") != null
            ? jwt.getClaimAsString("preferred_username")
            : "usuario@pedidos360.cl";

        return ResponseEntity.ok(
            recomendacionService.generarRecomendaciones(usuario, Collections.emptyList(), Collections.emptyList())
        );
    }

    @PostMapping("/sugerir")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> sugerirPorCompras(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> payload
    ) {
        String usuario = String.valueOf(payload.getOrDefault("usuario",
            jwt != null ? jwt.getClaimAsString("preferred_username") : "usuario@pedidos360.cl"));

        List<String> historial = (List<String>) payload.getOrDefault("historialCompras", Collections.emptyList());
        List<String> carrito = (List<String>) payload.getOrDefault("carritoActual", Collections.emptyList());

        return ResponseEntity.ok(
            recomendacionService.generarRecomendaciones(usuario, historial, carrito)
        );
    }

    @PostMapping("/registrar-compra")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> registrarCompra(@RequestBody Map<String, Object> payload) {
        String usuario = String.valueOf(payload.getOrDefault("usuario", "usuario@pedidos360.cl"));
        List<String> productos = (List<String>) payload.getOrDefault("productos", Collections.emptyList());

        recomendacionService.registrarCompra(usuario, productos);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Compra registrada en motor de recomendaciones",
            "usuario", usuario,
            "productosRegistrados", productos.size()
        ));
    }
}