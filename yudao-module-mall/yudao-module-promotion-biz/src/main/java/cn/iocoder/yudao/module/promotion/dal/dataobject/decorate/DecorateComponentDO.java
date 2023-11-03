package cn.iocoder.yudao.module.promotion.dal.dataobject.decorate;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecorateComponentEnum;
import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/**
 * 页面装修组件 DO, 一个页面由多个组件构成
 *
 * @author jason
 */
@TableName(value ="promotion_decorate_component")
@KeySequence("promotion_decorate_component_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class DecorateComponentDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 所属页面 id
     *
     * 枚举 {@link DecoratePageEnum#getPage()}
     */
    private Integer page;

    /**
     * 组件编码
     * 枚举 {@link DecorateComponentEnum#getCode()}
     */
    private String code;

    /**
     * 组件值：json 格式。包含配置和数据
     */
    private String value;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
