package cn.iocoder.yudao.module.promotion.dal.dataobject.diy;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 装修模板 DO
 *
 * 1. 新建一个模版，下面可以包含多个 {@link DiyPageDO} 页面，例如说首页、我的
 * 2. 如果需要使用某个模版，则将 {@link #used} 设置为 true，表示已使用，有且仅有一个
 *
 * @author owen
 */
@TableName(value = "promotion_diy_template", autoResultMap = true)
@KeySequence("promotion_diy_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyTemplateDO extends BaseDO {

    /**
     * 装修模板编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 是否使用
     */
    private Boolean used;
    /**
     * 使用时间
     */
    private LocalDateTime usedTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 预览图
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> previewImageUrls;
    /**
     * 底部导航属性，JSON 格式
     */
    private String property;

}
