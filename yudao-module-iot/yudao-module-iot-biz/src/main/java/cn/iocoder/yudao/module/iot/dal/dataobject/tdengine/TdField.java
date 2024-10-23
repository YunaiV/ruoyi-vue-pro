package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TD 引擎的字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TdField {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段长度
     */
    private int length;
}