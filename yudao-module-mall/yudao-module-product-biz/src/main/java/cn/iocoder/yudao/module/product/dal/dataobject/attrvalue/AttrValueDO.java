package cn.iocoder.yudao.module.product.dal.dataobject.attrvalue;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 规格值 DO
 *
 * @author 芋道源码
 */
@TableName("product_attr_value")
@KeySequence("product_attr_value_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttrValueDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 规格键id
     */
    private String attrKeyId;
    /**
     * 规格值名字
     */
    private String attrValueName;
    /**
     * 状态： 1 开启 ，2 禁用
     */
    private Integer status;

}
