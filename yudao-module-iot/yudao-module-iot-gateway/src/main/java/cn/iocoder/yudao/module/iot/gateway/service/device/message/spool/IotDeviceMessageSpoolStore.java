package cn.iocoder.yudao.module.iot.gateway.service.device.message.spool;

import cn.hutool.core.io.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * IoT 网关离线暂存的 SQLite 存储：单文件，WAL + synchronous=FULL，写入返回时已经持久化
 * <p>
 * 只有一个连接，所有操作串行执行；按自增编号的顺序读取、删除。消息数、字节数（消息 JSON 的 UTF-8 字节数）在内存中累计，
 * 打开时从文件统计
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceMessageSpoolStore {

    private final String file;

    private Connection connection;

    /**
     * 暂存的消息数
     */
    @Getter
    private long count;
    /**
     * 暂存的字节数
     */
    @Getter
    private long bytes;

    public IotDeviceMessageSpoolStore(String file) {
        this.file = file;
    }

    /**
     * 打开：校验文件完整、建表，统计已暂存的消息
     *
     * @throws SQLException 打开失败或文件损坏：不删除文件，需要人工处理
     */
    public synchronized void open() throws SQLException {
        FileUtil.mkParentDirs(file);
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
            // 1. 校验文件完整：损坏时不使用，也不删除
            try (ResultSet rs = statement.executeQuery("PRAGMA quick_check")) {
                String result = rs.next() ? rs.getString(1) : null;
                if (!"ok".equals(result)) {
                    throw new SQLException("SQLite 文件校验失败：" + result);
                }
            }
            // 2. WAL + FULL：每次提交都刷盘，返回时已经持久化
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = FULL");
            statement.execute("CREATE TABLE IF NOT EXISTS iot_device_message_spool ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "spool_time INTEGER NOT NULL, "
                    + "size INTEGER NOT NULL, "
                    + "message TEXT NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_iot_device_message_spool_time "
                    + "ON iot_device_message_spool (spool_time)");
            // 3. 统计已暂存的消息
            try (ResultSet rs = statement.executeQuery(
                    "SELECT COUNT(*), COALESCE(SUM(size), 0) FROM iot_device_message_spool")) {
                rs.next();
                count = rs.getLong(1);
                bytes = rs.getLong(2);
            }
        } catch (SQLException e) {
            close();
            throw e;
        }
    }

    /**
     * 写入一条消息
     *
     * @param spoolTime 网关接收时刻
     * @param message   消息 JSON
     * @param size      消息 JSON 的 UTF-8 字节数
     */
    public synchronized void insert(long spoolTime, String message, int size) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO iot_device_message_spool (spool_time, size, message) VALUES (?, ?, ?)")) {
            ps.setLong(1, spoolTime);
            ps.setInt(2, size);
            ps.setString(3, message);
            ps.executeUpdate();
        }
        count++;
        bytes += size;
    }

    /**
     * 按编号顺序，读取最早的若干条消息
     */
    public synchronized List<SpoolRecord> selectList(int limit) throws SQLException {
        List<SpoolRecord> records = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, message FROM iot_device_message_spool ORDER BY id LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new SpoolRecord(rs.getLong(1), rs.getString(2)));
                }
            }
        }
        return records;
    }

    /**
     * 删除编号不大于 maxId 的消息（已补传、或无法解析）
     */
    public synchronized void deleteUpTo(long maxId) throws SQLException {
        delete("id <= ?", maxId);
    }

    /**
     * 删除网关接收时刻早于 minSpoolTime 的消息（超过保留时间）
     *
     * @return 删除的消息数
     */
    public synchronized long deleteExpired(long minSpoolTime) throws SQLException {
        return delete("spool_time < ?", minSpoolTime);
    }

    private long delete(String where, long value) throws SQLException {
        // 先统计，再删除：只有一个连接且串行执行，两条语句之间不会有其它写入
        long deletedCount;
        long deletedBytes;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*), COALESCE(SUM(size), 0) FROM iot_device_message_spool WHERE " + where)) {
            ps.setLong(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                deletedCount = rs.getLong(1);
                deletedBytes = rs.getLong(2);
            }
        }
        if (deletedCount == 0) {
            return 0;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM iot_device_message_spool WHERE " + where)) {
            ps.setLong(1, value);
            ps.executeUpdate();
        }
        count -= deletedCount;
        bytes -= deletedBytes;
        return deletedCount;
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("[close][关闭离线暂存文件({}) 失败]", file, e);
        }
        connection = null;
    }

    /**
     * 暂存的一条消息
     */
    @Getter
    @AllArgsConstructor
    public static class SpoolRecord {

        /**
         * 编号
         */
        private final long id;
        /**
         * 消息 JSON
         */
        private final String message;

    }

}
