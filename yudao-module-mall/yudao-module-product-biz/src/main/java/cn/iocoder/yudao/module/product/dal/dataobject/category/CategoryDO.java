package cn.iocoder.yudao.module.product.dal.dataobject.category;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
    private Long pid;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类图标
     */
    private String icon;
    /**
     * 分类图片
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
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer status;

}
