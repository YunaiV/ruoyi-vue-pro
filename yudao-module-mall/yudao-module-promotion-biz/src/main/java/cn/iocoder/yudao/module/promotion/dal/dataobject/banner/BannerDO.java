package cn.iocoder.yudao.module.promotion.dal.dataobject.banner;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * banner DO
 *
 * @author xia
 */
@TableName("market_banner")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDO extends BaseDO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 跳转链接
     */
    private String url;
    /**
     * 图片链接
     */
    private String picUrl;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String memo;

    // TODO 芋艿 点击次数。&& 其他数据相关

}
