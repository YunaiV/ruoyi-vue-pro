package cn.iocoder.yudao.module.product.dal.dataobject.brand;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品品牌 DO
 *
 * @author 芋道源码
 */
@TableName("product_brand")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrandDO extends BaseDO {

    /**
     * 品牌编号
     */
    @TableId
    private Long id;
    /**
     * 品牌名称
     */
    private String name;
    /**
     * 品牌图片
     */
    private String picUrl;
    /**
     * 品牌排序
     */
    private Integer sort;
    /**
     * 品牌描述
     */
    private String description;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
