package cn.iocoder.yudao.module.iot.gateway.service.device.message.spool;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.redis.IotRedisMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_MESSAGE_SPOOL_REJECTED;

/**
 * IoT 网关离线暂存 Service：消息总线（Redis）不可用时，把上行数据暂存到本地 SQLite，恢复后按编号顺序补传
 * <p>
 * 1. 发送：没有积压时直发；直发失败、或已有积压时，写入暂存（提交后才返回，调用方再确认设备）；暂存失败时抛出
 *    {@link cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants#DEVICE_MESSAGE_SPOOL_REJECTED}，调用方不能确认设备
 * 2. 补传：单线程按编号顺序发送，进入 Redis 后才删除；Redis 失败时保留，稍后重试；无法解析的记录记日志后删除
 * 3. 顺序：只保证暂存内按编号顺序补传。直发与暂存切换的瞬间，并发的直发可能先于暂存中的消息进入 Redis
 * <p>
 * 补传只保证进入 Redis，不保证平台已经处理（入库），也不去重：发送成功但删除前进程退出，重启后会再补传一次
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceMessageSpoolService {

    /**
     * 暂存的消息方法：属性上报、事件上报、批量上报；不含回执（code 非空）
     */
    private static final Set<String> SPOOL_METHODS = SetUtils.asSet(
            IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
            IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
            IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod());
    /**
     * 暂存的协议：MQTT、HTTP；其它协议的确认方式不同，不暂存
     */
    private static final Set<IotProtocolTypeEnum> SPOOL_PROTOCOLS = SetUtils.asSet(
            IotProtocolTypeEnum.MQTT, IotProtocolTypeEnum.HTTP);

    /**
     * 补传时，每次读取的消息数
     */
    private static final int DRAIN_BATCH_SIZE = 100;
    /**
     * 补传任务的执行间隔：没有积压、或 Redis 失败时，下一轮的等待时间
     */
    private static final long DRAIN_INTERVAL_MILLIS = 1000;
    /**
     * 删除过期消息的间隔
     */
    private static final long EXPIRE_INTERVAL_MILLIS = 60 * 1000;

    @Resource
    private IotGatewayProperties gatewayProperties;
    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;
    @Resource
    private IotMessageBus messageBus;

    private boolean enabled;
    /**
     * SQLite 存储：打开失败（例如文件损坏）时为空，此时只能直发，直发失败时拒绝
     */
    private IotDeviceMessageSpoolStore store;
    /**
     * 是否暂存中：已有积压、或直发失败，新消息一律写入暂存，不再直发
     * <p>
     * 写入暂存与「暂存清空后恢复直发」都在 {@link #lock} 内执行：恢复直发时，暂存一定为空
     */
    private volatile boolean spooling;
    private final Object lock = new Object();

    private ScheduledExecutorService drainExecutor;
    private long lastExpireTime;

    @PostConstruct
    public void init() {
        IotGatewayProperties.SpoolProperties properties = gatewayProperties.getSpool();
        if (!properties.isEnabled()) {
            return;
        }
        if (!(messageBus instanceof IotRedisMessageBus)) {
            throw new IllegalStateException("离线暂存只支持 Redis 消息总线，当前为：" + messageBus.getClass().getSimpleName());
        }
        enabled = true;
        // 1. 打开暂存：失败时不删除文件，只能直发，直发失败时拒绝
        IotDeviceMessageSpoolStore spoolStore = new IotDeviceMessageSpoolStore(properties.getFile());
        try {
            spoolStore.open();
            store = spoolStore;
            spooling = store.getCount() > 0;
            log.info("[init][离线暂存({}) 已打开，积压 {} 条，{} 字节]", properties.getFile(),
                    store.getCount(), store.getBytes());
        } catch (SQLException e) {
            log.error("[init][离线暂存({}) 打开失败，文件已保留，需要人工处理；在此之前只能直发，直发失败时拒绝]",
                    properties.getFile(), e);
            return;
        }

        // 2. 启动补传任务
        drainExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "iot-gateway-spool-drain");
            thread.setDaemon(true);
            return thread;
        });
        drainExecutor.scheduleWithFixedDelay(this::drain, DRAIN_INTERVAL_MILLIS, DRAIN_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (drainExecutor != null) {
            drainExecutor.shutdownNow();
            try {
                if (!drainExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("[destroy][补传任务未在 5 秒内结束]");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (store != null) {
            store.close();
        }
    }

    /**
     * 是否走离线暂存：已开启，MQTT、HTTP 的属性上报、事件上报、批量上报，且不是回执
     *
     * @param message  消息
     * @param protocol 协议；为空时不暂存
     * @return 是否走离线暂存
     */
    public boolean isSpoolable(IotDeviceMessage message, IotProtocolTypeEnum protocol) {
        return enabled && protocol != null && SPOOL_PROTOCOLS.contains(protocol)
                && SPOOL_METHODS.contains(message.getMethod())
                && !IotDeviceMessageUtils.isReplyMessage(message);
    }

    /**
     * 发送消息：没有积压时直发；直发失败、或已有积压时，写入暂存
     *
     * @param message   消息
     * @param spoolTime 网关接收时刻，与消息的 reportTime 是同一次取样
     */
    public void send(IotDeviceMessage message, long spoolTime) {
        // 1. 直发：没有积压
        if (!spooling) {
            try {
                deviceMessageProducer.sendDeviceMessage(message);
                return;
            } catch (Exception ex) {
                log.warn("[send][消息({}) 发送消息总线失败，写入离线暂存：{}]", message.getId(), ex.getMessage());
            }
        }

        // 2. 写入暂存
        append(message, spoolTime);
    }

    private void append(IotDeviceMessage message, long spoolTime) {
        if (store == null) {
            throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "暂存文件不可用");
        }
        message.setSpoolTime(spoolTime);
        String json = JsonUtils.toJsonString(message);
        int size = StrUtil.utf8Bytes(json).length;
        long maxSize = gatewayProperties.getSpool().getMaxSize().toBytes();
        synchronized (lock) {
            if (store.getBytes() + size > maxSize) {
                log.error("[append][离线暂存已满（{} 条，{} 字节），拒绝消息({})]",
                        store.getCount(), store.getBytes(), message.getId());
                throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "暂存已满");
            }
            try {
                store.insert(spoolTime, json, size);
            } catch (SQLException e) {
                log.error("[append][消息({}) 写入离线暂存失败]", message.getId(), e);
                throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "写入暂存失败");
            }
            if (!spooling) {
                log.warn("[append][开始离线暂存，新消息不再直发，等待补传]");
                spooling = true;
            }
        }
    }

    /**
     * 补传：按编号顺序发送，进入 Redis 后删除；Redis 失败时保留，等待下一轮
     */
    void drain() {
        try {
            expireIfNecessary();
            long intervalNanos = TimeUnit.SECONDS.toNanos(1) / gatewayProperties.getSpool().getDrainRate();
            while (!Thread.currentThread().isInterrupted()) {
                // 1. 读取最早的一批：没有积压时，恢复直发
                List<IotDeviceMessageSpoolStore.SpoolRecord> records = store.selectList(DRAIN_BATCH_SIZE);
                if (records.isEmpty()) {
                    resumeIfEmpty();
                    return;
                }

                // 2. 逐条发送：失败时停止，已发送的删除，未发送的保留
                long sentId = 0;
                boolean failed = false;
                for (IotDeviceMessageSpoolStore.SpoolRecord record : records) {
                    IotDeviceMessage message = parseMessage(record);
                    if (message != null) {
                        try {
                            deviceMessageProducer.sendDeviceMessage(message);
                        } catch (Exception ex) {
                            log.warn("[drain][补传消息({}) 失败，保留并稍后重试：{}]", message.getId(), ex.getMessage());
                            failed = true;
                            break;
                        }
                    }
                    sentId = record.getId();
                    if (!sleepNanos(intervalNanos)) {
                        failed = true;
                        break;
                    }
                }
                if (sentId > 0) {
                    store.deleteUpTo(sentId);
                }
                if (failed) {
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("[drain][补传异常，稍后重试]", ex);
        }
    }

    /**
     * 暂存为空时恢复直发：与写入暂存互斥，保证恢复直发时没有积压
     */
    private void resumeIfEmpty() {
        synchronized (lock) {
            if (spooling && store.getCount() == 0) {
                spooling = false;
                log.info("[resumeIfEmpty][离线暂存已补传完成，恢复直发]");
            }
        }
    }

    /**
     * 删除超过保留时间的消息：释放暂存的字节数配额
     */
    private void expireIfNecessary() throws SQLException {
        long now = System.currentTimeMillis();
        if (now - lastExpireTime < EXPIRE_INTERVAL_MILLIS) {
            return;
        }
        lastExpireTime = now;
        long bytesBefore = store.getBytes();
        long deleted = store.deleteExpired(now - gatewayProperties.getSpool().getMaxAge().toMillis());
        if (deleted > 0) {
            log.warn("[expireIfNecessary][删除超过保留时间({}) 的暂存消息 {} 条，释放 {} 字节，剩余 {} 条]",
                    gatewayProperties.getSpool().getMaxAge(), deleted, bytesBefore - store.getBytes(), store.getCount());
        }
    }

    /**
     * 解析暂存的消息：无法解析时记日志，返回空（由调用方删除，避免补传一直卡在这条）
     */
    private static IotDeviceMessage parseMessage(IotDeviceMessageSpoolStore.SpoolRecord record) {
        try {
            IotDeviceMessage message = JsonUtils.parseObject(record.getMessage(), IotDeviceMessage.class);
            if (message != null && message.getSpoolTime() != null) {
                return message;
            }
        } catch (Exception ignored) {
        }
        log.error("[parseMessage][暂存记录({}) 无法解析，丢弃：{}]", record.getId(),
                StrUtil.maxLength(record.getMessage(), 200));
        return null;
    }

    /**
     * 补传限速
     *
     * @return 是否正常结束；被中断（关闭）时返回 false
     */
    private static boolean sleepNanos(long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
