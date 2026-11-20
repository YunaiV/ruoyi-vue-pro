package cn.iocoder.yudao.module.iot.dal.questdb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDevicePropertyDAO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.framework.questdb.core.QuestDBUtils;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import cn.iocoder.yudao.module.iot.framework.storage.core.util.IotStorageFieldUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于 QuestDB 的 {@link IotDevicePropertyDAO} 实现类
 *
 * 每个产品一张表 product_property_{productId}，设备共用产品表，通过 device_id 列区分。
 * 用户属性列为 p_ + 中立字段名；DATE 属性为 d_ + 中立字段名，前缀表示存储编码，DATE 与其它类型互相变化时，列名随之变化。
 *
 * 与 TDengine、IoTDB 的差异：
 * 1. INT、ENUM、BOOL 都映射为 LONG：QuestDB 的 INT 以 Integer.MIN_VALUE 表示 NULL，存 INT 会丢失这个合法值；读出时转回 Integer
 * 2. DATE 映射为 VARCHAR，存十进制毫秒：LONG 以 Long.MIN_VALUE 表示 NULL；读出时转回 Long
 * 3. 不开启 DEDUP：同一设备同一毫秒的两次局部上报，两行都保留，避免整行替换清空对方的列
 * 4. DDL 不参与事务（ROLLBACK 不会撤销），ALTER 异步应用；执行中途失败时，再次定义即可收敛
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "questdb")
@Slf4j
public class IotDevicePropertyQuestDBDAO implements IotDevicePropertyDAO {

    /**
     * 用户属性列的前缀
     */
    public static final String COLUMN_PREFIX = "p_";
    /**
     * DATE 属性列的前缀
     */
    public static final String COLUMN_PREFIX_DATE = "d_";

    public static final String TYPE_LONG = "LONG";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_VARCHAR = "VARCHAR";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";

    /**
     * 物模型的数据类型，与 QuestDB 数据类型的映射关系。DATE 单独处理，见 {@link #buildColumn(IotThingModelDO)}
     *
     * @see <a href="https://questdb.com/docs/reference/sql/datatypes/">QuestDB 数据类型</a>
     */
    private static final Map<String, String> TYPE_MAPPING = MapUtil.<String, String>builder()
            .put(IotDataSpecsDataTypeEnum.INT.getDataType(), TYPE_LONG)
            .put(IotDataSpecsDataTypeEnum.FLOAT.getDataType(), TYPE_FLOAT)
            .put(IotDataSpecsDataTypeEnum.DOUBLE.getDataType(), TYPE_DOUBLE)
            .put(IotDataSpecsDataTypeEnum.ENUM.getDataType(), TYPE_LONG)
            .put(IotDataSpecsDataTypeEnum.BOOL.getDataType(), TYPE_LONG)
            .put(IotDataSpecsDataTypeEnum.TEXT.getDataType(), TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.DATE.getDataType(), TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.STRUCT.getDataType(), TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.ARRAY.getDataType(), TYPE_VARCHAR)
            .build();

    /**
     * 系统列：列名 -> 类型。ts 为指定时间戳
     */
    private static final Map<String, String> SYSTEM_COLUMNS = MapUtil.<String, String>builder()
            .put(IotStorageFieldUtils.FIELD_TS, TYPE_TIMESTAMP)
            .put(IotStorageFieldUtils.FIELD_DEVICE_ID, TYPE_LONG)
            .put(IotStorageFieldUtils.FIELD_REPORT_TIME, TYPE_LONG)
            .build();

    @Resource
    private IotDevicePropertyQuestDBMapper devicePropertyMapper;

    @Override
    public void initialize() {
        // 产品属性表在产品发布、手动同步时创建，启动时无需处理
    }

