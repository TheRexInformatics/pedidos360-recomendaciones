package com.pedidos360.pedidos360_recomendaciones.consumer;

import com.pedidos360.pedidos360_recomendaciones.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecomendacionKafkaConsumer {

    @Autowired
    private RecomendacionService recomendacionService;

    private String extraerCampoTexto(String json, String campo, String valorPorDefecto) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : valorPorDefecto;
    }

    private Integer extraerCampoEntero(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    @KafkaListener(topics = "interacciones-productos", groupId = "recomendaciones-group")
    public void consumirInteraccion(String mensaje) {
        try {
            String usuario = extraerCampoTexto(mensaje, "usuario", "anonimo");
            String producto = extraerCampoTexto(mensaje, "producto", "");
            String accion = extraerCampoTexto(mensaje, "accion", "VER");
            Integer peso = extraerCampoEntero(mensaje, "peso");

            if (producto.isEmpty()) {
                System.err.println("[KAFKA RECOMENDACIONES] Mensaje sin producto recibido: " + mensaje);
                return;
            }

            int pesoAplicado = recomendacionService.registrarInteraccion(usuario, producto, accion, peso);
            System.out.println("[KAFKA RECOMENDACIONES] Producto: " + producto
                    + " | Accion: " + accion + " | +" + pesoAplicado + " pts");
        } catch (Exception e) {
            System.err.println("[KAFKA RECOMENDACIONES] Error procesando mensaje: " + mensaje);
        }
    }
}
