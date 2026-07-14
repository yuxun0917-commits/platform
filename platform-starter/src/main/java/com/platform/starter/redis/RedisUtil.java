package com.platform.starter.redis;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 工具类
 *
 * <p>封装常用的 Redis 操作，包括 String、Hash、List、Set、ZSet 五大数据结构的增删改查，
 * 以及 Key 管理、过期时间控制等基础能力。</p>
 *
 * <p>本工具类基于 {@link RedisTemplate} 实现，key 统一为 String 类型，value 为 Object 类型，
 * 序列化方式由容器中的 RedisTemplate 决定（推荐 JSON 序列化）。</p>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>所有 key 会自动拼接全局前缀（{@code platform.redis.key-prefix}），默认 "platform:"</li>
 *   <li>调用 {@link #set(String, Object)} 未指定过期时间时，自动应用全局过期时间
 *       （{@code platform.redis.global-ttl} + {@code platform.redis.global-ttl-unit}），
 *       设为 0 或负数表示不过期</li>
 * </ul>
 *
 * <p>使用方式：业务模块引入 {@code project-starter} 的 Maven 坐标后，
 * 直接通过 {@code @Autowired RedisUtil redisUtil} 注入即可使用。</p>
 *
 * @author platform
 */
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties redisProperties;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    /**
     * 拼接全局 key 前缀
     *
     * @param key 原始 key
     * @return 拼接前缀后的完整 key
     */
    private String wrapKey(String key) {
        String prefix = redisProperties.getPrefix();
        if (Objects.isNull(prefix) || prefix.isEmpty()) {
            return key;
        }
        return prefix + key;
    }

    /**
     * 判断是否启用全局过期时间
     *
     * @return true 启用 / false 不过期
     */
    private boolean hasGlobalTtl() {
        return redisProperties.getTtl() > 0;
    }

    // ============================ Key 相关操作 ============================

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true 存在 / false 不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(wrapKey(key));
    }

    /**
     * 删除一个或多个 key
     *
     * @param keys 一个或多个键
     * @return 成功删除的数量
     */
    public Long delete(String... keys) {
        if (Objects.isNull(keys) || keys.length == 0) {
            return 0L;
        }
        List<String> wrappedKeys = Arrays.stream(keys).map(this::wrapKey).toList();
        return redisTemplate.delete(wrappedKeys);
    }

    /**
     * 删除集合中的 key
     *
     * @param keys 键集合
     * @return 成功删除的数量
     */
    public Long delete(Collection<String> keys) {
        if (Objects.isNull(keys) || keys.isEmpty()) {
            return 0L;
        }
        List<String> wrappedKeys = keys.stream().map(this::wrapKey).toList();
        return redisTemplate.delete(wrappedKeys);
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(wrapKey(key), timeout, unit);
    }

    /**
     * 设置过期时间（秒）
     *
     * @param key     键
     * @param seconds 过期秒数
     * @return true 设置成功
     */
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(wrapKey(key), seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取剩余过期时间（秒）
     *
     * @param key 键
     * @return 剩余过期时间（秒）；-1 表示永久有效；-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(wrapKey(key));
    }

    /**
     * 移除 key 的过期时间，使其永久有效
     *
     * @param key 键
     * @return true 操作成功
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(wrapKey(key));
    }

    /**
     * 根据模式匹配获取 key 集合（返回已去除前缀的 key）
     *
     * @param pattern 匹配模式（如 "user:*"）
     * @return 匹配到的 key 集合（已去除全局前缀）
     */
    public Set<String> keys(String pattern) {
        String prefix = redisProperties.getPrefix();
        Set<String> rawKeys = redisTemplate.keys(wrapKey(pattern));
        if (Objects.isNull(rawKeys) || rawKeys.isEmpty()) {
            return Set.of();
        }
        if (Objects.isNull(prefix) || prefix.isEmpty()) {
            return rawKeys;
        }
        return rawKeys.stream().map(k -> k.startsWith(prefix) ? k.substring(prefix.length()) : k).collect(Collectors.toSet());
    }

    /**
     * 获取 Redis 服务器 INFO 信息
     *
     * <p>返回 Redis {@code INFO} 命令的原始属性集合，常用字段包括：</p>
     * <ul>
     *   <li>{@code redis_version}：服务器版本</li>
     *   <li>{@code redis_mode}：运行模式（standalone / sentinel / cluster）</li>
     *   <li>{@code tcp_port}：监听端口</li>
     *   <li>{@code uptime_in_days}：运行时长（天）</li>
     *   <li>{@code connected_clients}：当前客户端连接数</li>
     *   <li>{@code used_memory_human} / {@code maxmemory_human}：内存占用 / 最大内存（人类可读）</li>
     *   <li>{@code total_connections_received} / {@code total_commands_processed}：累计连接数 / 累计命令数</li>
     *   <li>{@code keyspace_hits} / {@code keyspace_misses}：键命中 / 未命中次数</li>
     * </ul>
     *
     * @return Redis INFO 属性集合；连接异常时返回空 Properties
     */
    public Properties getInfo() {
        return redisTemplate.execute((RedisCallback<Properties>) connection -> connection.info());
    }

    // ============================ String 操作 ============================

    /**
     * 写入缓存（自动应用全局过期时间）
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        String wrappedKey = wrapKey(key);
        if (hasGlobalTtl()) {
            redisTemplate.opsForValue().set(wrappedKey, value,
                    redisProperties.getTtl(), redisProperties.getTtlUnit());
        } else {
            redisTemplate.opsForValue().set(wrappedKey, value);
        }
    }

    /**
     * 写入缓存并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(wrapKey(key), value, timeout, unit);
    }

    /**
     * 写入缓存并设置过期时间（秒）
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期秒数
     */
    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(wrapKey(key), value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 仅当 key 不存在时写入缓存（分布式锁常用）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 写入成功 / false key 已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(wrapKey(key), value, timeout, unit);
    }

    /**
     * 读取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(wrapKey(key));
    }

    /**
     * 读取缓存并转换为指定类型
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 转换后的值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(wrapKey(key));
        if (Objects.isNull(value)) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 自增（步长 1）
     *
     * @param key 键
     * @return 自增后的值
     */
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(wrapKey(key));
    }

    /**
     * 自增指定步长
     *
     * @param key       键
     * @param increment 步长
     * @return 自增后的值
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(wrapKey(key), increment);
    }

    /**
     * 自减（步长 1）
     *
     * @param key 键
     * @return 自减后的值
     */
    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(wrapKey(key));
    }

    /**
     * 自减指定步长
     *
     * @param key       键
     * @param decrement 步长
     * @return 自减后的值
     */
    public Long decrBy(String key, long decrement) {
        return redisTemplate.opsForValue().decrement(wrapKey(key), decrement);
    }

    // ============================ Hash 操作 ============================

    /**
     * 向 Hash 中写入一个键值对
     *
     * @param key     键
     * @param hashKey Hash 键
     * @param value   值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(wrapKey(key), hashKey, value);
    }

    /**
     * 批量写入 Hash
     *
     * @param key  键
     * @param map  Hash 数据
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(wrapKey(key), map);
    }

    /**
     * 从 Hash 中读取一个值
     *
     * @param key     键
     * @param hashKey Hash 键
     * @return 值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(wrapKey(key), hashKey);
    }

    /**
     * 从 Hash 中读取一个值并转换为指定类型
     *
     * @param key     键
     * @param hashKey Hash 键
     * @param clazz   目标类型
     * @param <T>     泛型
     * @return 转换后的值
     */
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(wrapKey(key), hashKey);
        if (Objects.isNull(value)) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 获取 Hash 中的所有键值对
     *
     * @param key 键
     * @return Hash 全部数据
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(wrapKey(key));
    }

    /**
     * 判断 Hash 中是否存在指定 hashKey
     *
     * @param key     键
     * @param hashKey Hash 键
     * @return true 存在 / false 不存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(wrapKey(key), hashKey);
    }

    /**
     * 删除 Hash 中的一个或多个 hashKey
     *
     * @param key      键
     * @param hashKeys Hash 键
     * @return 成功删除的数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(wrapKey(key), hashKeys);
    }

    /**
     * Hash 中指定 hashKey 自增
     *
     * @param key       键
     * @param hashKey   Hash 键
     * @param increment 步长
     * @return 自增后的值
     */
    public Long hIncr(String key, String hashKey, long increment) {
        return redisTemplate.opsForHash().increment(wrapKey(key), hashKey, increment);
    }

    // ============================ List 操作 ============================

    /**
     * 向列表左侧写入一个值
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(wrapKey(key), value);
    }

    /**
     * 向列表左侧批量写入
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    public Long lLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(wrapKey(key), values);
    }

    /**
     * 向列表右侧写入一个值
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(wrapKey(key), value);
    }

    /**
     * 从列表左侧弹出一个值
     *
     * @param key 键
     * @return 弹出的值
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(wrapKey(key));
    }

    /**
     * 从列表右侧弹出一个值
     *
     * @param key 键
     * @return 弹出的值
     */
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(wrapKey(key));
    }

    /**
     * 获取列表指定范围的元素（start ~ end，闭区间）
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素集合
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(wrapKey(key), start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(wrapKey(key));
    }

    /**
     * 根据索引设置列表元素的值
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(wrapKey(key), index, value);
    }

    /**
     * 从列表中移除指定数量的匹配元素
     *
     * @param key   键
     * @param count 移除数量（正数从左往右，负数从右往左，0 全部）
     * @param value 要移除的值
     * @return 实际移除的数量
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(wrapKey(key), count, value);
    }

    // ============================ Set 操作 ============================

    /**
     * 向 Set 中写入一个或多个值
     *
     * @param key    键
     * @param values 值
     * @return 新增成员数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(wrapKey(key), values);
    }

    /**
     * 获取 Set 中所有成员
     *
     * @param key 键
     * @return 成员集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(wrapKey(key));
    }

    /**
     * 判断 Set 中是否包含指定值
     *
     * @param key   键
     * @param value 值
     * @return true 包含 / false 不包含
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(wrapKey(key), value);
    }

    /**
     * 获取 Set 的大小
     *
     * @param key 键
     * @return 成员数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(wrapKey(key));
    }

    /**
     * 从 Set 中移除一个或多个值
     *
     * @param key    键
     * @param values 值
     * @return 实际移除的数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(wrapKey(key), values);
    }

    /**
     * 随机弹出 Set 中的一个成员
     *
     * @param key 键
     * @return 弹出的成员
     */
    public Object sPop(String key) {
        return redisTemplate.opsForSet().pop(wrapKey(key));
    }

    // ============================ ZSet 操作 ============================

    /**
     * 向 ZSet 中写入成员及其分数
     *
     * @param key    键
     * @param value  成员
     * @param score  分数
     * @return true 新增成功 / false 已存在仅更新分数
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(wrapKey(key), value, score);
    }

    /**
     * 从 ZSet 中移除一个或多个成员
     *
     * @param key    键
     * @param values 成员
     * @return 实际移除的数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(wrapKey(key), values);
    }

    /**
     * 获取成员的分数
     *
     * @param key   键
     * @param value 成员
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(wrapKey(key), value);
    }

    /**
     * 获取成员在 ZSet 中的排名（从小到大，从 0 开始）
     *
     * @param key   键
     * @param value 成员
     * @return 排名
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(wrapKey(key), value);
    }

    /**
     * 获取成员在 ZSet 中的排名（从大到小，从 0 开始）
     *
     * @param key   键
     * @param value 成员
     * @return 排名
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(wrapKey(key), value);
    }

    /**
     * 获取指定分数范围内的成员（升序）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 成员集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(wrapKey(key), min, max);
    }

    /**
     * 获取指定排名范围内的成员（升序，start ~ end 闭区间）
     *
     * @param key   键
     * @param start 起始排名
     * @param end   结束排名
     * @return 成员集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(wrapKey(key), start, end);
    }

    /**
     * 获取指定排名范围内的成员（降序，start ~ end 闭区间）
     *
     * @param key   键
     * @param start 起始排名
     * @param end   结束排名
     * @return 成员集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(wrapKey(key), start, end);
    }

    /**
     * 获取 ZSet 的大小
     *
     * @param key 键
     * @return 成员数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(wrapKey(key));
    }

    /**
     * 成员分数自增
     *
     * @param key       键
     * @param value     成员
     * @param increment 步长
     * @return 自增后的分数
     */
    public Double zIncrScore(String key, Object value, double increment) {
        return redisTemplate.opsForZSet().incrementScore(wrapKey(key), value, increment);
    }

    /**
     * 获取 ZSet 中指定范围内的成员及分数（升序）
     *
     * @param key   键
     * @param start 起始排名
     * @param end   结束排名
     * @return 成员与分数的集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(wrapKey(key), start, end);
    }
}
