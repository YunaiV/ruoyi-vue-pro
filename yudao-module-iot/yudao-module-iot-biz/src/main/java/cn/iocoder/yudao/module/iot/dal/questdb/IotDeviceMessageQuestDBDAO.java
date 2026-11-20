package cn.iocoder.yudao.module.iot.dal.questdb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.framework.questdb.core.QuestDBUtils;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于 QuestDB 的 {@link IotDeviceMessageDAO} 实现类
 *
 * 一张全局表 device_message，身份为 (ts, device_id, id)，与 TDengine 一致，由 DEDUP UPSERT KEYS 保证。
 * 写入经 WAL 异步应用：写入成功表示已接收，不表示立即可查；表被挂起时写入仍然成功，需要运维检查 wal_tables()
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "questdb")
@Slf4j
public class IotDeviceMessageQuestDBDAO implements IotDeviceMessageDAO {

    /**
     * 消息表的列：列名 -> 数据类型
     */
    private static final Map<String, String> COLUMN_TYPES = MapUtil.builder(new LinkedHashMap<String, String>())
            .put("ts", "TIMESTAMP").put("device_id", "LONG").put("id", "VARCHAR")
            .put("report_time", "LONG").put("tenant_id", "LONG").put("server_id", "VARCHAR")
            .put("upstream", "BOOLEAN").put("reply", "BOOLEAN").put("identifier", "VARCHAR")
            .put("request_id", "VARCHAR").put("method", "VARCHAR").put("params", "VARCHAR")
            .put("data", "VARCHAR").put("code", "LONG").put("msg", "VARCHAR").put("raw", "VARCHAR")
            .build();
    /**
     * DEDUP UPSERT KEYS 的列
     */
    private static final Set<String> UPSERT_KEYS = new TreeSet<>(Arrays.asList("ts", "device_id", "id"));
    /**
     * 建表语句中的 DEDUP 模式：只接受 UPSERT（后写覆盖）；SKIP 会保留先写的一行
     */
    private static final String DEDUP_UPSERT = "DEDUP UPSERT KEYS";

    @Resource
    private IotDeviceMessageQuestDBMapper deviceMessageMapper;

    @Override
    public void initialize() {
        // 1. 创建消息表：CREATE IF NOT EXISTS 不会检查已有表的结构
        deviceMessageMapper.createTable();
        // 2. 校验表结构：不兼容时，需要人工处理，不会自动修改、删除
        validateTable(deviceMessageMapper.selectTable(), deviceMessageMapper.selectColumnList(),
                deviceMessageMapper.selectCreateTable());
        log.info("[initialize][设备消息表已就绪]");
    }

    /**
     * 校验消息表的结构：列名与类型、指定时间戳、WAL、DEDUP UPSERT KEYS
     *
     * @param table       tables() 的结果
     * @param columns     table_columns() 的结果
     * @param createTable SHOW CREATE TABLE 的结果，用于区分 DEDUP UPSERT 与 SKIP
     */
    private static void validateTable(Map<String, Object> table, List<Map<String, Object>> columns, String createTable) {
        List<String> errors = new ArrayList<>();
        // 1. 表属性
        if (table == null || !"ts".equals(table.get("designatedTimestamp"))
                || !Boolean.TRUE.equals(table.get("walEnabled")) || !Boolean.TRUE.equals(table.get("dedup"))) {
            errors.add(StrUtil.format("需要指定时间戳为 ts，并开启 WAL、DEDUP，当前为 {}", String.valueOf(table)));
        }
        // 2. 各列的类型、DEDUP 键
        Map<String, String> types = new HashMap<>();
        Set<String> upsertKeys = new TreeSet<>();
        for (Map<String, Object> column : columns) {
            String name = StrUtil.toStringOrNull(column.get("column"));
            types.put(name, StrUtil.toStringOrNull(column.get("type")));
            if (Boolean.TRUE.equals(column.get("upsertKey"))) {
                upsertKeys.add(name);
            }
        }
        COLUMN_TYPES.forEach((name, type) -> {
            if (!StrUtil.equalsIgnoreCase(types.get(name), type)) {
                errors.add(StrUtil.format("列 {} 应为 {}，当前为 {}", name, type, types.get(name)));
            }
        });
        if (!upsertKeys.equals(UPSERT_KEYS)) {
            errors.add(StrUtil.format("DEDUP UPSERT KEYS 应为 {}，当前为 {}", UPSERT_KEYS, upsertKeys));
        }
        // 3. DEDUP 模式：只接受 UPSERT
        if (!StrUtil.contains(createTable, DEDUP_UPSERT)) {
            errors.add(StrUtil.format("DEDUP 模式应为 UPSERT（同键后写覆盖），当前建表语句为 {}", createTable));
        }
        if (errors.isEmpty()) {
            return;
        }
        throw new IllegalStateException(StrUtil.format("[QuestDB 设备消息表] device_message 的结构与当前版本不兼容：{}。" +
                "请人工处理（例如说，导出需要保留的数据后删除该表，或者更换实例），应用不会自动修改、删除表",
                String.join("；", errors)));
    }

