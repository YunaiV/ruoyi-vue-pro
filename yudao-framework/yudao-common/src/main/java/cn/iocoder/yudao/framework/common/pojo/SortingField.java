package cn.iocoder.yudao.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 排序字段 DTO
 *
 * 类名加了 ing 的原因是，避免和 ES SortField 重名。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortingField implements Serializable {

    /**
     * 顺序 - 升序
     */
    public static final String ORDER_ASC = "asc";
    /**
     * 顺序 - 降序
     */
    public static final String ORDER_DESC = "desc";

    /**
     * 字段
     */
    private String field;
    /**
     * 顺序
     */
    private String order;
    /**
     * 是否转换字段名格式（驼峰转下划线）
     * 默认为 true，保持向后兼容性
     */
    private Boolean convertFieldFormat = true;

    /**
     * 构造函数，保持向后兼容性
     */
    public SortingField(String field, String order) {
        this.field = field;
        this.order = order;
        this.convertFieldFormat = true;
    }

}
