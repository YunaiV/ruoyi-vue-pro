package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectLevelEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * PMS 项目 DO
 *
 * @author 芋道源码
 */
@TableName("pms_project")
@KeySequence("pms_project_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectDO extends BaseDO {

    /**
     * 项目编号
     */
    @TableId
    private Long id;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目状态
     *
     * 枚举 {@link PmsProjectStatusEnum}
     */
    private Integer status;
    /**
     * 项目类型
     *
     * 枚举 {@link PmsProjectTypeEnum}
     */
    private Integer type;
    /**
     * 项目优先级
     *
     * 枚举 {@link PmsProjectLevelEnum}
     */
    private Integer level;
    /**
     * 项目描述
     */
    private String description;
    /**
     * 是否公开
     */
    private Boolean openStatus;
    /**
     * 项目图标
     */
    private String icon;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 截止时间
     */
    private LocalDateTime endTime;
    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;
    /**
     * 移入回收站时间
     */
    private LocalDateTime recycleTime;
    /**
     * 最近访问时间
     */
    private LocalDateTime accessTime;

}
