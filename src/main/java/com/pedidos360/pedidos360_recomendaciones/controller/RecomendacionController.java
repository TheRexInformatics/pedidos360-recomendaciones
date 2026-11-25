package com.pedidos360.pedidos360_recomendaciones.controller;

import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/recomendaciones")
public class RecomendacionController {

    @Autowired
    private RecomendacionService recomendacionService;

    @GetMapping
    public Map<String, Integer> obtenerRecomendados() {
        return recomendacionService.obtenerRecomendadosOrdenados();
    }

    @GetMapping("/ranking")
    public List> obtenerRankingDetallado() {
        return recomendacionService.obtenerRankingDetallado();
    }

    @PostMapping("/interaccion")
    public Map<String, Object> registrarInteraccion(@RequestBody Map<String, Object> body) {
        String usuario = String.valueOf(body.getOrDefault("usuario", "anonimo"));
        String producto = String.valueOf(body.getOrDefault("producto", "")).trim();
        String accion = String.valueOf(body.getOrDefault("accion", "VER")).toUpperCase();

        int peso = recomendacionService.registrarInteraccion(usuario, producto, accion, null);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("mensaje", "Interacción procesada en Recomendaciones");
        resp.put("producto", producto);
        resp.put("accion", accion);
        resp.put("peso", peso);
        resp.put("recomendados", recomendacionService.obtenerRecomendadosOrdenados());
        return resp;
    }
}
