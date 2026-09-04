package cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 工作项看板状态 DO
 *
 * @author 芋道源码
 */
@TableName("pms_work_item_status")
@KeySequence("pms_work_item_status_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemStatusDO extends BaseDO {

    /**
     * 状态编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     *
     * 关联 {@link PmsProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 工作项类型
     *
     * 枚举 {@link PmsWorkItemTypeEnum}
     */
    private Integer workItemType;
    /**
     * 状态名称
     */
    private String name;
    /**
     * 语义状态
     *
     * 枚举 {@link PmsWorkItemStatusTypeEnum}
     */
    private Integer statusType;
    /**
     * 状态描述
     */
    private String description;
    /**
     * 所属看板列名称，为空时不在看板展示
     */
    private String boardName;
    /**
     * 系统状态编码，仅默认状态使用，用于保证并发初始化幂等
     */
    private String systemCode;
    /**
     * 是否初始状态
     */
    private Boolean defaultStatus;
    /**
     * 显示顺序
     */
    private Integer sort;

}
