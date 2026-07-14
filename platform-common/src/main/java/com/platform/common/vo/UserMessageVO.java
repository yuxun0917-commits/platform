package com.platform.common.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户消息 VO（RabbitMQ 消息体）
 *
 * <p>作为 RabbitMQ 消息体对象，放在 common 模块以便生产者（service）与消费者（service）共享，
 * 同时避免 service 与 api 模块之间产生循环依赖。使用 Jackson2JsonMessageConverter 序列化为 JSON 传输。</p>
 *
 * @author platform
 */
@Data
public class UserMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 操作类型（SAVE/UPDATE/DELETE） */
    private String operation;

    /** 时间戳（消息发送时间） */
    private Long timestamp;
}
