package com.pedidos360.pedidos360_recomendaciones.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {

    private static class ContadorCompras extends ConcurrentHashMap<String, Integer> {}

    private final Map<String, ContadorCompras> historialPorUsuario = new ConcurrentHashMap<>();

    private record ReglaAfinidad(String productoOrigen, String productoRecomendado, int peso, String motivo) {}

    private static final List<ReglaAfinidad> REGLAS_AFINIDAD = List.of(
        new ReglaAfinidad("Hamburguesa Triple", "Papas Supremas", 10, "Acompañamiento clásico para tu Hamburguesa Triple"),
        new ReglaAfinidad("Hamburguesa Triple", "Bebida Grande", 8, "Ideal para refrescar tu combo de hamburguesa"),
        new ReglaAfinidad("Pizza Familiar", "Bebida Grande", 10, "Imprescindible para compartir con tu Pizza Familiar"),
        new ReglaAfinidad("Pizza Familiar", "Pastel de Chocolate", 7, "El toque dulce perfecto después de la pizza"),
        new ReglaAfinidad("Papas Supremas", "Hamburguesa Triple", 9, "Combina tus Papas Supremas con nuestro plato estrella"),
        new ReglaAfinidad("Papas Supremas", "Bebida Grande", 7, "Completa tu pedido con una bebida helada"),
        new ReglaAfinidad("Bebida Grande", "Hamburguesa Triple", 8, "Los clientes que piden bebida suelen elegirla"),
        new ReglaAfinidad("Bebida Grande", "Pizza Familiar", 8, "Excelente opción para acompañar tu bebida"),
        new ReglaAfinidad("Pastel de Chocolate", "Bebida Grande", 9, "Para acompañar tu postre"),
        new ReglaAfinidad("Pastel de Chocolate", "Pizza Familiar", 6, "Sugerencia destacada según tus gustos")
    );

    public void registrarCompra(String usuario, List<String> productos) {
        if (usuario == null || usuario.isBlank() || productos == null || productos.isEmpty()) {
            return;
        }
        ContadorCompras comprasUsuario = historialPorUsuario.computeIfAbsent(usuario, k -> new ContadorCompras());
        for (String producto : productos) {
            comprasUsuario.merge(producto, 1, Integer::sum);
        }
    }

    public Map<String, Object> generarRecomendaciones(
            String usuario,
            List<String> historialExterno,
            List<String> carritoActual
    ) {
        Map<String, Integer> frecuencias = new HashMap<>();
        ContadorCompras guardadas = historialPorUsuario.get(usuario);
        if (guardadas != null) {
            frecuencias.putAll(guardadas);
        }

        if (historialExterno != null) {
            for (String prod : historialExterno) {
                frecuencias.merge(prod, 1, Integer::sum);
            }
        }

        Set<String> excluidos = carritoActual != null ? new HashSet<>(carritoActual) : Collections.emptySet();
        Map<String, Integer> puntajeProducto = new HashMap<>();
        Map<String, String> motivoProducto = new HashMap<>();

        for (Map.Entry<String, Integer> compra : frecuencias.entrySet()) {
            String productoComprado = compra.getKey();
            int vecesComprado = compra.getValue();

            for (ReglaAfinidad regla : REGLAS_AFINIDAD) {
                if (!regla.productoOrigen().equals(productoComprado)) {
                    continue;
                }
                if (excluidos.contains(regla.productoRecomendado())) {
                    continue;
                }
                int puntos = regla.peso() * vecesComprado;
                puntajeProducto.merge(regla.productoRecomendado(), puntos, Integer::sum);
                motivoProducto.putIfAbsent(regla.productoRecomendado(), regla.motivo());
            }
        }

        boolean basadoEnHistorial = !puntajeProducto.isEmpty();
        if (!basadoEnHistorial) {
            List<String> populares = List.of("Hamburguesa Triple", "Papas Supremas", "Bebida Grande");
            int pesoBase = 10;
            for (String pop : populares) {
                if (!excluidos.contains(pop)) {
                    puntajeProducto.put(pop, pesoBase--);
                    motivoProducto.put(pop, "Más pedido en Pedidos360");
                }
            }
        }

        List<Object> recomendaciones = puntajeProducto.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(3)
            .map(entry -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("producto", entry.getKey());
                item.put("afinidadScore", entry.getValue());
                item.put("motivo", motivoProducto.get(entry.getKey()));
                return (Object) item;
            })
            .collect(Collectors.toList());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario);
        respuesta.put("basadoEnHistorial", basadoEnHistorial);
        respuesta.put("totalComprasAnalizadas", frecuencias.values().stream().mapToInt(Integer::intValue).sum());
        respuesta.put("recomendaciones", recomendaciones);
        return respuesta;
    }
}