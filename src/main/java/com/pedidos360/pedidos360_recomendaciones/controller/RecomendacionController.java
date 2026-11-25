package com.pedidos360.pedidos360_recomendaciones.controller;

import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/recomendaciones")
public class RecomendacionController {

    public static class InteraccionRequest {
        public String usuario;
        public String producto;
        public String accion;
    }

    public static class InteraccionResponse {
        public String mensaje;
        public String producto;
        public String accion;
        public int peso;
        public Map recomendados;

        public InteraccionResponse(String mensaje, String producto, String accion, int peso, Map recomendados) {
            this.mensaje = mensaje;
            this.producto = producto;
            this.accion = accion;
            this.peso = peso;
            this.recomendados = recomendados;
        }
    }

    @Autowired
    private RecomendacionService recomendacionService;

    @GetMapping
    public Map obtenerRecomendados() {
        return recomendacionService.obtenerRecomendadosOrdenados();
    }

    @GetMapping("/ranking")
    public List obtenerRankingDetallado() {
        return recomendacionService.obtenerRankingDetallado();
    }

    @PostMapping("/interaccion")
    public InteraccionResponse registrarInteraccion(@RequestBody InteraccionRequest body) {
        String usuario = (body != null && body.usuario != null) ? body.usuario : "anonimo";
        String producto = (body != null && body.producto != null) ? body.producto.trim() : "";
        String accion = (body != null && body.accion != null) ? body.accion.toUpperCase() : "VER";

        int peso = recomendacionService.registrarInteraccion(usuario, producto, accion, null);

        return new InteraccionResponse(
                "Interaccion procesada en Recomendaciones",
                producto,
                accion,
                peso,
                recomendacionService.obtenerRecomendadosOrdenados()
        );
    }
}
