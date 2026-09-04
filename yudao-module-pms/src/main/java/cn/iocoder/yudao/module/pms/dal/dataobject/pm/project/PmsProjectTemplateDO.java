package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * PMS 项目模板 DO
 *
 * <p>模板只保存新建项目时使用的默认协作配置，项目创建后会生成自己的运行配置</p>
 *
 * @author 芋道源码
 */
@TableName(value = "pms_project_template", autoResultMap = true)
@KeySequence("pms_project_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectTemplateDO extends BaseDO {

    /**
     * 模板编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板描述
     */
    private String description;
    /**
     * 项目类型
     *
     * 枚举 {@link PmsProjectTypeEnum}
     */
    private Integer projectType;
    /**
     * 模板状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 启用的工作项类型列表
     *
     * 枚举 {@link PmsWorkItemTypeEnum}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Integer> itemTypes;
    /**
     * 工作项状态模板列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<StatusTemplate> statuses;
    /**
     * 看板列模板列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<BoardTemplate> boards;

    /**
     * 工作项状态模板
     */
    @Data
    public static class StatusTemplate {

        /**
         * 模板内稳定编码
         */
        private String code;
        /**
         * 状态名称
         */
        private String name;
        /**
         * 工作项类型
         *
         * 枚举 {@link PmsWorkItemTypeEnum}
         */
        private Integer workItemType;
        /**
         * 语义状态
         *
         * 枚举 {@link PmsWorkItemStatusTypeEnum}
         */
        private Integer statusType;
        /**
         * 是否为初始状态
         */
        private Boolean defaultStatus;
        /**
         * 显示顺序
         */
        private Integer sort;
        /**
         * 所属看板列编码
         */
        private String boardCode;
    }

    /**
     * 看板列模板
     */
    @Data
    public static class BoardTemplate {

        /**
         * 模板内稳定编码
         */
        private String code;
        /**
         * 看板列名称
         */
        private String name;
        /**
         * 工作项类型
         *
         * 枚举 {@link PmsWorkItemTypeEnum}
         */
        private Integer workItemType;
        /**
         * 显示顺序
         */
        private Integer sort;
        /**
         * 看板列关联的状态编码列表
         */
        private List<String> statusCodes;
    }

}
