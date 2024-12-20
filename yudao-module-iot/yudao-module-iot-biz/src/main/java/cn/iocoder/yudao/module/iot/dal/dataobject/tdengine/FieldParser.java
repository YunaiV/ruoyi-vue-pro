package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelRespVO;

import java.util.HashMap;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * FieldParser 类用于解析和转换物模型字段到 TDengine 字段
 */
public class FieldParser {

    /**
     * 物模型到td数据类型映射
     */
    private static final HashMap<String, String> TYPE_MAPPING = new HashMap<>() {
        {
            put("INT", "INT");
            put("FLOAT", "FLOAT");
            put("DOUBLE", "DOUBLE");
            put("BOOL", "BOOL");
            put("ENUM", "NCHAR");
            put("TEXT", "NCHAR");
            put("DATE", "NCHAR");
        }
    };

    /**
     * 将物模型字段转换为td字段
     *
     * @param property 物模型属性
     * @return TdField对象
     */
    public static TdFieldDO parse(ThingModelProperty property) {
        // 将物模型字段类型映射为 td 字段类型
        String fieldName = property.getIdentifier().toLowerCase();
        String fType = TYPE_MAPPING.get(property.getDataType().toUpperCase());
        // 如果字段类型为NCHAR，默认长度为64
        int dataLength = 0;
        if ("NCHAR".equals(fType)) {
            dataLength = 64;
        }
        return new TdFieldDO(fieldName, fType, dataLength);
    }

    /**
     * 从物模型中获取字段列表
     *
     * @param thingModel 物模型响应对象
     * @return 字段列表
     */
    public static List<TdFieldDO> parse(ThingModelRespVO thingModel) {
        return convertList(thingModel.getModel().getProperties(), FieldParser::parse);
    }

    /**
     * 将从库中查出来的字段信息转换为 TDengine 字段对象
     *
     * @param rows 从库中查出的字段信息列表
     * @return 转换后的 TDengine 字段对象列表
     */
    public static List<TdFieldDO> parse(List<List<Object>> rows) {
        return convertList(rows, row -> {
            String type = row.get(1).toString().toUpperCase();
            // TODO @puhui999："NCHAR" 最好枚举下
            int dataLength = "NCHAR".equals(type) ? Integer.parseInt(row.get(2).toString()) : -1;
            return new TdFieldDO(row.get(0).toString(), type, dataLength);
        });
    }

    /**
     * 获取字段字义
     */
    public static String getFieldDefine(TdFieldDO field) {
        return "`" + field.getFieldName() + "`" + " "
                + (field.getDataLength() > 0 ? String.format("%s(%d)", field.getDataType(), field.getDataLength())
                : field.getDataType());
    }

}