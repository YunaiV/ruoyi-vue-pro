package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * ERP 产品单位 DO
 *
 * @author 芋道源码
 */
@TableName("erp_product_unit")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductUnitDO extends BaseDO {

    /**
     * 单位编号
     */
    @TableId
    private Long id;
    /**
     * 单位名字
     */
    private String name;
    /**
     * 单位状态
     */
    private Integer status;

}