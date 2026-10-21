package cn.iocoder.yudao.module.iot.dal.iotdb;

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
import cn.iocoder.yudao.module.iot.framework.iotdb.core.IoTDBUtils;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import cn.iocoder.yudao.module.iot.framework.storage.core.util.IotStorageFieldUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于 IoTDB（表模型）的 {@link IotDevicePropertyDAO} 实现类
 *
 * 每个产品一张表 product_property_{productId}，设备共用产品表，通过 device_id TAG 区分。
 * 用户属性列统一为 p_ + 中立字段名，避免与 time、device_id、report_time 等内置列冲突。
 *
 * 与 TDengine 的差异：
 * 1. STRING 没有长度，物模型 TEXT 的长度变化不会改表；长度由写入前的业务转换校验
 * 2. INT、ENUM、BOOL 都映射为 INT32，三者之间的逻辑类型变化不会改表，历史数据保留
 * 3. 物理类型变化通过 DROP + ADD 完成，旧列的历史数据不会保留
 * 4. IoTDB 的 DDL 不参与事务，也无法回滚；执行中途失败时，再次定义即可收敛（ADD/DROP 均为幂等语句）
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "iotdb")
@Slf4j
public class IotDevicePropertyIoTDBDAO implements IotDevicePropertyDAO {

    /**
     * 用户属性列的前缀
     */
    public static final String COLUMN_PREFIX = "p_";

    public static final String TYPE_INT32 = "INT32";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";

    /**
     * DESC 结果中，FIELD 列的类别
     */
    private static final String CATEGORY_FIELD = "FIELD";

    /**
     * 物模型的数据类型，与 IoTDB 数据类型的映射关系
     *
     * @see <a href="https://iotdb.apache.org/zh/UserGuide/latest-Table/Background-knowledge/Data-Type.html">IoTDB 数据类型</a>
     */
    private static final Map<String, String> TYPE_MAPPING = MapUtil.<String, String>builder()
            .put(IotDataSpecsDataTypeEnum.INT.getDataType(), TYPE_INT32)
            .put(IotDataSpecsDataTypeEnum.FLOAT.getDataType(), TYPE_FLOAT)
            .put(IotDataSpecsDataTypeEnum.DOUBLE.getDataType(), TYPE_DOUBLE)
            .put(IotDataSpecsDataTypeEnum.ENUM.getDataType(), TYPE_INT32)
            .put(IotDataSpecsDataTypeEnum.BOOL.getDataType(), TYPE_INT32)
            .put(IotDataSpecsDataTypeEnum.TEXT.getDataType(), TYPE_STRING)
            .put(IotDataSpecsDataTypeEnum.DATE.getDataType(), TYPE_TIMESTAMP)
            .put(IotDataSpecsDataTypeEnum.STRUCT.getDataType(), TYPE_STRING)
            .put(IotDataSpecsDataTypeEnum.ARRAY.getDataType(), TYPE_STRING)
            .build();

    @Resource
    private IotDevicePropertyIoTDBMapper devicePropertyMapper;
    @Resource
    private IoTDBSystemMapper systemMapper;

    @Override
    public void initialize() {
        // 产品属性表在产品发布、手动同步时创建，启动时只校验前提条件
        systemMapper.verifyPrerequisites();
    }

    @Override
    public void defineProductPropertyTable(Long productId, List<IotThingModelDO> propertyThingModels) {
        // 1.1 校验属性标识符，可以作为列名
        IotStorageFieldUtils.validatePropertyIdentifiers(convertList(propertyThingModels, IotThingModelDO::getIdentifier));
        List<IotDevicePropertyIoTDBMapper.Column> newColumns = buildColumnList(propertyThingModels);
        // 1.2 解析 DB 里的用户属性列
        Map<String, String> oldColumns = getUserColumnTypes(productId);

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
        Set<String> newColumnNames = new HashSet<>(convertList(newColumns, IotDevicePropertyIoTDBMapper.Column::getName));
        // 2.2.1 删除不再需要的列
        oldColumns.keySet().stream().filter(name -> !newColumnNames.contains(name))
                .forEach(name -> devicePropertyMapper.dropColumn(productId, name));
        for (IotDevicePropertyIoTDBMapper.Column column : newColumns) {
            String oldType = oldColumns.get(column.getName());
            // 2.2.2 新增的列
            if (oldType == null) {
                devicePropertyMapper.addColumn(productId, column);
                continue;
            }
            // 2.2.3 物理类型变化的列：IoTDB 不支持修改列类型，只能 DROP + ADD，旧列的数据不会保留
            if (!StrUtil.equalsIgnoreCase(oldType, column.getType())) {
                log.warn("[defineProductPropertyTable][productId({}) 列({}) 类型从 {} 变为 {}，删除后重建，历史数据不保留]",
                        productId, column.getName(), oldType, column.getType());
                devicePropertyMapper.dropColumn(productId, column.getName());
                devicePropertyMapper.addColumn(productId, column);
            }
        }
    }

