package com.platform.framework.config;

import com.platform.common.constant.RabbitMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * RabbitMQ 配置类
 *
 * <p>声明交换机、队列、绑定关系，并配置 {@link Jackson2JsonMessageConverter} 进行消息序列化。</p>
 *
 * @author platform
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // ==================== 消息转换器 ====================

    /**
     * Jackson JSON 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 自定义 RabbitTemplate
     *
     * <p>设置 JSON 转换器，开启 mandatory 回调与确认回调。</p>
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setMandatory(true);
        // 消息确认回调（配合 publisher-confirm-type: correlated）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("[MQ消息确认] 成功, correlationId={}",
                        Objects.nonNull(correlationData) ? correlationData.getId() : null);
            } else {
                log.error("[MQ消息确认] 失败, correlationId={}, cause={}",
                        Objects.nonNull(correlationData) ? correlationData.getId() : null, cause);
            }
        });
        // 消息退回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("[MQ消息退回] exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText());
        });
        return rabbitTemplate;
    }

    /**
     * 自定义监听容器工厂
     *
     * <p>配置手动签收（ACK）模式，确保数据不丢失。</p>
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 手动签收模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        // 限流：每次只处理一条未确认消息
        factory.setPrefetchCount(1);
        return factory;
    }

    // ==================== 用户模块：Direct 交换机 ====================

    /**
     * 用户交换机（持久化、非自动删除）
     */
    @Bean
    public DirectExchange userExchange() {
        return ExchangeBuilder.directExchange(RabbitMqConstant.USER_EXCHANGE)
                .durable(true)
                .autoDelete()
                .build();
    }

    /**
     * 用户队列（持久化）
     */
    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(RabbitMqConstant.USER_QUEUE).build();
    }

    /**
     * 用户队列绑定到用户交换机
     */
    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue())
                .to(userExchange())
                .with(RabbitMqConstant.USER_ROUTING_KEY);
    }

    // ==================== 死信队列 ====================

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(RabbitMqConstant.DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstant.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMqConstant.DEAD_LETTER_ROUTING_KEY);
    }
}
