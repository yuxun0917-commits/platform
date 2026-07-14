package com.platform.service.mq;

import com.platform.common.constant.RabbitMqConstant;
import com.platform.common.vo.UserMessageVO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户消息消费者
 *
 * <p>监听 user.queue，手动签收（basic.ack）模式，确保数据不丢失。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserConsumer {

    /**
     * 消费用户消息
     *
     * <p>手动签收：业务处理成功后调用 basic.ack；处理失败时调用 basic.nack，
     * 不重新入队（避免无限循环），可结合死信队列处理。</p>
     *
     * @param message    消息体（UserMessageVO）
     * @param rawMessage 原始消息（用于获取 deliveryTag）
     * @param channel    RabbitMQ 通道
     * @throws IOException 签收异常
     */
    @RabbitListener(queues = RabbitMqConstant.USER_QUEUE, ackMode = "MANUAL")
    public void consume(UserMessageVO message, Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("[MQ消费者] 收到用户消息, message={}", message);

            // TODO 此处可扩展：发送欢迎邮件、同步用户缓存、推送通知等
            log.info("[MQ消费者] 处理用户消息完成, userId={}, username={}, operation={}",
                    message.getId(), message.getUsername(), message.getOperation());

            // 手动签收（确认消费）
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[MQ消费者] 处理用户消息失败, message={}", message, e);
            // 拒绝消息，不重新入队（false 表示丢弃，可结合死信队列处理）
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
