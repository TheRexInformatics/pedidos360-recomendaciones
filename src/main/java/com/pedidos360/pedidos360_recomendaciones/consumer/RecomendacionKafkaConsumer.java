package com.pedidos360.pedidos360_recomendaciones.consumer;

import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecomendacionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionKafkaConsumer.class);
    private static final Pattern PATRON_USUARIO = Pattern.compile("\"(?:cliente|usuario)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PATRON_NOMBRE = Pattern.compile("\"nombre\"\\s*:\\s*\"([^\"]+)\"");

    private final RecomendacionService recomendacionService;

    public RecomendacionKafkaConsumer(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @KafkaListener(topics = {"pedidos-topic", "pagos-topic"}, groupId = "recomendaciones-group")
    public void consumirEventoCompra(String mensaje) {
        try {
            String usuario = "anonimo@pedidos360.cl";
            Matcher matcherUsuario = PATRON_USUARIO.matcher(mensaje);
            if (matcherUsuario.find()) {
                usuario = matcherUsuario.group(1);
            }

            List<String> productos = new ArrayList<>();
            Matcher matcherNombre = PATRON_NOMBRE.matcher(mensaje);
            while (matcherNombre.find()) {
                productos.add(matcherNombre.group(1));
            }

            if (!productos.isEmpty()) {
                recomendacionService.registrarCompra(usuario, productos);
                log.info("Historial actualizado para {} con {} productos", usuario, productos.size());
            }
        } catch (Exception e) {
            log.warn("No se pudo procesar mensaje Kafka en recomendaciones: {}", e.getMessage());
        }
    }
}