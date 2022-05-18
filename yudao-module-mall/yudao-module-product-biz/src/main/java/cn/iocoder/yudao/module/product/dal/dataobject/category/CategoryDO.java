package cn.iocoder.yudao.module.product.dal.dataobject.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @JeromeSoar：Product 前缀
/**
 * 商品分类 DO
 *
 * @author 芋道源码
 */
@TableName("product_category")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDO extends BaseDO {

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 父分类编号
     */
    private Long parentId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类图标
     */
    private String icon;
    /**
     * 分类 Banner 图片
     *
     * 第一层的商品分类，会有该字段，用于用户 App 展示
     */
    private String bannerUrl;
    /**
     * 分类排序
     */
    private Integer sort;
    /**
     * 分类描述
     */
    private String description;
    /**
     * 开启状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
