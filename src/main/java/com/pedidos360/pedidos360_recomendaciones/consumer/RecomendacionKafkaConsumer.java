package com.pedidos360.pedidos360_recomendaciones.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RecomendacionKafkaConsumer {

    @Autowired
    private RecomendacionService recomendacionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "interacciones-productos", groupId = "recomendaciones-group")
    public void consumirInteraccion(String mensaje) {
        try {
            JsonNode root = objectMapper.readTree(mensaje);
            String usuario = root.path("usuario").asText("anonimo");
            String producto = root.path("producto").asText("");
            String accion = root.path("accion").asText("VER");
            Integer peso = root.has("peso") ? root.path("peso").asInt() : null;

            int pesoAplicado = recomendacionService.registrarInteraccion(usuario, producto, accion, peso);
            System.out.println(">>> [KAFKA RECOMENDACIONES] Producto: " + producto
                    + " | Acción: " + accion + " | +" + pesoAplicado + " pts");
        } catch (Exception e) {
            System.err.println(">>> [KAFKA RECOMENDACIONES] Mensaje no JSON recibido: " + mensaje);
        }
    }
}
