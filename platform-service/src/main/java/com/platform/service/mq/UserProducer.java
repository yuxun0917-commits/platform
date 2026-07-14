package com.platform.service.mq;

import com.platform.common.constant.RabbitMqConstant;
import com.platform.common.vo.UserMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 用户消息生产者
 *
 * <p>注入 {@link RabbitTemplate} 进行消息发送，使用 correlationId 进行链路追踪。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送用户消息到 user.queue
     *
     * <p>消息体为 {@link UserMessageVO}，由 Jackson2JsonMessageConverter 序列化为 JSON。
     * 使用 UUID 作为 correlationId 便于全链路追踪。</p>
     *
     * @param user      用户实体
     * @param operation 操作类型（SAVE/UPDATE/DELETE）
     */
//    public void sendUserMessage(User user, String operation) {
//        // 构造消息体（显式字段映射，避免 BeanUtils 批量拷贝）
//        UserMessageVO message = new UserMessageVO();
//        message.setId(user.getId());
//        message.setUsername(user.getUsername());
//        message.setEmail(user.getEmail());
//        message.setOperation(operation);
//        message.setTimestamp(System.currentTimeMillis());
//
//        // 生成 correlationId 用于链路追踪
//        String correlationId = UUID.randomUUID().toString();
//        CorrelationData data = new CorrelationData(correlationId);
//
//        log.info("[MQ生产者] 发送用户消息, correlationId={}, exchange={}, routingKey={}, message={}",
//                correlationId, RabbitMqConstant.USER_EXCHANGE, RabbitMqConstant.USER_ROUTING_KEY, message);
//
//        rabbitTemplate.convertAndSend(
//                RabbitMqConstant.USER_EXCHANGE,
//                RabbitMqConstant.USER_ROUTING_KEY,
//                message,
//                data);
//    }
}
