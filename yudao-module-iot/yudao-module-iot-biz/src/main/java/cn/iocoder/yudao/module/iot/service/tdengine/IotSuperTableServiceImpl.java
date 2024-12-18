package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.FieldParser;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDDLMapper;
import cn.iocoder.yudao.module.iot.enums.thinkmodel.IotProductThinkModelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IoT 超级表服务实现类，负责根据物模型创建和更新超级表，以及创建超级表的子表等操作。
 */
@Service
@Slf4j
public class IotSuperTableServiceImpl implements IotSuperTableService {

    @Resource
    private TdEngineDDLMapper tdEngineDDLMapper;

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Override
    public void createSuperTableDataModel(IotProductDO product, List<IotProductThinkModelDO> functionList) {
        ThinkModelRespVO thingModel = buildThinkModel(product, functionList);

        if (thingModel.getModel() == null || CollUtil.isEmpty(thingModel.getModel().getProperties())) {
            log.warn("物模型属性列表为空，不创建超级表");
            return;
        }

        String superTableName = getSuperTableName(product.getDeviceType(), product.getProductKey());
        String databaseName = getDatabaseName();

        List<Map<String, Object>> results = tdEngineDDLMapper.showSuperTables(new TdTableDO(databaseName, superTableName));
        int tableExists = results == null || results.isEmpty() ? 0 : results.size();
        if (tableExists > 0) {
            updateSuperTable(thingModel, product.getDeviceType());
        } else {
            createSuperTable(thingModel, product.getDeviceType());
        }
    }

    /**
     * 创建超级表
     */
    private void createSuperTable(ThinkModelRespVO thingModel, Integer deviceType) {
        // 解析物模型，获取字段列表
        List<TdFieldDO> schemaFields = new ArrayList<>();
        schemaFields.add(TdFieldDO.builder()
                .fieldName("time")
                .dataType("TIMESTAMP")
                .build());
        schemaFields.addAll(FieldParser.parse(thingModel));

        // 设置超级表的标签
        List<TdFieldDO> tagsFields = List.of(
                TdFieldDO.builder().fieldName("product_key").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_key").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_name").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("device_type").dataType("INT").build()
        );

        // 获取超级表的名称和数据库名称
        String superTableName = getSuperTableName(deviceType, thingModel.getProductKey());
        String databaseName = getDatabaseName();