    /**
     * 获得产品属性表的用户属性列
     *
     * @param productId 产品编号
     * @return 列名与类型的映射；表不存在时，返回 null
     */
    private Map<String, String> getUserColumnTypes(Long productId) {
        List<Map<String, Object>> columns;
        try {
            columns = devicePropertyMapper.selectColumnList(productId);
        } catch (Exception exception) {
            if (IoTDBUtils.isTableNotExists(exception)) {
                return null;
            }
            throw exception;
        }
        // 只比较 p_ 开头的 FIELD 列：time、device_id、report_time 由系统维护，不参与差异
        Map<String, String> userColumns = new LinkedHashMap<>();
        for (Map<String, Object> column : columns) {
            String name = StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "ColumnName"));
            String category = StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "Category"));
            if (StrUtil.startWith(name, COLUMN_PREFIX) && StrUtil.equalsIgnoreCase(category, CATEGORY_FIELD)) {
                userColumns.put(name, StrUtil.toStringOrNull(IoTDBUtils.getIgnoreCase(column, "DataType")));
            }
        }
        return userColumns;
    }

    private List<IotDevicePropertyIoTDBMapper.Column> buildColumnList(List<IotThingModelDO> thingModels) {
        return convertList(thingModels, thingModel -> {
            String type = TYPE_MAPPING.get(thingModel.getProperty().getDataType());
            if (type == null) {
                throw new IllegalArgumentException(StrUtil.format("属性({}) 的数据类型({}) 不支持存储",
                        thingModel.getIdentifier(), thingModel.getProperty().getDataType()));
            }
            return new IotDevicePropertyIoTDBMapper.Column(buildColumnName(thingModel.getIdentifier()), type, null);
        });
    }

    @Override
    public void insert(IotDeviceDO device, Map<String, Object> properties, Long ts, Long reportTime) {
        // 校验属性标识符：在拼接 SQL 之前，避免保留字段、归一化后重名的属性写入
        IotStorageFieldUtils.validatePropertyIdentifiers(properties.keySet());
        if (CollUtil.isEmpty(properties)) {
            return;
        }
        List<IotDevicePropertyIoTDBMapper.Column> columns = new ArrayList<>(properties.size());
        properties.forEach((identifier, value) -> columns.add(
                new IotDevicePropertyIoTDBMapper.Column(buildColumnName(identifier), null, convertValue(value))));
        devicePropertyMapper.insert(device.getProductId(), String.valueOf(device.getId()), ts, reportTime, columns);
    }

    /**
     * 转换属性值为 IoTDB 驱动支持的类型
     *
     * 原因：ENUM、BOOL 业务转换后为 Byte（对应 TDengine 的 TINYINT），而 IoTDB 驱动不支持 setByte，需要转为 INT32 对应的 Integer
     */
    private static Object convertValue(Object value) {
        if (value instanceof Byte) {
            return ((Byte) value).intValue();
        }
        return value;
    }

    @Override
    public List<IotDevicePropertyRespVO> selectHistoryList(Long productId, IotDevicePropertyHistoryListReqVO reqVO) {
        try {
            return devicePropertyMapper.selectHistoryList(productId, buildColumnName(reqVO.getIdentifier()),
                    String.valueOf(reqVO.getDeviceId()),
                    LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[0]),
                    LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[1]));
        } catch (Exception exception) {
            // 只有表不存在（产品尚未定义属性）返回空；列不存在等其它错误原样抛出
            if (IoTDBUtils.isTableNotExists(exception)) {
                return Collections.emptyList();
            }
            throw exception;
        }
    }

    /**
     * 构建属性列名：p_ + 中立字段名
     *
     * @param identifier 属性标识符，调用前需已通过 {@link IotStorageFieldUtils#validatePropertyIdentifiers(Collection)} 校验
     * @return 列名
     */
    public static String buildColumnName(String identifier) {
        if (!IotStorageFieldUtils.isPropertyFieldName(identifier)) {
            throw new IllegalArgumentException(StrUtil.format("属性标识符({}) 不能作为存储列", identifier));
        }
        return COLUMN_PREFIX + IotStorageFieldUtils.buildFieldName(identifier);
    }

}
