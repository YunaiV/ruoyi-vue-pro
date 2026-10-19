package cn.iocoder.yudao.module.oa.dal.dataobject.workreport;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportStatusEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 工作汇报 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_work_report", autoResultMap = true)
@KeySequence("oa_work_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaWorkReportDO extends BaseDO {

    /**
     * 汇报编号
     */
    @TableId
    private Long id;
    /**
     * 汇报单号
     */
    private String no;
    /**
     * 汇报类型
     *
     * 枚举 {@link OaWorkReportTypeEnum}
     */
    private Integer type;
    /**
     * 汇报状态
     *
     * 枚举 {@link OaWorkReportStatusEnum}
     */
    private Integer status;
    /**
     * 汇报人所属部门编号
     *
     * 关联 {@link cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 周期开始时间
     */
    private LocalDateTime startTime;
    /**
     * 周期结束时间
     */
    private LocalDateTime endTime;
    /**
     * 汇报标题
     */
    private String title;
    /**
     * 工作总结
     */
    private String summary;
    /**
     * 工作计划补充说明
     */
    private String plan;
    /**
     * 问题与协调事项
     */
    private String problem;
    /**
     * 已完成工作项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<WorkItem> workItems;
    /**
     * 工作计划项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PlanItem> planItems;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 备注
     */
    private String remark;

    /**
     * 已完成工作项
     */
    @Data
    public static class WorkItem {

        /**
         * 工作内容
         */
        private String content;
        /**
         * 完成进度
         */
        private Integer progress;

    }

    /**
     * 工作计划项
     */
    @Data
    public static class PlanItem {

        /**
         * 计划内容
         */
        private String content;

    }

}
