package cn.iocoder.yudao.module.iot.framework.questdb.core;

import cn.hutool.core.util.StrUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * QuestDB 工具类
 *
 * @author 芋道源码
 */
public class QuestDBUtils {

    /**
     * 表不存在时，QuestDB 返回的错误消息片段，例如说 table does not exist [table=device_message]
     *
     * 注意：QuestDB 通过 PGWire 返回的 SQLState 为 00000，无法按状态码识别，只能按消息识别
     */
    private static final String MESSAGE_TABLE_NOT_EXISTS = "table does not exist";

    /**
     * 判断异常是否为“表不存在”
     *
     * 沿 cause 链查找 SQLException：Spring、MyBatis 会把 PSQLException 包装多层。
     * 列不存在（Invalid column）、类型不匹配、连接等其它错误，都返回 false，由调用方原样抛出。
     *
     * @param throwable 异常
     * @return 是否表不存在
     */
    public static boolean isTableNotExists(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = throwable; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof SQLException && StrUtil.contains(cause.getMessage(), MESSAGE_TABLE_NOT_EXISTS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 毫秒转换为 QuestDB TIMESTAMP 的微秒
     *
     * @param millis 毫秒
     * @return 微秒；毫秒为空时，返回 null
     * @throws ArithmeticException 溢出时
     */
    public static Long toMicros(Long millis) {
        return millis != null ? Math.multiplyExact(millis, 1000L) : null;
    }

}
