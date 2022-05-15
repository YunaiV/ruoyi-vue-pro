package cn.iocoder.yudao.module.product.dal.dataobject.attrkey;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 规格名称 DO
 *
 * @author 芋道源码
 */
@TableName("product_attr_key")
@KeySequence("product_attr_key_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttrKeyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 规格名称
     */
    private String attrName;
    /**
     * 状态： 1 开启 ，2 禁用
     */
    private Integer status;

}