    @Override
    public void defineProductPropertyTable(Long productId, List<IotThingModelDO> propertyThingModels) {
        // 1.1 校验属性标识符，可以作为列名
        IotStorageFieldUtils.validatePropertyIdentifiers(convertList(propertyThingModels, IotThingModelDO::getIdentifier));
        List<IotDevicePropertyQuestDBMapper.Column> newColumns = convertList(propertyThingModels, IotDevicePropertyQuestDBDAO::buildColumn);
        // 1.2 解析 DB 里的用户属性列：先等待已提交的 ALTER 应用完成，避免读到旧结构；并核验已有表的属性
        Map<String, String> oldColumns = getUserColumnTypes(productId, true);

        // 2.1 情况一：如果是新增的时候，需要创建表
        if (oldColumns == null) {
            if (CollUtil.isEmpty(newColumns)) {
                log.info("[defineProductPropertyTable][productId({}) 没有需要定义的属性]", productId);
                return;
            }
            devicePropertyMapper.createTable(productId, newColumns);
            return;
        }
        // 2.2 情况二：如果是修改的时候，需要更新表
        Set<String> newColumnNames = new HashSet<>(convertList(newColumns, IotDevicePropertyQuestDBMapper.Column::getName));
        // 2.2.1 删除不再需要的列（包括 DATE 与其它类型互相变化后，前缀不同的旧列）
        oldColumns.keySet().stream().filter(name -> !newColumnNames.contains(name))
                .forEach(name -> devicePropertyMapper.dropColumn(productId, name));
        for (IotDevicePropertyQuestDBMapper.Column column : newColumns) {
            String oldType = oldColumns.get(column.getName());
            // 2.2.2 新增的列
            if (oldType == null) {
                devicePropertyMapper.addColumn(productId, column);
                continue;
            }
            // 2.2.3 物理类型变化的列：与 TDengine、IoTDB 保持一致，DROP + ADD，旧列的数据不会保留
            if (!StrUtil.equalsIgnoreCase(oldType, column.getType())) {
                log.warn("[defineProductPropertyTable][productId({}) 列({}) 类型从 {} 变为 {}，删除后重建，历史数据不保留]",
                        productId, column.getName(), oldType, column.getType());
                devicePropertyMapper.dropColumn(productId, column.getName());
                devicePropertyMapper.addColumn(productId, column);
            }
        }
    }

    /**
     * 获得产品属性表的用户属性列，并校验系统列
     *
     * @param productId 产品编号
     * @param define    是否为定义表结构：是时，先等待 WAL 应用完成，并核验表属性；历史查询时不做，避免增加固定成本
     * @return 列名与类型的映射；表不存在时，返回 null
     */
    private Map<String, String> getUserColumnTypes(Long productId, boolean define) {
        List<Map<String, Object>> columns;
        try {
            if (define) {
                devicePropertyMapper.waitWalTable(productId);
            }
            columns = devicePropertyMapper.selectColumnList(productId);
            if (define) {
                validateTable(productId, devicePropertyMapper.selectTable(productId));
            }
        } catch (Exception exception) {
            if (QuestDBUtils.isTableNotExists(exception)) {
                return null;
            }
            throw exception;
        }
        Map<String, String> userColumns = new LinkedHashMap<>();
        Set<String> systemColumns = new HashSet<>();
        for (Map<String, Object> column : columns) {
            String name = StrUtil.toStringOrNull(column.get("column"));
            String type = StrUtil.toStringOrNull(column.get("type"));
            if (SYSTEM_COLUMNS.containsKey(name)) {
                // 系统列：类型一致，且只有 ts 为指定时间戳
                boolean designated = Boolean.TRUE.equals(column.get("designated"));
                if (StrUtil.equalsIgnoreCase(type, SYSTEM_COLUMNS.get(name))
                        && designated == IotStorageFieldUtils.FIELD_TS.equals(name)) {
                    systemColumns.add(name);
                }
            } else if (StrUtil.startWithAny(name, COLUMN_PREFIX, COLUMN_PREFIX_DATE)) {
                userColumns.put(name, type);
            }
        }
        if (systemColumns.size() != SYSTEM_COLUMNS.size()) {
            throw new IllegalStateException(StrUtil.format("[QuestDB 属性表] product_property_{} 的系统列与当前版本不兼容，" +
                    "需要 {}（ts 为指定时间戳），当前为 {}。请人工处理，应用不会自动修改、删除表", productId, SYSTEM_COLUMNS, columns));
        }
        return userColumns;
    }

    /**
     * 校验属性表的属性：开启 WAL，且没有 DEDUP。
     * 原因：DEDUP 会把同一设备同一毫秒的两次局部上报整行合并，静默丢失属性
     *
     * @param productId 产品编号
     * @param table     tables() 的结果
     */
    private static void validateTable(Long productId, Map<String, Object> table) {
        if (table != null && Boolean.TRUE.equals(table.get("walEnabled")) && Boolean.FALSE.equals(table.get("dedup"))) {
            return;
        }
        throw new IllegalStateException(StrUtil.format("[QuestDB 属性表] product_property_{} 需要开启 WAL 且不开启 DEDUP，当前为 {}。" +
                "请人工处理，应用不会自动修改、删除表", productId, String.valueOf(table)));
    }

    private static IotDevicePropertyQuestDBMapper.Column buildColumn(IotThingModelDO thingModel) {
        String dataType = thingModel.getProperty().getDataType();
        String type = TYPE_MAPPING.get(dataType);
        if (type == null) {
            throw new IllegalArgumentException(StrUtil.format("属性({}) 的数据类型({}) 不支持存储",
                    thingModel.getIdentifier(), dataType));
        }
        String name = IotDataSpecsDataTypeEnum.DATE.getDataType().equals(dataType)
                ? buildDateColumnName(thingModel.getIdentifier()) : buildColumnName(thingModel.getIdentifier());
        return new IotDevicePropertyQuestDBMapper.Column(name, type, null);
    }

