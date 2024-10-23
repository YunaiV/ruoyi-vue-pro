package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IotDbStructureDataServiceImpl implements IotDbStructureDataService {

    @Resource
    private TdEngineMapper tdEngineMapper;

    @Resource
    private TdRestApi tdRestApi;

    @Override
    public void createSuperTable(ThingModelRespVO thingModel, Integer deviceType) {
        // 获取物模型中的属性定义
        List<TdField> fields = FieldParser.parse(thingModel);
        String tbName = getProductPropertySTableName(deviceType, thingModel.getProductKey());

        // 生成创建超级表的 SQL
        String sql = TableManager.getCreateSTableSql(tbName, fields, new TdField("device_id", "NCHAR", 64));
        if (sql == null) {
            log.warn("生成的 SQL 为空，无法创建超级表");
            return;
        }
        log.info("执行 SQL: {}", sql);

        // 执行 SQL 创建超级表
        tdEngineMapper.createSuperTableDevice(sql);
    }

    @Override
    public void updateSuperTable(ThingModelRespVO thingModel, Integer deviceType) {
        try {
            // 获取旧字段信息
            String tbName = getProductPropertySTableName(deviceType, thingModel.getProductKey());
            String sql = TableManager.getDescTableSql(tbName);
            TdResponse response = tdRestApi.execSql(sql);
            if (response.getCode() != TdResponse.CODE_SUCCESS) {
                throw new RuntimeException("获取表描述错误: " + JSONUtil.toJsonStr(response));
            }

            List<TdField> oldFields = FieldParser.parse(response.getData());
            List<TdField> newFields = FieldParser.parse(thingModel);

            // 找出新增的字段
            List<TdField> addFields = newFields.stream()
                    .filter(f -> oldFields.stream().noneMatch(old -> old.getName().equals(f.getName())))
                    .collect(Collectors.toList());
            if (!addFields.isEmpty()) {
                sql = TableManager.getAddSTableColumnSql(tbName, addFields);
                response = tdRestApi.execSql(sql);
                if (response.getCode() != TdResponse.CODE_SUCCESS) {
                    throw new RuntimeException("添加表字段错误: " + JSONUtil.toJsonStr(response));
                }
            }

            // 找出修改的字段
            List<TdField> modifyFields = newFields.stream()
                    .filter(f -> oldFields.stream().anyMatch(old ->
                            old.getName().equals(f.getName()) &&
                                    (!old.getType().equals(f.getType()) || old.getLength() != f.getLength())))
                    .collect(Collectors.toList());
            if (!modifyFields.isEmpty()) {
                sql = TableManager.getModifySTableColumnSql(tbName, modifyFields);
                response = tdRestApi.execSql(sql);
                if (response.getCode() != TdResponse.CODE_SUCCESS) {
                    throw new RuntimeException("修改表字段错误: " + JSONUtil.toJsonStr(response));
                }
            }

            // 找出删除的字段
            List<TdField> dropFields = oldFields.stream()
                    .filter(f -> !"time".equals(f.getName()) && !"device_id".equals(f.getName()) &&
                            newFields.stream().noneMatch(n -> n.getName().equals(f.getName())))
                    .collect(Collectors.toList());
            if (!dropFields.isEmpty()) {
                sql = TableManager.getDropSTableColumnSql(tbName, dropFields);
                response = tdRestApi.execSql(sql);
                if (response.getCode() != TdResponse.CODE_SUCCESS) {
                    throw new RuntimeException("删除表字段错误: " + JSONUtil.toJsonStr(response));
                }
            }
        } catch (Throwable e) {
            log.error("更新物模型超级表失败", e);
        }
    }

    @Override
    public void createSuperTableDataModel(IotProductDO product, List<IotThinkModelFunctionDO> functionList) {
        // 1. 生成 ThingModelRespVO
        ThingModelRespVO thingModel = new ThingModelRespVO();
        thingModel.setId(product.getId());
        thingModel.setProductKey(product.getProductKey());

        // 1.1 设置属性、服务和事件
        ThingModelRespVO.Model model = new ThingModelRespVO.Model();
        List<ThingModelProperty> properties = new ArrayList<>();

        // 1.2 遍历功能列表并分类
        for (IotThinkModelFunctionDO function : functionList) {
            if (Objects.requireNonNull(IotProductFunctionTypeEnum.valueOf(function.getType())) == IotProductFunctionTypeEnum.PROPERTY) {
                ThingModelProperty property = new ThingModelProperty();
                property.setIdentifier(function.getIdentifier());
                property.setName(function.getName());
                property.setDescription(function.getDescription());
                property.setDataType(function.getProperty().getDataType());
                properties.add(property);
            }
        }

        // 1.3 判断属性列表是否为空
        if (properties.isEmpty()) {
            log.warn("物模型属性列表为空，不创建超级表");
            return;
        }

        model.setProperties(properties);
        thingModel.setModel(model);

        // 2. 判断是否已经创建,如果已经创建则进行更新
        String tbName = getProductPropertySTableName(product.getDeviceType(), product.getProductKey());
        Integer iot = tdEngineMapper.checkTableExists("ruoyi_vue_pro", tbName);
        if (iot != null && iot > 0) {
            // 3. 更新
            updateSuperTable(thingModel, product.getDeviceType());
        } else {
            // 4. 创建
            createSuperTable(thingModel, product.getDeviceType());
        }
    }

    /**
     * 根据产品key获取产品属性超级表名
     */
    static String getProductPropertySTableName(Integer deviceType, String productKey) {
        if (deviceType == 1) {
            return String.format("gateway_sub_" + productKey).toLowerCase();
        } else if (deviceType == 2) {
            return String.format("gateway_" + productKey).toLowerCase();
        } else {
            return String.format("device_" + productKey).toLowerCase();
        }
    }

    /**
     * 根据deviceId获取设备属性表名
     */
    static String getDevicePropertyTableName(String deviceType, String productKey, String deviceKey) {
        return String.format(deviceType + "_" + productKey + "_" + deviceKey).toLowerCase();
    }
}
