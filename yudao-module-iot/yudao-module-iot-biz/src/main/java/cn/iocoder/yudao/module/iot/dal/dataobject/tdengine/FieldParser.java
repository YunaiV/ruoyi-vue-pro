package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelDataType;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FieldParser 类用于解析和转换物模型字段到 TDengine 字段
 * 
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
    public static TdField parse(ThingModelProperty property) {
        String fieldName = property.getIdentifier().toLowerCase();
        ThingModelDataType type = property.getDataType();

        // 将物模型字段类型映射为td字段类型
        String fType = TYPE_MAPPING.get(type.getType().toUpperCase());

        int len = -1;
        // 如果字段类型为NCHAR，默认长度为64
        if ("NCHAR".equals(fType)) {
            len = 64;
        }

        return new TdField(fieldName, fType, len);
    }

    /**
     * 获取物模型中的字段列表
     */
    public static List<TdField> parse(ThingModelRespVO thingModel) {
        return thingModel.getModel().getProperties().stream().map(FieldParser::parse).collect(Collectors.toList());
    }

    /**
     * 将从库中查出来的字段信息转换为td字段对象
     */
    public static List<TdField> parse(List<List<Object>> rows) {
        return rows.stream().map(row -> {
            String type = row.get(1).toString().toUpperCase();
            return new TdField(
                    row.get(0).toString(),
                    type,
                    type.equals("NCHAR") ? Integer.parseInt(row.get(2).toString()) : -1);
        }).collect(Collectors.toList());
    }

    /**
     * 获取字段字义
     */
    public static String getFieldDefine(TdField field) {
        return "`" + field.getName() + "`" + " "
                + (field.getLength() > 0 ? String.format("%s(%d)", field.getType(), field.getLength())
                        : field.getType());
    }

}