    @Override
    public void deleteProductPropertyTable(Long productId) {
        // 设备共用产品表，删除产品表即删除该产品下所有设备的属性数据
        devicePropertyMapper.dropTable(productId);
    }

    /**
     * 写入设备属性。失败时向上抛出，与 TDengine、IoTDB 一致，由调用方处理
     */
    @Override
    public void insert(IotDeviceDO device, Map<String, Object> properties, Long ts, Long reportTime) {
        // 校验属性标识符：在拼接 SQL 之前，避免保留字段、归一化后重名的属性写入
        IotStorageFieldUtils.validatePropertyIdentifiers(properties.keySet());
        if (CollUtil.isEmpty(properties)) {
            return;
        }
        List<IotDevicePropertyQuestDBMapper.Column> columns = new ArrayList<>(properties.size());
        properties.forEach((identifier, value) -> columns.add(buildInsertColumn(identifier, value)));
        devicePropertyMapper.insert(device.getProductId(), device.getId(), QuestDBUtils.toMicros(ts), reportTime, columns);
    }

    /**
     * 构建写入的属性列：按业务转换后的值类型，选择列名与存储值
     *
     * 业务转换（IotThingModelServiceImpl#convertThingModelPropertyValue）的结果：
     * DATE 为 Long 毫秒；INT 为 Integer；ENUM、BOOL 为 Byte；FLOAT 为 Float；DOUBLE 为 Double；TEXT、STRUCT、ARRAY 为 String
     */
    @SuppressWarnings("PatternVariableCanBeUsed") // 保持 JDK8 兼容性
    private static IotDevicePropertyQuestDBMapper.Column buildInsertColumn(String identifier, Object value) {
        if (value instanceof Long) {
            return new IotDevicePropertyQuestDBMapper.Column(buildDateColumnName(identifier), null, value.toString());
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return new IotDevicePropertyQuestDBMapper.Column(buildColumnName(identifier), null, ((Number) value).longValue());
        }
        return new IotDevicePropertyQuestDBMapper.Column(buildColumnName(identifier), null, value);
    }

    @Override
    public List<IotDevicePropertyRespVO> selectHistoryList(Long productId, IotDevicePropertyHistoryListReqVO reqVO) {
        // 1. 按表结构确定列：表不存在时返回空；列不存在时抛出异常，与 IoTDB 的“列不存在原样抛出”一致
        Map<String, String> columns = getUserColumnTypes(productId, false);
        if (columns == null) {
            return Collections.emptyList();
        }
        String columnName = buildColumnName(reqVO.getIdentifier());
        String dateColumnName = buildDateColumnName(reqVO.getIdentifier());
        boolean date = columns.containsKey(dateColumnName);
        if (!date && !columns.containsKey(columnName)) {
            throw new IllegalArgumentException(StrUtil.format("属性({}) 在 product_property_{} 中不存在，请同步产品的属性表",
                    reqVO.getIdentifier(), productId));
        }
        String queryColumnName = date ? dateColumnName : columnName;
        boolean integer = !date && TYPE_LONG.equalsIgnoreCase(columns.get(columnName));

        // 2. 查询并解码：DATE 转回 Long；INT、ENUM、BOOL 转回 Integer
        List<IotDevicePropertyRespVO> list;
        try {
            list = devicePropertyMapper.selectHistoryList(productId, queryColumnName, reqVO.getDeviceId(),
                    QuestDBUtils.toMicros(LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[0])),
                    QuestDBUtils.toMicros(LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[1])));
        } catch (Exception exception) {
            if (QuestDBUtils.isTableNotExists(exception)) {
                return Collections.emptyList();
            }
            throw exception;
        }
        for (IotDevicePropertyRespVO item : list) {
            if (date) {
                item.setValue(Long.valueOf(item.getValue().toString()));
            } else if (integer) {
                item.setValue(Math.toIntExact(((Number) item.getValue()).longValue()));
            }
        }
        return list;
    }

    /**
     * 构建属性列名：p_ + 中立字段名
     *
     * @param identifier 属性标识符，调用前需已通过 {@link IotStorageFieldUtils#validatePropertyIdentifiers(Collection)} 校验
     * @return 列名
     */
    public static String buildColumnName(String identifier) {
        return COLUMN_PREFIX + buildFieldName(identifier);
    }

    /**
     * 构建 DATE 属性列名：d_ + 中立字段名
     */
    public static String buildDateColumnName(String identifier) {
        return COLUMN_PREFIX_DATE + buildFieldName(identifier);
    }

    private static String buildFieldName(String identifier) {
        if (!IotStorageFieldUtils.isPropertyFieldName(identifier)) {
            throw new IllegalArgumentException(StrUtil.format("属性标识符({}) 不能作为存储列", identifier));
        }
        return IotStorageFieldUtils.buildFieldName(identifier);
    }

}
