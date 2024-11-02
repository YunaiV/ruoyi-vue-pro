package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.FieldParser;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IotDbStructureDataServiceImpl implements IotDbStructureDataService {

    @Resource
    private IotTdEngineService iotTdEngineService;

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Override
    public void createSuperTable(ThingModelRespVO thingModel, Integer deviceType) {
        // 1. 解析物模型，获得字段列表
        List<TdFieldDO> schemaFields = new ArrayList<>();
        schemaFields.add(TdFieldDO.builder()
                .fieldName("time")
                .dataType("TIMESTAMP")
                .build());
        schemaFields.addAll(FieldParser.parse(thingModel));

        // 3. 设置超级表的标签
        List<TdFieldDO> tagsFields = Arrays.asList(
                TdFieldDO.builder().fieldName("product_key").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_key").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_name").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_type").dataType("INT").build()
        );

        // 4. 获取超级表的名称
        String superTableName = getProductPropertySTableName(deviceType, thingModel.getProductKey());

        // 5. 创建超级表
        String dataBaseName = getDatabaseName();
        iotTdEngineService.createSuperTable(schemaFields, tagsFields, dataBaseName, superTableName);
    }

    @Override
    public void updateSuperTable(ThingModelRespVO thingModel, Integer deviceType) {
        try {
            String tbName = getProductPropertySTableName(deviceType, thingModel.getProductKey());
            List<TdFieldDO> oldFields = getTableFields(tbName);
            List<TdFieldDO> newFields = FieldParser.parse(thingModel);

            updateTableFields(tbName, oldFields, newFields);
        } catch (Exception e) {
            log.error("更新物模型超级表失败", e);
        }
    }

    // 获取表字段
    private List<TdFieldDO> getTableFields(String tableName) {
        List<TdFieldDO> fields = new ArrayList<>();
        // 获取超级表的描述信息
        List<Map<String, Object>> maps = iotTdEngineService.describeSuperTable(getDatabaseName(), tableName);
        if (maps != null) {
            // 过滤掉 note 字段为 TAG 的记录和 time 字段
            List<Map<String, Object>> filteredMaps = maps.stream()
                    .filter(map -> !"TAG".equals(map.get("note")))
                    .filter(map -> !"time".equals(map.get("field")))
                    .toList();
            // 解析字段信息
            fields = FieldParser.parse(filteredMaps.stream()
                    .map(map -> List.of(map.get("field"), map.get("type"), map.get("length")))
                    .collect(Collectors.toList()));
        }
        return fields;
    }

    // 更新表字段
    private void updateTableFields(String tableName, List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        // 获取新增字段
        List<TdFieldDO> addFields = getAddFields(oldFields, newFields);
        // 获取修改字段
        List<TdFieldDO> modifyFields = getModifyFields(oldFields, newFields);
        // 获取删除字段
        List<TdFieldDO> dropFields = getDropFields(oldFields, newFields);

        String dataBaseName = getDatabaseName();
        // 添加新增字段
        if (CollUtil.isNotEmpty(addFields)) {
            iotTdEngineService.addColumnForSuperTable(dataBaseName, tableName, addFields);
        }
        // 删除旧字段
        if (CollUtil.isNotEmpty(dropFields)) {
            iotTdEngineService.dropColumnForSuperTable(dataBaseName, tableName, dropFields);
        }
        // 修改字段（先删除再添加）
        if (CollUtil.isNotEmpty(modifyFields)) {
            iotTdEngineService.dropColumnForSuperTable(dataBaseName, tableName, modifyFields);
            iotTdEngineService.addColumnForSuperTable(dataBaseName, tableName, modifyFields);
        }
    }

    // 获取新增字段
    private List<TdFieldDO> getAddFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Set<String> oldFieldNames = oldFields.stream()
                .map(TdFieldDO::getFieldName)
                .collect(Collectors.toSet());
        return newFields.stream()
                .filter(f -> !oldFieldNames.contains(f.getFieldName()))
                .collect(Collectors.toList());
    }

    // 获取修改字段
    private List<TdFieldDO> getModifyFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Map<String, TdFieldDO> oldFieldMap = oldFields.stream()
                .collect(Collectors.toMap(TdFieldDO::getFieldName, f -> f));

        return newFields.stream()
                .filter(f -> {
                    TdFieldDO oldField = oldFieldMap.get(f.getFieldName());
                    return oldField != null &&
                            (!oldField.getDataType().equals(f.getDataType()) ||
                                    !Objects.equals(oldField.getDataLength(), f.getDataLength()));
                })
                .collect(Collectors.toList());
    }

    // 获取删除字段
    private List<TdFieldDO> getDropFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Set<String> newFieldNames = newFields.stream()
                .map(TdFieldDO::getFieldName)
                .collect(Collectors.toSet());
        return oldFields.stream()
                .filter(f -> !"time".equals(f.getFieldName()) && !"device_id".equals(f.getFieldName()))
                .filter(f -> !newFieldNames.contains(f.getFieldName()))
                .collect(Collectors.toList());
    }

    @Override
    public void createSuperTableDataModel(IotProductDO product, List<IotThinkModelFunctionDO> functionList) {
        ThingModelRespVO thingModel = buildThingModel(product, functionList);

        if (thingModel.getModel() == null || CollUtil.isEmpty(thingModel.getModel().getProperties())) {
            log.warn("物模型属性列表为空，不创建超级表");
            return;
        }

        String superTableName = getProductPropertySTableName(product.getDeviceType(), product.getProductKey());
        String dataBaseName = getDatabaseName();
        Integer tableExists = iotTdEngineService.checkSuperTableExists(dataBaseName, superTableName);

        if (tableExists != null && tableExists > 0) {
            updateSuperTable(thingModel, product.getDeviceType());
        } else {
            createSuperTable(thingModel, product.getDeviceType());
        }
    }

    private ThingModelRespVO buildThingModel(IotProductDO product, List<IotThinkModelFunctionDO> functionList) {
        ThingModelRespVO thingModel = new ThingModelRespVO();
        thingModel.setId(product.getId());
        thingModel.setProductKey(product.getProductKey());

        ThingModelRespVO.Model model = new ThingModelRespVO.Model();
        List<ThingModelProperty> properties = functionList.stream()
                .filter(function -> IotProductFunctionTypeEnum.PROPERTY.equals(
                        IotProductFunctionTypeEnum.valueOfType(function.getType())))
                .map(this::buildThingModelProperty)
                .collect(Collectors.toList());

        model.setProperties(properties);
        thingModel.setModel(model);

        return thingModel;
    }

    private ThingModelProperty buildThingModelProperty(IotThinkModelFunctionDO function) {
        ThingModelProperty property = BeanUtil.copyProperties(function, ThingModelProperty.class);
        property.setDataType(function.getProperty().getDataType());
        return property;
    }

    private String getDatabaseName() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    static String getProductPropertySTableName(Integer deviceType, String productKey) {
        return switch (deviceType) {
            case 1 -> String.format("gateway_sub_%s", productKey).toLowerCase();
            case 2 -> String.format("gateway_%s", productKey).toLowerCase();
            default -> String.format("device_%s", productKey).toLowerCase();
        };
    }

}