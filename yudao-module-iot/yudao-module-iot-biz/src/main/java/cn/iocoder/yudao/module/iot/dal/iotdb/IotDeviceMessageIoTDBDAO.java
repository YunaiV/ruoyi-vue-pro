package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.framework.iotdb.core.IoTDBUtils;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.dal.iotdb.IotDeviceMessageIoTDBCodec.*;

/**
 * 基于 IoTDB（表模型）的 {@link IotDeviceMessageDAO} 实现类
 *
 * 一张全局表 device_message，身份为 (device_id, time)，编解码见 {@link IotDeviceMessageIoTDBCodec}
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "iotdb")
@Slf4j
public class IotDeviceMessageIoTDBDAO implements IotDeviceMessageDAO {

    private static final String CATEGORY_TAG = "TAG";
    private static final String CATEGORY_FIELD = "FIELD";

    /**
     * 唯一的 TAG 列
     */
    private static final String COLUMN_DEVICE_ID = "device_id";
    /**
     * 消息表的列：列名 -> 数据类型。除 {@link #COLUMN_DEVICE_ID} 为 TAG 外，其它都是 FIELD
     */
    private static final Map<String, String> COLUMN_TYPES = MapUtil.builder(new LinkedHashMap<String, String>())
            .put(COLUMN_DEVICE_ID, "STRING").put("id", "STRING")
            .put("upstream", "INT32").put("reply", "INT32")
            .put("method", "STRING").put("identifier", "STRING").put("request_id", "STRING").put("server_id", "STRING")
            .put("params", "STRING").put("data", "STRING").put("msg", "STRING")
            .put("report_time", "STRING").put("tenant_id", "STRING").put("code", "STRING")
            .put("raw", "STRING")
            .build();

    @Resource
    private IotDeviceMessageIoTDBMapper deviceMessageMapper;
    @Resource
    private IoTDBSystemMapper systemMapper;

    @Override
    public void initialize() {
        // 1. 校验前提条件：只读取配置，不修改
        systemMapper.verifyPrerequisites();
        // 2. 获得消息表的列：表不存在时，创建后再读取
        List<Map<String, Object>> columns = getColumnList();
        if (columns == null) {
            deviceMessageMapper.createTable();
            log.info("[initialize][设备消息表不存在，创建成功]");
            columns = deviceMessageMapper.selectColumnList();
        }
        // 3. 校验表结构：不兼容时，需要人工处理，不会自动修改、删除
        validateColumns(columns);
        log.info("[initialize][设备消息表已就绪]");
    }

    /**
     * 获得消息表的列
     *
     * @return 列列表；表不存在时，返回 null
     */
    private List<Map<String, Object>> getColumnList() {
        try {
            return deviceMessageMapper.selectColumnList();
        } catch (Exception exception) {
            // 只有表不存在（550）返回 null；权限、连接等其它错误原样抛出
            if (IoTDBUtils.isTableNotExists(exception)) {
                return null;
            }
            throw exception;
        }
    }

    /**
     * 校验消息表的结构：device_id 为唯一的 TAG，其它列都是类型一致的 FIELD
     *
     * 例如说，旧结构的 id 为 TAG，身份不同，且每条消息会产生一个 IoTDB 设备，所以不兼容
     *
     * @param columns DESC 的结果，包括 ColumnName、DataType、Category
     */
    private static void validateColumns(List<Map<String, Object>> columns) {
        // 1. 解析 DB 里的列：列名 -> 类别、类型
        Map<String, String> categories = new HashMap<>();
        Map<String, String> types = new HashMap<>();
        for (Map<String, Object> column : columns) {
            String name = StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "ColumnName"));
            categories.put(name, StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "Category")));
            types.put(name, StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "DataType")));
        }

        // 2.1 TAG 只能是 device_id：多出的 TAG 会改变身份
        List<String> errors = new ArrayList<>();
        Set<String> tags = new TreeSet<>();
        categories.forEach((name, category) -> {
            if (StrUtil.equalsIgnoreCase(category, CATEGORY_TAG)) {
                tags.add(name);
            }
        });
        if (!tags.equals(Collections.singleton(COLUMN_DEVICE_ID))) {
            errors.add(StrUtil.format("TAG 只能为 {}，当前为 {}", COLUMN_DEVICE_ID, tags));
        }
        // 2.2 各列的类别、类型
        COLUMN_TYPES.forEach((name, type) -> {
            if (!categories.containsKey(name)) {
                errors.add(StrUtil.format("缺少列 {}", name));
                return;
            }
            String category = COLUMN_DEVICE_ID.equals(name) ? CATEGORY_TAG : CATEGORY_FIELD;
            if (!StrUtil.equalsIgnoreCase(categories.get(name), category)
                    || !StrUtil.equalsIgnoreCase(types.get(name), type)) {
                errors.add(StrUtil.format("列 {} 应为 {} {}，当前为 {} {}",
                        name, type, category, types.get(name), categories.get(name)));
            }
        });
        if (errors.isEmpty()) {
            return;
        }
        throw new IllegalStateException(StrUtil.format("[IoTDB 设备消息表] device_message 的结构与当前版本不兼容：{}。" +
                "请人工处理（例如说，导出需要保留的数据后删除该表，或者更换数据库），应用不会自动修改、删除表",
                String.join("；", errors)));
    }

    @Override
    public void insert(IotDeviceMessageDO message) {
        deviceMessageMapper.insert(encode(message));
    }

    @Override
    public PageResult<IotDeviceMessageDO> selectPage(IotDeviceMessagePageReqVO reqVO) {
        // 1. 编码查询条件：空字符串与 TDengine 保持一致，视为不过滤
        String deviceId = encodeDeviceId(reqVO.getDeviceId());
        String method = StrUtil.isNotEmpty(reqVO.getMethod()) ? encodeString(reqVO.getMethod()) : null;
        Integer upstream = reqVO.getUpstream() != null ? encodeBoolean(reqVO.getUpstream()) : null;
        Integer reply = reqVO.getReply() != null ? encodeBoolean(reqVO.getReply()) : null;
        String identifier = StrUtil.isNotEmpty(reqVO.getIdentifier()) ? encodeString(reqVO.getIdentifier()) : null;
        // 是否有原始帧：true 为 v: 开头；false 为 n:
        Boolean raw = reqVO.getRaw();
        String rawValuePattern = PREFIX_VALUE + "%";
        try {
            // 2.1 查询总数
            Long total = deviceMessageMapper.selectCount(deviceId, method, upstream, reply, identifier,
                    raw, PREFIX_NULL, rawValuePattern);
            if (total == null || total == 0) {
                return PageResult.empty();
            }
            // 2.2 查询列表：pageSize 为 -1 时，不分页
            boolean pageable = ObjUtil.notEqual(reqVO.getPageSize(), PageParam.PAGE_SIZE_NONE);
            List<IotDeviceMessageIoTDBRow> rows = deviceMessageMapper.selectList(deviceId, method, upstream, reply, identifier,
                    raw, PREFIX_NULL, rawValuePattern,
                    pageable ? PageUtils.getStart(reqVO) : null, pageable ? reqVO.getPageSize() : null);
            return new PageResult<>(convertList(rows, IotDeviceMessageIoTDBCodec::decode), total);
        } catch (Exception exception) {
            if (IoTDBUtils.isTableNotExists(exception)) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> selectListByRequestIdsAndReply(Long deviceId, Collection<String> requestIds, Boolean reply) {
        // reply 为空时，与 TDengine 的 reply = NULL 一致，匹配不到任何消息
        if (reply == null) {
            return Collections.emptyList();
        }
        // requestId 为空的消息，存储值为 n:，不应被 null 匹配到，所以过滤掉
        List<String> encodedRequestIds = new ArrayList<>();
        if (requestIds != null) {
            requestIds.stream().filter(Objects::nonNull).distinct()
                    .forEach(requestId -> encodedRequestIds.add(encodeString(requestId)));
        }
        if (CollUtil.isEmpty(encodedRequestIds)) {
            return Collections.emptyList();
        }
        List<IotDeviceMessageIoTDBRow> rows = deviceMessageMapper.selectListByRequestIdsAndReply(
                encodeDeviceId(deviceId), encodedRequestIds, encodeBoolean(reply));
        return convertList(rows, IotDeviceMessageIoTDBCodec::decode);
    }

    @Override
    public Long selectCountByCreateTime(Long createTime) {
        return deviceMessageMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public List<Map<String, Object>> selectDeviceMessageCountGroupByDate(Long startTime, Long endTime) {
        List<Map<String, Object>> counts = deviceMessageMapper.selectCountGroupByHour(startTime, endTime);
        return convertList(counts, count -> IotDeviceMessageDAO.buildMessageCount(
                IoTDBUtils.getIgnoreCase(count, COUNT_TIME),
                IoTDBUtils.getIgnoreCase(count, COUNT_UPSTREAM),
                IoTDBUtils.getIgnoreCase(count, COUNT_DOWNSTREAM)));
    }

}