        // 创建超级表
        tdEngineDDLMapper.createSuperTable(new TdTableDO(databaseName, superTableName, schemaFields, tagsFields));
    }

    /**
     * 更新超级表
     */
    private void updateSuperTable(ThinkModelRespVO thingModel, Integer deviceType) {
        String superTableName = getSuperTableName(deviceType, thingModel.getProductKey());
        try {
            List<TdFieldDO> oldFields = getTableFields(superTableName);
            List<TdFieldDO> newFields = FieldParser.parse(thingModel);

            updateTableFields(superTableName, oldFields, newFields);
        } catch (Exception e) {
            log.error("更新物模型超级表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取表的字段信息
     */
    private List<TdFieldDO> getTableFields(String tableName) {
        List<Map<String, Object>> tableDescription = tdEngineDDLMapper.describeSuperTable(new TdTableDO(getDatabaseName(), tableName));
        if (CollUtil.isEmpty(tableDescription)) {
            return Collections.emptyList();
        }

        return tableDescription.stream()
                .filter(map -> !"TAG".equals(map.get("note")))
                .filter(map -> !"time".equals(map.get("field")))
                .map(map -> TdFieldDO.builder()
                        .fieldName((String) map.get("field"))
                        .dataType((String) map.get("type"))
                        .dataLength((Integer) map.get("length"))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 更新表的字段，包括新增、修改和删除字段
     */
    private void updateTableFields(String tableName, List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        String databaseName = getDatabaseName();

        // 获取新增、修改、删除的字段
        List<TdFieldDO> addFields = getAddFields(oldFields, newFields);
        List<TdFieldDO> modifyFields = getModifyFields(oldFields, newFields);
        List<TdFieldDO> dropFields = getDropFields(oldFields, newFields);

        // 添加新增字段
        if (CollUtil.isNotEmpty(addFields)) {
            for (TdFieldDO addField : addFields) {
                tdEngineDDLMapper.addColumnForSuperTable(TdTableDO.builder()
                        .dataBaseName(databaseName)
                        .superTableName(tableName)
                        .column(addField)
                        .build());
            }
        }
        // 删除旧字段
        if (CollUtil.isNotEmpty(dropFields)) {
            for (TdFieldDO dropField : dropFields) {
                tdEngineDDLMapper.dropColumnForSuperTable(TdTableDO.builder()
                        .dataBaseName(databaseName)
                        .superTableName(tableName)
                        .column(dropField)
                        .build());
            }
        }
        // 修改字段（先删除再添加）
        if (CollUtil.isNotEmpty(modifyFields)) {
            for (TdFieldDO modifyField : modifyFields) {
                tdEngineDDLMapper.dropColumnForSuperTable(TdTableDO.builder()
                        .dataBaseName(databaseName)
                        .superTableName(tableName)
                        .column(modifyField)
                        .build());
                tdEngineDDLMapper.addColumnForSuperTable(TdTableDO.builder()
                        .dataBaseName(databaseName)
                        .superTableName(tableName)
                        .column(modifyField)
                        .build());
            }
        }
    }

    /**
     * 获取需要新增的字段
     */
    private List<TdFieldDO> getAddFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Set<String> oldFieldNames = oldFields.stream()
                .map(TdFieldDO::getFieldName)
                .collect(Collectors.toSet());
        return newFields.stream()
                .filter(f -> !oldFieldNames.contains(f.getFieldName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取需要修改的字段
     */
    private List<TdFieldDO> getModifyFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Map<String, TdFieldDO> oldFieldMap = oldFields.stream()
                .collect(Collectors.toMap(TdFieldDO::getFieldName, f -> f));

        return newFields.stream()
                .filter(f -> {
                    TdFieldDO oldField = oldFieldMap.get(f.getFieldName());
                    return oldField != null && (
                            !oldField.getDataType().equals(f.getDataType()) ||
                                    !Objects.equals(oldField.getDataLength(), f.getDataLength())
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取需要删除的字段
     */
    private List<TdFieldDO> getDropFields(List<TdFieldDO> oldFields, List<TdFieldDO> newFields) {
        Set<String> newFieldNames = newFields.stream()
                .map(TdFieldDO::getFieldName)
                .collect(Collectors.toSet());
        return oldFields.stream()
                .filter(f -> !"time".equals(f.getFieldName()))
                .filter(f -> !newFieldNames.contains(f.getFieldName()))
                .collect(Collectors.toList());
    }

    /**
     * 构建物模型
     */
    private ThinkModelRespVO buildThinkModel(IotProductDO product, List<IotProductThinkModelDO> functionList) {
        ThinkModelRespVO thingModel = new ThinkModelRespVO();
        thingModel.setId(product.getId());
        thingModel.setProductKey(product.getProductKey());

        List<ThinkModelProperty> properties = functionList.stream()
                .filter(function -> IotProductThinkModelTypeEnum.PROPERTY.equals(
                        IotProductThinkModelTypeEnum.valueOfType(function.getType())))
                .map(this::buildThinkModelProperty)
                .collect(Collectors.toList());

        ThinkModelRespVO.Model model = new ThinkModelRespVO.Model();
        model.setProperties(properties);
        thingModel.setModel(model);

        return thingModel;
    }

    /**
     * 构建物模型属性
     */
    private ThinkModelProperty buildThinkModelProperty(IotProductThinkModelDO function) {
        ThinkModelProperty property = BeanUtil.copyProperties(function, ThinkModelProperty.class);
        property.setDataType(function.getProperty().getDataType());
        return property;
    }

    /**
     * 获取数据库名称
     */
    private String getDatabaseName() {
        int index = url.lastIndexOf("/");
        return index != -1 ? url.substring(index + 1) : url;
    }

    /**
     * 获取超级表名称
     */
    private String getSuperTableName(Integer deviceType, String productKey) {
        String prefix = switch (deviceType) {
            case 1 -> "gateway_sub_";
            case 2 -> "gateway_";
            default -> "device_";
        };
        return (prefix + productKey).toLowerCase();
    }

}