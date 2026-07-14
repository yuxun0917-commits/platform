package com.platform.common.constant;

/**
 * RabbitMQ 常量池
 *
 * <p>集中管理 RabbitMQ 的交换机、队列、路由键名称，避免硬编码。</p>
 *
 * @author platform
 */
public final class RabbitMqConstant {

    private RabbitMqConstant() {
    }

    // ==================== 用户模块 ====================

    /** 用户交换机 */
    public static final String USER_EXCHANGE = "user.exchange";

    /** 用户队列 */
    public static final String USER_QUEUE = "user.queue";

    /** 用户路由键 */
    public static final String USER_ROUTING_KEY = "user.routing.key";

    // ==================== 死信队列 ====================

    /** 死信交换机 */
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    /** 死信队列 */
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    /** 死信路由键 */
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.routing.key";
}
