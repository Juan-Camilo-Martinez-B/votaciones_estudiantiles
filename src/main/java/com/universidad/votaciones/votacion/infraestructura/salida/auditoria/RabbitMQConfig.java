package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de RabbitMQ para el módulo de auditoría.
 *
 * <p>Define la topología AMQP completa:
 * <ul>
 *   <li>Un {@link DirectExchange} durable para enrutamiento directo.</li>
 *   <li>Una {@link Queue} durable con su Dead-Letter Queue (DLQ) asociada,
 *       garantizando que ningún mensaje se pierda ante fallos de procesamiento.</li>
 *   <li>Un {@link Binding} que conecta el exchange con la cola mediante una routing key.</li>
 *   <li>Un {@link MessageConverter} Jackson para serialización/deserialización JSON automática.</li>
 * </ul>
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li>SRP: cada bean tiene una única responsabilidad.</li>
 *   <li>OCP: extensible sin modificar esta clase.</li>
 *   <li>DIP: inyección por interfaz ({@link ConnectionFactory}).</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    // ─────────────────────────────────────────────────────────────
    //  Constantes de topología AMQP
    // ─────────────────────────────────────────────────────────────

    /** Nombre del exchange principal de auditoría (DirectExchange). */
    public static final String AUDIT_EXCHANGE = "auditoria.exchange";

    /** Nombre de la cola principal donde llegan los eventos de auditoría. */
    public static final String AUDIT_QUEUE = "auditoria.cola";

    /** Routing key que enlaza el exchange con la cola de auditoría. */
    public static final String AUDIT_ROUTING_KEY = "auditoria.evento";

    /** Cola de mensajes fallidos (Dead-Letter Queue). */
    public static final String AUDIT_DLQ = "auditoria.cola.dlq";

    /** Exchange de dead-letter para redirigir mensajes rechazados o expirados. */
    public static final String AUDIT_DLX = "auditoria.exchange.dlx";

    // ─────────────────────────────────────────────────────────────
    //  Exchange principal
    // ─────────────────────────────────────────────────────────────

    /**
     * Exchange de tipo Direct, durable (sobrevive reinicios del broker).
     * El enrutamiento se realiza por coincidencia exacta de routing key.
     *
     * @return instancia de {@link DirectExchange}
     */
    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange(AUDIT_EXCHANGE, true, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  Dead-Letter Exchange (DLX) y Dead-Letter Queue (DLQ)
    // ─────────────────────────────────────────────────────────────

    /**
     * Exchange de tipo Direct para mensajes fallidos (Dead-Letter Exchange).
     *
     * @return instancia de {@link DirectExchange} para DLX
     */
    @Bean
    public DirectExchange auditDeadLetterExchange() {
        return new DirectExchange(AUDIT_DLX, true, false);
    }

    /**
     * Cola de mensajes muertos (Dead-Letter Queue).
     * Recibe los mensajes rechazados o expirados de la cola principal.
     *
     * @return instancia de {@link Queue} para DLQ
     */
    @Bean
    public Queue auditDeadLetterQueue() {
        return QueueBuilder.durable(AUDIT_DLQ).build();
    }

    /**
     * Binding que conecta la DLQ con el Dead-Letter Exchange.
     *
     * @param auditDeadLetterQueue    cola DLQ
     * @param auditDeadLetterExchange exchange DLX
     * @return instancia de {@link Binding}
     */
    @Bean
    public Binding deadLetterBinding(Queue auditDeadLetterQueue,
                                     DirectExchange auditDeadLetterExchange) {
        return BindingBuilder
                .bind(auditDeadLetterQueue)
                .to(auditDeadLetterExchange)
                .with(AUDIT_ROUTING_KEY);
    }

    // ─────────────────────────────────────────────────────────────
    //  Cola principal de auditoría
    // ─────────────────────────────────────────────────────────────

    /**
     * Cola principal de auditoría.
     * <ul>
     *   <li><b>Durable</b>: los mensajes sobreviven reinicios del broker.</li>
     *   <li><b>x-dead-letter-exchange</b>: redirige mensajes fallidos al DLX.</li>
     *   <li><b>x-message-ttl</b>: 7 días (604,800,000 ms) evitan acumulación indefinida.</li>
     * </ul>
     *
     * @return instancia de {@link Queue}
     */
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", AUDIT_DLX)
                .withArgument("x-dead-letter-routing-key", AUDIT_ROUTING_KEY)
                .withArgument("x-message-ttl", 604_800_000L)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  Binding: Exchange → Cola principal
    // ─────────────────────────────────────────────────────────────

    /**
     * Binding que conecta el exchange principal con la cola de auditoría.
     *
     * @param auditQueue    cola de destino
     * @param auditExchange exchange de origen
     * @return instancia de {@link Binding}
     */
    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange auditExchange) {
        return BindingBuilder
                .bind(auditQueue)
                .to(auditExchange)
                .with(AUDIT_ROUTING_KEY);
    }

    // ─────────────────────────────────────────────────────────────
    //  Conversión JSON
    // ─────────────────────────────────────────────────────────────

    /**
     * Conversor de mensajes basado en Jackson2.
     * Permite que {@link RabbitTemplate} serialice/deserialice objetos Java
     * como JSON de forma automática y transparente.
     *
     * @return instancia de {@link MessageConverter}
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura el {@link RabbitTemplate} con el conversor JSON.
     * Spring Boot autoconfigura la {@link ConnectionFactory}; aquí solo
     * sobreescribimos el MessageConverter para serialización JSON.
     *
     * @param connectionFactory fábrica de conexiones gestionada por Spring Boot
     * @return instancia de {@link RabbitTemplate} configurada
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
