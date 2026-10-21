package cn.iocoder.yudao.module.iot.framework.iotdb.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.iotdb.rpc.StatementExecutionException;

import java.sql.SQLException;
import java.util.*;

/**
 * IoTDB 工具类
 *
 * 1. 异常分类：识别“表不存在”
 * 2. 启动前提检查：只读取配置并校验，不修改 IoTDB 的任何配置
 *
 * @author 芋道源码
 */
public class IoTDBUtils {

    /**
     * IoTDB 状态码 - 表不存在
     */
    public static final int STATUS_CODE_TABLE_NOT_EXISTS = 550;

    /**
     * 要求的时间精度：毫秒
     */
    public static final String TIMESTAMP_PRECISION_MS = "ms";
    /**
     * 要求关闭的配置项：部分写入。开启时，一行中类型错误的列会失败，其它列仍然写入
     */
    public static final String CONFIG_ENABLE_PARTIAL_INSERT = "enable_partial_insert";
    /**
     * DataNode 的运行状态
     */
    public static final String DATA_NODE_STATUS_RUNNING = "Running";

    // ========== 异常分类 ==========

    /**
     * 判断异常是否为“表不存在”
     *
     * 只识别明确的 550 状态码，并沿 cause 链查找：外层 SQLException 的 errorCode 可能为 0，
     * 真实状态码在内层的 SQLException 或 {@link StatementExecutionException} 中。
     * 列不存在（616）、类型不匹配（507）、权限等其它错误，都返回 false，由调用方原样抛出。
     *
     * @param throwable 异常
     * @return 是否表不存在
     */
    @SuppressWarnings("PatternVariableCanBeUsed") // 保持 JDK8 兼容性
    public static boolean isTableNotExists(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = throwable; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof StatementExecutionException
                    && ((StatementExecutionException) cause).getStatusCode() == STATUS_CODE_TABLE_NOT_EXISTS) {
                return true;
            }
            if (cause instanceof SQLException
                    && ((SQLException) cause).getErrorCode() == STATUS_CODE_TABLE_NOT_EXISTS) {
                return true;
            }
        }
        return false;
    }

    // ========== 启动前提检查 ==========

    /**
     * 校验时间精度为毫秒
     *
     * @param variables SHOW VARIABLES 的结果
     */
    public static void validateTimestampPrecision(List<Map<String, Object>> variables) {
        Map<String, Object> variable = CollUtil.findOne(variables,
                row -> StrUtil.equalsIgnoreCase(getString(row, "Variable"), "TimestampPrecision"));
        if (variable == null) {
            throw new IllegalStateException("[IoTDB 前提检查] 无法读取 TimestampPrecision，请确认账号有查询权限");
        }
        String precision = getString(variable, "Value");
        if (!StrUtil.equals(TIMESTAMP_PRECISION_MS, precision)) {
            throw new IllegalStateException(StrUtil.format(
                    "[IoTDB 前提检查] TimestampPrecision 必须为 ms，当前为 {}", precision));
        }
    }

    /**
     * 校验所有 DataNode 都处于运行状态，并返回它们的编号
     *
     * @param dataNodes SHOW DATANODES 的结果
     * @return DataNode 编号列表
     */
    public static List<Integer> validateDataNodes(List<Map<String, Object>> dataNodes) {
        if (CollUtil.isEmpty(dataNodes)) {
            throw new IllegalStateException("[IoTDB 前提检查] 无法读取 DataNode 列表，请确认账号有查询权限");
        }
        List<Integer> nodeIds = new ArrayList<>(dataNodes.size());
        for (Map<String, Object> dataNode : dataNodes) {
            Object nodeId = getIgnoreCase(dataNode, "NodeID");
            String status = getString(dataNode, "Status");
            if (!(nodeId instanceof Number)) {
                throw new IllegalStateException(StrUtil.format("[IoTDB 前提检查] 无法识别 DataNode 编号：{}", dataNode));
            }
            if (!StrUtil.equalsIgnoreCase(DATA_NODE_STATUS_RUNNING, status)) {
                throw new IllegalStateException(StrUtil.format(
                        "[IoTDB 前提检查] DataNode({}) 状态为 {}，必须全部为 Running 才能确认部分写入配置", nodeId, status));
            }
            nodeIds.add(((Number) nodeId).intValue());
        }
        return nodeIds;
    }

    /**
     * 校验 DataNode 关闭了部分写入（enable_partial_insert=false）
     *
     * @param nodeId         DataNode 编号
     * @param configurations SHOW ALL CONFIGURATION ON nodeId 的结果
     */
    public static void validatePartialInsertDisabled(Integer nodeId, List<Map<String, Object>> configurations) {
        Map<String, Object> configuration = CollUtil.findOne(configurations,
                row -> StrUtil.equals(getString(row, "name"), CONFIG_ENABLE_PARTIAL_INSERT));
        if (configuration == null) {
            throw new IllegalStateException(StrUtil.format(
                    "[IoTDB 前提检查] DataNode({}) 无法读取 {} 配置，请确认 IoTDB 版本与账号权限", nodeId, CONFIG_ENABLE_PARTIAL_INSERT));
        }
        String value = getString(configuration, "value");
        if (!StrUtil.equalsIgnoreCase(Boolean.FALSE.toString(), value)) {
            throw new IllegalStateException(StrUtil.format(
                    "[IoTDB 前提检查] DataNode({}) 的 {} 必须为 false，当前为 {}。请修改所有 DataNode 的配置文件，应用不会自动修改",
                    nodeId, CONFIG_ENABLE_PARTIAL_INSERT, value));
        }
    }

    /**
     * 忽略大小写，获得结果行的列值。原因：IoTDB 系统语句的列名为 NodeID、Variable 等，不同版本大小写可能不同
     */
    public static Object getIgnoreCase(Map<String, Object> row, String column) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (StrUtil.equalsIgnoreCase(entry.getKey(), column)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String getString(Map<String, Object> row, String column) {
        Object value = getIgnoreCase(row, column);
        return value != null ? value.toString() : null;
    }

}
