package cn.iocoder.yudao.module.promotion.dal.dataobject.decorate;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.decorate.PageComponentEnum;
import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/**
 * 页面装修 DO
 *
 * @author jason
 */
@TableName(value ="promotion_page_decorate")
@KeySequence("promotion_page_decorate_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class PageDecorateDO extends BaseDO {
    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 页面类型
     * 枚举 {@link DecoratePageTypeEnum#getType()}
     */
    private Integer type;

    /**
     *  组件编码
     *  枚举 {@link PageComponentEnum#getCode()}
     */
    private String componentCode;

    /**
     * 组件值：json 格式。包含配置和数据
     */
    private String componentValue;

    /**
     * 状态
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
}