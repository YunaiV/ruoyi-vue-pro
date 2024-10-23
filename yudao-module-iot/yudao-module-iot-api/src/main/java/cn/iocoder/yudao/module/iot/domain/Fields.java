package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

/**
 * @ClassDescription: 建表的字段实体类
 * @ClassName: Fields
 * @Author: fxlinks
 * @Date: 2021-12-28 09:09:04
 * @Version 1.0
 */
@Data
public class Fields {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段值
     */
    private Object fieldValue;

    /**
     * 字段数据类型
     */
//    private DataTypeEnum dataType;

    /**
     * 字段字节大小
     */
    private Integer size;

    public Fields() {
    }

    public Fields(String fieldName, String dataType, Integer size) {
//        this.fieldName = fieldName;
//        //根据规则匹配字段数据类型
//        switch (dataType.toLowerCase()) {
//            case ("json"):
//                this.dataType = DataTypeEnum.JSON;
//                this.size = size;
//                break;
//            case ("string"):
//                this.dataType = DataTypeEnum.NCHAR;
//                this.size = size;
//                break;
//            case ("binary"):
//                this.dataType = DataTypeEnum.BINARY;
//                this.size = size;
//                break;
//            case ("int"):
//                this.dataType = DataTypeEnum.INT;
//                break;
//            case ("bool"):
//                this.dataType = DataTypeEnum.BOOL;
//                break;
//            case ("decimal"):
//                this.dataType = DataTypeEnum.DOUBLE;
//                break;
//            case ("timestamp"):
//                if ("eventTime".equals(fieldName)) {
//                    this.fieldName = "eventTime";
//                }
//                this.dataType = DataTypeEnum.TIMESTAMP;
//                break;
//        }
    }
}
