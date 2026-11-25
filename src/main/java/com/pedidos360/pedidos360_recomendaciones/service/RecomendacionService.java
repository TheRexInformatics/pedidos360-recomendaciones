package com.pedidos360.pedidos360_recomendaciones.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {

    private final Map<String, Integer> puntajesProductos = new ConcurrentHashMap<>();
    private final List> historial = new CopyOnWriteArrayList<>();

    public int calcularPeso(String accion) {
        if (accion == null) return 1;
        return switch (accion.trim().toUpperCase()) {
            case "COMPRAR" -> 3;
            case "GUARDAR" -> 2;
            default -> 1; // VER
        };
    }

    public int registrarInteraccion(String usuario, String producto, String accion, Integer pesoExplicito) {
        if (producto == null || producto.trim().isEmpty()) {
            return 0;
        }
        String prodLimpio = producto.trim();
        String accionNorm = (accion == null || accion.isBlank()) ? "VER" : accion.trim().toUpperCase();
        int peso = (pesoExplicito != null && pesoExplicito > 0) ? pesoExplicito : calcularPeso(accionNorm);

        puntajesProductos.merge(prodLimpio, peso, Integer::sum);

        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("usuario", usuario != null ? usuario : "anonimo");
        evento.put("producto", prodLimpio);
        evento.put("accion", accionNorm);
        evento.put("peso", peso);
        evento.put("timestamp", Instant.now().toString());
        historial.add(evento);

        return peso;
    }

    public Map<String, Integer> obtenerRecomendadosOrdenados() {
        return puntajesProductos.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public List> obtenerRankingDetallado() {
        List> ranking = new ArrayList<>();
        int pos = 1;
        for (Map.Entry<String, Integer> entry : obtenerRecomendadosOrdenados().entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("posicion", pos++);
            item.put("producto", entry.getKey());
            item.put("puntaje", entry.getValue());
            ranking.add(item);
        }
        return ranking;
    }

    public List> obtenerHistorial() {
        return historial;
    }
}
