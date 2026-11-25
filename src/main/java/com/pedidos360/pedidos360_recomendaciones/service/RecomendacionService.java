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

    public static class RankingItem {
        public int posicion;
        public String producto;
        public int puntaje;

        public RankingItem(int posicion, String producto, int puntaje) {
            this.posicion = posicion;
            this.producto = producto;
            this.puntaje = puntaje;
        }
    }

    public static class EventoInteraccion {
        public String usuario;
        public String producto;
        public String accion;
        public int peso;
        public String timestamp;

        public EventoInteraccion(String usuario, String producto, String accion, int peso, String timestamp) {
            this.usuario = usuario;
            this.producto = producto;
            this.accion = accion;
            this.peso = peso;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, Integer> puntajesProductos = new ConcurrentHashMap<>();
    private final List<EventoInteraccion> historial = new CopyOnWriteArrayList<>();

    public int calcularPeso(String accion) {
        if (accion == null) return 1;
        return switch (accion.trim().toUpperCase()) {
            case "COMPRAR" -> 3;
            case "GUARDAR" -> 2;
            default -> 1;
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

        historial.add(new EventoInteraccion(
                usuario != null ? usuario : "anonimo",
                prodLimpio,
                accionNorm,
                peso,
                Instant.now().toString()
        ));

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

    public List<RankingItem> obtenerRankingDetallado() {
        List<RankingItem> ranking = new ArrayList<>();
        int pos = 1;
        for (Map.Entry<String, Integer> entry : obtenerRecomendadosOrdenados().entrySet()) {
            ranking.add(new RankingItem(pos++, entry.getKey(), entry.getValue()));
        }
        return ranking;
    }

    public List<EventoInteraccion> obtenerHistorial() {
        return historial;
    }
}
