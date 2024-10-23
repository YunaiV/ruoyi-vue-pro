package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public static List<FieldsVo> fieldsTranscoding(List<Fields> fieldsList) throws SQLException {
        List<FieldsVo> fieldsVoList = new ArrayList<>();
        for (Fields fields : fieldsList) {
            fieldsVoList.add(fieldsTranscoding(fields));
        }
        return fieldsVoList;
    }
}
