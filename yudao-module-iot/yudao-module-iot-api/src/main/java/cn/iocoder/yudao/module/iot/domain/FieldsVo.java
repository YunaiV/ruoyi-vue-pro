package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassDescription: 建表的字段实体类的vo类
 * @ClassName: FieldsVo
 * @Author: fxlinks
 * @Date: 2021-12-28 11:30:06
 * @Version 1.0
 */
@Data
public class FieldsVo {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段数据类型
     */
    private String dataType;

    /**
     * 字段字节大小
     */
    private Integer size;

    /**
     * @param fields 字段实体类
     * @return FieldsVo 字段实体vo类
     * @MethodDescription 字段实体类转为vo类
     * @author fx
     * @Date 2021/12/28 13:48
     */
    public static FieldsVo fieldsTranscoding(Fields fields) throws SQLException {
//        if (StringUtils.isBlank(fields.getFieldName()) || fields.getDataType() == null) {
//            throw new SQLException("invalid operation: fieldName or dataType can not be null");
//        }
//        FieldsVo fieldsVo = new FieldsVo();
//        fieldsVo.setFieldName(fields.getFieldName());
//        fieldsVo.setDataType(fields.getDataType().getDataType());
//        fieldsVo.setSize(fields.getSize());
//        return fieldsVo;
        return null;
    }

    /**
     * @param fieldsList 字段实体类集合
     * @return List<FieldsVo> 字段实体vo类集合
     * @MethodDescription 字段实体类集合转为vo类集合
     * @author fx
     * @Date 2021/12/28 14:00
     */
    public static List<FieldsVo> fieldsTranscoding(List<Fields> fieldsList) throws SQLException {
        List<FieldsVo> fieldsVoList = new ArrayList<>();
        for (Fields fields : fieldsList) {
            fieldsVoList.add(fieldsTranscoding(fields));
        }
        return fieldsVoList;
    }
}
