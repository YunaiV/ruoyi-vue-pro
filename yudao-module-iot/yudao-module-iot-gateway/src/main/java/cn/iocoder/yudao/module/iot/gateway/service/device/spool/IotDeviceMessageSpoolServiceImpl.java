package cn.iocoder.yudao.module.iot.gateway.service.device.spool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import cn.iocoder.yudao.module.iot.core.messagebus.core.local.IotLocalMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_MESSAGE_REJECTED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_MESSAGE_SPOOL_REJECTED;

/**
 * IoT 网关离线暂存 Service 实现类：消息总线不可用时，把上行数据暂存到本地 SQLite，恢复后按编号顺序补传
 * <p>
 * 1. 范围：已开启时，协议入口（指定了协议）的属性上报、事件上报、批量上报，含透传解析出的；不含回执、状态、纯原始帧
 * 2. 发送：没有积压时直发；直发失败、或已有积压时，写入暂存（提交后才返回，调用方再确认设备）；暂存失败时抛出
 *    {@link cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants#DEVICE_MESSAGE_SPOOL_REJECTED}，调用方不能确认设备；
 *    直发被永久拒绝（{@link IotMessageRejectedException}，例如说超过大小限制）时不暂存，抛出
 *    {@link cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants#DEVICE_MESSAGE_REJECTED}
 * 3. 补传：单线程按编号顺序发送，broker 确认后才删除；发送失败（含超时）时保留，稍后重试；
 *    无法解析、超过保留时间、被永久拒绝的记录记日志后跳过（删除），不阻塞后面的消息
 * 4. 顺序：只保证暂存内按编号顺序补传。直发与暂存切换的瞬间，并发的直发可能先于暂存中的消息进入消息总线
 * <p>
 * 存储：单个 SQLite 文件，WAL + synchronous=FULL，写入返回时已经持久化；只有一个连接，读写都在 {@link #lock} 内串行执行。
 * 消息数、字节数（消息 JSON 的 UTF-8 字节数）在内存中累计，打开时从文件统计
 * <p>
 * 发送：直发、补传都经 {@link IotMessageBus#postConfirmed} 确认发送，各消息总线自行保证「返回即 broker 已接收」；
 * Local 消息总线为进程内，没有断网，不启用。确认只到 broker，不保证平台已经处理（入库），也不去重：
 * 发送成功但删除前进程退出，重启后会再补传一次
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceMessageSpoolServiceImpl implements IotDeviceMessageSpoolService {

    /**
     * 暂存的消息方法：属性上报、事件上报、批量上报；不含回执（code 非空）
     */
    private static final Set<String> SPOOL_METHODS = SetUtils.asSet(
            IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
            IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
            IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod());

    /**
     * 补传时，每次读取的消息数
     */
    private static final int DRAIN_BATCH_SIZE = 100;
    /**
     * 补传任务的执行间隔：没有积压、或发送失败时，下一轮的等待时间
     */
    private static final long DRAIN_INTERVAL_MILLIS = 1000;
    /**
     * 删除过期消息的间隔
     */
    private static final long EXPIRE_INTERVAL_MILLIS = 60 * 1000;

    /**
     * 统计消息数、字节数的结果映射：[消息数, 字节数]
     */
    private static final RowMapper<long[]> COUNT_BYTES_ROW_MAPPER = (rs, rowNum) -> new long[]{rs.getLong(1), rs.getLong(2)};

    @Resource
    private IotGatewayProperties gatewayProperties;
    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;
    @Resource
    private IotMessageBus messageBus;

    private boolean enabled;
    /**
     * SQLite 的单连接数据源：打开失败（例如文件损坏）时为空，此时只能直发，直发失败时拒绝
     */
    private SingleConnectionDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    /**
     * 暂存的消息数
     */
    private long count;
    /**
     * 暂存的字节数
     */
    private long bytes;
    /**
     * 是否暂存中：已有积压、或直发失败，新消息一律写入暂存，不再直发
     * <p>
     * 写入暂存与「暂存清空后恢复直发」都在 {@link #lock} 内执行：恢复直发时，暂存一定为空
     */
    private volatile boolean spooling;
    /**
     * 锁：SQLite 的读写、暂存状态的切换；发送消息总线时不持有
     */
    private final Object lock = new Object();

    private ScheduledExecutorService drainExecutor;
    private long lastExpireTime;

    @PostConstruct
    public void init() {
        IotGatewayProperties.SpoolProperties properties = gatewayProperties.getSpool();
        if (!properties.isEnabled()) {
            return;
        }
        // Local 消息总线：进程内发布，没有网络断开，不需要也不启用离线暂存，保持原来的直发
        if (messageBus instanceof IotLocalMessageBus) {
            log.warn("[init][Local 消息总线为进程内，离线暂存不生效]");
            return;
        }
        // 校验配置：配额不合法、消息总线不能确认发送时，启动失败，避免消息只进不出
        if (properties.getMaxSize().toBytes() <= 0 || properties.getMaxAge().isNegative()
                || properties.getMaxAge().isZero()) {
            throw new IllegalStateException(StrUtil.format("离线暂存的 max-size({})、max-age({}) 必须大于 0",
                    properties.getMaxSize(), properties.getMaxAge()));
        }
        messageBus.validateConfirmable();
        enabled = true;
        // 1. 打开暂存：失败时不删除文件，只能直发，直发失败时拒绝
        try {
            open(properties.getFile());
            log.info("[init][离线暂存({}) 已打开，积压 {} 条，{} 字节]", properties.getFile(), count, bytes);
        } catch (Exception e) {
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
        close();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isSpoolableMessage(IotDeviceMessage message, IotProtocolTypeEnum protocol) {
        // 协议非空：调用方是协议入口，会按协议自己的方式处理 DEVICE_MESSAGE_SPOOL_REJECTED（不确认设备，或应答错误）
        return enabled && protocol != null
                && SPOOL_METHODS.contains(message.getMethod())
                && !IotDeviceMessageUtils.isReplyMessage(message);
    }

    @Override
    public void sendSpoolableMessage(IotDeviceMessage message, long spoolTime) {
        // 1. 直发：没有积压
        if (!spooling) {
            try {
                deviceMessageProducer.sendDeviceMessageConfirmed(message);
                return;
            } catch (IotMessageRejectedException ex) {
                // 被永久拒绝（例如说超过大小限制）：写入暂存也补传不了，直接返回错误
                log.error("[sendSpoolableMessage][消息({}) 被消息总线拒绝，不写入离线暂存：{}]", message.getId(), ex.getMessage());
                throw exception(DEVICE_MESSAGE_REJECTED);
            } catch (Exception ex) {
                log.warn("[sendSpoolableMessage][消息({}) 发送消息总线失败，写入离线暂存：{}]", message.getId(), ex.getMessage());
            }
        }

        // 2. 写入暂存
        append(message, spoolTime);
    }

    /**
     * 写入暂存：提交（刷盘）后才返回，之后新消息一律写入暂存，直到补传完成
     *
     * @param message   消息
     * @param spoolTime 网关接收时刻
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 暂存文件不可用、已满、写入失败
     */
    private void append(IotDeviceMessage message, long spoolTime) {
        // 1. 序列化：带上 spoolTime，按 UTF-8 字节数计入配额
        message.setSpoolTime(spoolTime);
        String json = JsonUtils.toJsonString(message);
        int size = StrUtil.utf8Bytes(json).length;
        long maxSize = gatewayProperties.getSpool().getMaxSize().toBytes();
        synchronized (lock) {
            // 2.1 校验暂存可用、未满
            if (jdbcTemplate == null) {
                throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "暂存文件不可用");
            }
            if (bytes + size > maxSize) {
                log.error("[append][离线暂存已满（{} 条，{} 字节），拒绝消息({})]", count, bytes, message.getId());
                throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "暂存已满");
            }
            // 2.2 写入
            try {
                insert(spoolTime, json, size);
            } catch (DataAccessException e) {
                log.error("[append][消息({}) 写入离线暂存失败]", message.getId(), e);
                throw exception(DEVICE_MESSAGE_SPOOL_REJECTED, "写入暂存失败");
            }
            // 2.3 进入暂存中：新消息不再直发，由补传任务清空后恢复
            if (!spooling) {
                log.warn("[append][开始离线暂存，新消息不再直发，等待补传]");
                spooling = true;
            }
        }
    }

    /**
     * 补传：按编号顺序发送，进入消息总线后删除；发送失败时保留，等待下一轮
     */
    void drain() {
        try {
            long intervalNanos = TimeUnit.SECONDS.toNanos(1) / gatewayProperties.getSpool().getDrainRate();
            while (!Thread.currentThread().isInterrupted()) {
                // 1.1 删除过期消息：每批都检查（内部按间隔节流），持续积压时也能清理
                expireIfNecessary();
                // 1.2 读取最早的一批：没有积压时，恢复直发
                Map<Long, String> records = selectList(DRAIN_BATCH_SIZE);
                if (records.isEmpty()) {
                    resumeIfEmpty();
                    return;
                }

                // 2. 逐条发送：失败时停止，已处理（发送、跳过）的删除，未发送的保留
                long sentId = 0;
                boolean failed = false;
                for (Map.Entry<Long, String> record : records.entrySet()) {
                    IotDeviceMessage message = parseMessage(record.getKey(), record.getValue());
                    if (message != null && !drainMessage(message)) {
                        failed = true;
                        break;
                    }
                    sentId = record.getKey();
                    if (!sleepNanos(intervalNanos)) {
                        failed = true;
                        break;
                    }
                }
                if (sentId > 0) {
                    delete("id <= ?", sentId);
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
     * 补传一条消息
     *
     * @param message 消息
     * @return 是否处理完成（可以删除）：已发送，或超过保留时间、被永久拒绝而跳过；发送失败时返回 false（保留，稍后重试）
     */
    private boolean drainMessage(IotDeviceMessage message) {
        // 1. 超过保留时间：不再补传。发出前逐条检查，不只依赖定期的过期删除
        Duration maxAge = gatewayProperties.getSpool().getMaxAge();
        if (message.getSpoolTime() < System.currentTimeMillis() - maxAge.toMillis()) {
            log.warn("[drainMessage][补传消息({}) 超过保留时间({})，跳过]", message.getId(), maxAge);
            return true;
        }

        // 2. 发送：被永久拒绝（例如说超过大小限制）时跳过，避免阻塞后面的消息；其它失败（含超时）保留
        try {
            deviceMessageProducer.sendDeviceMessageConfirmed(message);
            return true;
        } catch (IotMessageRejectedException ex) {
            log.error("[drainMessage][补传消息({}) 被消息总线拒绝，重试也不会成功，跳过：{}]", message.getId(), ex.getMessage());
            return true;
        } catch (Exception ex) {
            log.warn("[drainMessage][补传消息({}) 失败，保留并稍后重试：{}]", message.getId(), ex.getMessage());
            return false;
        }
    }

    /**
     * 暂存为空时恢复直发：与写入暂存互斥，保证恢复直发时没有积压
     */
    private void resumeIfEmpty() {
        synchronized (lock) {
            if (spooling && count == 0) {
                spooling = false;
                log.info("[resumeIfEmpty][离线暂存已补传完成，恢复直发]");
            }
        }
    }

    /**
     * 删除超过保留时间的消息：释放暂存的字节数配额
     */
    private void expireIfNecessary() {
        long now = System.currentTimeMillis();
        if (now - lastExpireTime < EXPIRE_INTERVAL_MILLIS) {
            return;
        }
        lastExpireTime = now;
        // 在锁内删除并读取计数：避免并发写入，导致释放的字节数、剩余条数不准
        synchronized (lock) {
            long bytesBefore = bytes;
            long deleted = delete("spool_time < ?", now - gatewayProperties.getSpool().getMaxAge().toMillis());
            if (deleted > 0) {
                log.warn("[expireIfNecessary][删除超过保留时间({}) 的暂存消息 {} 条，释放 {} 字节，剩余 {} 条]",
                        gatewayProperties.getSpool().getMaxAge(), deleted, bytesBefore - bytes, count);
            }
        }
    }

    /**
     * 解析暂存的消息：无法解析时记日志，返回空（由调用方删除，避免补传一直卡在这条）
     */
    private static IotDeviceMessage parseMessage(long id, String json) {
        try {
            IotDeviceMessage message = JsonUtils.parseObject(json, IotDeviceMessage.class);
            if (message != null && message.getSpoolTime() != null) {
                return message;
            }
        } catch (Exception ignored) {
        }
        log.error("[parseMessage][暂存记录({}) 无法解析，丢弃：{}]", id, StrUtil.maxLength(json, 200));
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

    // ========== SQLite 存储 ==========

    /**
     * 打开暂存文件：校验文件完整、建表，统计已暂存的消息
     *
     * @param file 文件路径
     * @throws RuntimeException 打开失败或文件损坏：不删除文件，需要人工处理
     */
    void open(String file) {
        synchronized (lock) {
            // 1. 创建单连接的数据源：所有读写在锁内串行，使用同一个连接
            FileUtil.mkParentDirs(file);
            SingleConnectionDataSource newDataSource = new SingleConnectionDataSource("jdbc:sqlite:" + file, true);
            JdbcTemplate newJdbcTemplate = new JdbcTemplate(newDataSource);
            try {
                newJdbcTemplate.execute("PRAGMA busy_timeout = 5000");
                // 2. 校验文件完整：损坏时不使用，也不删除
                String result = newJdbcTemplate.queryForObject("PRAGMA quick_check", String.class);
                if (!"ok".equals(result)) {
                    throw new IllegalStateException("SQLite 文件校验失败：" + result);
                }
                // 3. WAL + FULL：每次提交都刷盘，返回时已经持久化
                newJdbcTemplate.execute("PRAGMA journal_mode = WAL");
                newJdbcTemplate.execute("PRAGMA synchronous = FULL");
                newJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS iot_device_message_spool ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "spool_time INTEGER NOT NULL, "
                        + "size INTEGER NOT NULL, "
                        + "message TEXT NOT NULL)");
                newJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_iot_device_message_spool_time "
                        + "ON iot_device_message_spool (spool_time)");
                // 4. 统计已暂存的消息：有积压时，处于暂存中
                long[] countAndBytes = newJdbcTemplate.queryForObject(
                        "SELECT COUNT(*), COALESCE(SUM(size), 0) FROM iot_device_message_spool", COUNT_BYTES_ROW_MAPPER);
                count = countAndBytes[0];
                bytes = countAndBytes[1];
            } catch (RuntimeException e) {
                newDataSource.destroy();
                throw e;
            }
            dataSource = newDataSource;
            jdbcTemplate = newJdbcTemplate;
            spooling = count > 0;
        }
    }

    /**
     * 关闭暂存文件
     */
    void close() {
        synchronized (lock) {
            if (dataSource == null) {
                return;
            }
            dataSource.destroy();
            dataSource = null;
            jdbcTemplate = null;
        }
    }

    /**
     * 写入一条消息；调用方已持有 {@link #lock}
     */
    private void insert(long spoolTime, String json, int size) {
        jdbcTemplate.update("INSERT INTO iot_device_message_spool (spool_time, size, message) VALUES (?, ?, ?)",
                spoolTime, size, json);
        count++;
        bytes += size;
    }

    /**
     * 按编号顺序，读取最早的若干条消息
     *
     * @return 编号与消息 JSON，按编号排序
     */
    private Map<Long, String> selectList(int limit) {
        synchronized (lock) {
            // 使用 LinkedHashMap：保持 ORDER BY id 的顺序
            Map<Long, String> records = new LinkedHashMap<>();
            // 已关闭（与补传竞争）：不再读取
            if (jdbcTemplate == null) {
                return records;
            }
            jdbcTemplate.query("SELECT id, message FROM iot_device_message_spool ORDER BY id LIMIT ?",
                    rs -> {
                        records.put(rs.getLong(1), rs.getString(2));
                    }, limit);
            return records;
        }
    }

    /**
     * 删除消息：已补传、无法解析的（id &lt;= ?），超过保留时间的（spool_time &lt; ?）
     *
     * @return 删除的消息数
     */
    private long delete(String where, long value) {
        synchronized (lock) {
            // 0. 已关闭（与补传竞争）：不删除，消息保留在文件中，重启后再补传（已发送的会重复一次）
            if (jdbcTemplate == null) {
                return 0;
            }
            // 1. 统计要删除的消息数、字节数：在锁内执行，与删除之间不会有其它写入
            long[] deleted = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*), COALESCE(SUM(size), 0) FROM iot_device_message_spool WHERE " + where,
                    COUNT_BYTES_ROW_MAPPER, value);
            if (deleted[0] == 0) {
                return 0;
            }
            // 2. 删除，并释放配额
            jdbcTemplate.update("DELETE FROM iot_device_message_spool WHERE " + where, value);
            count -= deleted[0];
            bytes -= deleted[1];
            return deleted[0];
        }
    }

}
