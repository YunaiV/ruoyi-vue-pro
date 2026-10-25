package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.dal.storage.IotDevicePropertyDAO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import cn.iocoder.yudao.module.iot.framework.storage.core.util.IotStorageFieldUtils;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于 TDengine 的 {@link IotDevicePropertyDAO} 实现类
 *
 * 每个产品一张超级表 product_property_{productId}，每个设备一张子表 device_property_{deviceId}
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "tdengine", matchIfMissing = true)
@Slf4j
public class IotDevicePropertyTDengineDAO implements IotDevicePropertyDAO {

    /**
     * 物模型的数据类型，与 TDengine 数据类型的映射关系
     *
     * @see <a href="https://docs.taosdata.com/reference/taos-sql/data-type/">TDEngine 数据类型</a>
     */
    private static final Map<String, String> TYPE_MAPPING = MapUtil.<String, String>builder()
            .put(IotDataSpecsDataTypeEnum.INT.getDataType(), TDengineTableField.TYPE_INT)
            .put(IotDataSpecsDataTypeEnum.FLOAT.getDataType(), TDengineTableField.TYPE_FLOAT)
            .put(IotDataSpecsDataTypeEnum.DOUBLE.getDataType(), TDengineTableField.TYPE_DOUBLE)
            .put(IotDataSpecsDataTypeEnum.ENUM.getDataType(), TDengineTableField.TYPE_TINYINT)
            .put(IotDataSpecsDataTypeEnum.BOOL.getDataType(), TDengineTableField.TYPE_TINYINT)
            .put(IotDataSpecsDataTypeEnum.TEXT.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.DATE.getDataType(), TDengineTableField.TYPE_TIMESTAMP)
            .put(IotDataSpecsDataTypeEnum.STRUCT.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.ARRAY.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .build();

    @Resource
    private IotDevicePropertyMapper devicePropertyMapper;

    @Override
    public void initialize() {
        // 产品属性表在产品发布、手动同步时创建，启动时无需初始化
    }

    @Override
    public void defineProductPropertyTable(Long productId, List<IotThingModelDO> propertyThingModels) {
        // 1.1 校验属性标识符，可以作为字段名
        IotStorageFieldUtils.validatePropertyIdentifiers(convertList(propertyThingModels, IotThingModelDO::getIdentifier));
        // 1.2 解析 DB 里的字段
        List<TDengineTableField> oldFields = new ArrayList<>();
        try {
            oldFields.addAll(devicePropertyMapper.getProductPropertySTableFieldList(productId));
        } catch (Exception e) {
            if (!isTableNotExists(e)) {
                throw e;
            }
        }

        // 2.1 情况一：如果是新增的时候，需要创建表
        List<TDengineTableField> newFields = buildTableFieldList(propertyThingModels);
        if (CollUtil.isEmpty(oldFields)) {
            if (CollUtil.isEmpty(newFields)) {
                log.info("[defineProductPropertyTable][productId({}) 没有需要定义的属性]", productId);
                return;
            }
            devicePropertyMapper.createProductPropertySTable(productId, newFields);
            return;
        }
        // 2.2 情况二：如果是修改的时候，需要更新表
        devicePropertyMapper.alterProductPropertySTable(productId, oldFields, newFields);
    }

    private List<TDengineTableField> buildTableFieldList(List<IotThingModelDO> thingModels) {
        return convertList(thingModels, thingModel -> {
            TDengineTableField field = new TDengineTableField(
                    IotStorageFieldUtils.buildFieldName(thingModel.getIdentifier()),
                    TYPE_MAPPING.get(thingModel.getProperty().getDataType()));
            String dataType = thingModel.getProperty().getDataType();
            if (Objects.equals(dataType, IotDataSpecsDataTypeEnum.TEXT.getDataType())) {
                field.setLength(((ThingModelDateOrTextDataSpecs) thingModel.getProperty().getDataSpecs()).getLength());
            } else if (ObjectUtils.equalsAny(dataType, IotDataSpecsDataTypeEnum.STRUCT.getDataType(),
                    IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
                field.setLength(ThingModelProperty.LENGTH_JSON);
            }
            return field;
        });
    }

    @Override
    public void deleteProductPropertyTable(Long productId) {
        // 删除超级表时，TDengine 会一并删除其下的设备子表 device_property_{deviceId}
        devicePropertyMapper.dropProductPropertySTable(productId);
    }

    @Override
    public void insert(IotDeviceDO device, Map<String, Object> properties, Long ts, Long reportTime) {
        // 校验属性标识符：在拼接 SQL 之前，避免保留字段、归一化后重名的属性写入
        IotStorageFieldUtils.validatePropertyIdentifiers(properties.keySet());
        devicePropertyMapper.insert(device, properties, ts, reportTime);
    }

    @Override
    public List<IotDevicePropertyRespVO> selectHistoryList(Long productId, IotDevicePropertyHistoryListReqVO reqVO) {
        try {
            return devicePropertyMapper.selectListByHistory(reqVO);
        } catch (Exception exception) {
            if (isTableNotExists(exception)) {
                return Collections.emptyList();
            }
            throw exception;
        }
    }

    private static boolean isTableNotExists(Exception exception) {
        return StrUtil.contains(exception.getMessage(), "Table does not exist");
    }

}
