package cn.iocoder.yudao.module.iot.domain;

import lombok.Builder;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 字段信息 VO
 */
@Data
@Builder
public class FieldsVo {

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
}
