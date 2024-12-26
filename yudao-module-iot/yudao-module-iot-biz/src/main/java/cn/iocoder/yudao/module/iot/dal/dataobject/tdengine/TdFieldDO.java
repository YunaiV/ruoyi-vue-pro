package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO 芋艿：看看是不是后续简化掉。
/**
 * TD 引擎的字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Deprecated
public class TdFieldDO {

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private String dataType;

    /**
     * 字段长度
     */
    private Integer dataLength = 0;

    /**
     * 字段值
     */
    private Object fieldValue;

    public TdFieldDO(String fieldName, String dataType, Integer dataLength) {
        this.fieldName = fieldName;
        this.dataType = dataType;
        this.dataLength = dataLength;
    }

    public TdFieldDO(String fieldName, Object fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}