    /**
     * 写入消息。失败时向上抛出，由 Service 的异步写日志方法捕获并只记录日志（IotDeviceMessageServiceImpl#createDeviceLogAsync）
     */
    @Override
    public void insert(IotDeviceMessageDO message) {
        // BOOLEAN 不能为 NULL：为空时拒绝写入，避免被存成 false
        Assert.notNull(message.getTs(), "消息的 ts 不能为空");
        Assert.notNull(message.getDeviceId(), "消息的 deviceId 不能为空");
        Assert.notEmpty(message.getId(), "消息的 id 不能为空");
        Assert.notNull(message.getUpstream(), "消息的 upstream 不能为空");
        Assert.notNull(message.getReply(), "消息的 reply 不能为空");
        deviceMessageMapper.insert(message, QuestDBUtils.toMicros(message.getTs()),
                toJsonString(message.getParams()), toJsonString(message.getData()),
                message.getCode() != null ? message.getCode().longValue() : null);
    }

    /**
     * 转换 params、data 为字符串：Service 写入前已转为 JSON 字符串，这里兼容其它对象
     */
    private static String toJsonString(Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        return JsonUtils.toJsonString(value);
    }

    @Override
    public PageResult<IotDeviceMessageDO> selectPage(IotDeviceMessagePageReqVO reqVO) {
        try {
            // 1. 查询总数
            Long total = deviceMessageMapper.selectCount(reqVO);
            if (total == null || total == 0) {
                return PageResult.empty();
            }
            // 2.1 情况一：pageSize 为 -1 时，不分页
            if (ObjUtil.equal(reqVO.getPageSize(), PageParam.PAGE_SIZE_NONE)) {
                return new PageResult<>(deviceMessageMapper.selectList(reqVO, null, null), total);
            }
            // 2.2 情况二：分页。按 long 计算偏移，避免 pageNo 很大时 int 溢出；超出总数时不再查询
            long lo = Math.multiplyExact((long) (reqVO.getPageNo() - 1), reqVO.getPageSize());
            if (lo >= total) {
                return new PageResult<>(Collections.emptyList(), total);
            }
            return new PageResult<>(deviceMessageMapper.selectList(reqVO, lo, lo + reqVO.getPageSize()), total);
        } catch (Exception exception) {
            if (QuestDBUtils.isTableNotExists(exception)) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> selectListByRequestIdsAndReply(Long deviceId, Collection<String> requestIds, Boolean reply) {
        // reply 为空时，与 TDengine 的 reply = NULL 一致，匹配不到任何消息，无需查询
        if (reply == null || requestIds == null) {
            return Collections.emptyList();
        }
        // 与 TDengine、IoTDB 保持一致：忽略 null、去重，避免 IN 列表中绑定 NULL
        List<String> validRequestIds = requestIds.stream().filter(Objects::nonNull).distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validRequestIds)) {
            return Collections.emptyList();
        }
        return deviceMessageMapper.selectListByRequestIdsAndReply(deviceId, validRequestIds, reply);
    }

    @Override
    public Long selectCountByCreateTime(Long createTime) {
        return deviceMessageMapper.selectCountByCreateTime(QuestDBUtils.toMicros(createTime));
    }

    @Override
    public List<Map<String, Object>> selectDeviceMessageCountGroupByDate(Long startTime, Long endTime) {
        List<Map<String, Object>> counts = deviceMessageMapper.selectCountGroupByHour(
                QuestDBUtils.toMicros(startTime), QuestDBUtils.toMicros(endTime));
        return convertList(counts, count -> IotDeviceMessageDAO.buildMessageCount(
                count.get(COUNT_TIME), count.get(COUNT_UPSTREAM), count.get(COUNT_DOWNSTREAM)));
    }

}
