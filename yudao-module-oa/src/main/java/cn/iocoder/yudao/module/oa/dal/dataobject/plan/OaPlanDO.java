package cn.iocoder.yudao.module.oa.dal.dataobject.plan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanStatusEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 工作计划 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_plan", autoResultMap = true)
@KeySequence("oa_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaPlanDO extends BaseDO {

    /**
     * 计划编号
     */
    @TableId
    private Long id;
    /**
     * 计划类型
     *
     * 枚举 {@link OaPlanTypeEnum}
     */
    private Integer type;
    /**
     * 计划状态
     *
     * 枚举 {@link OaPlanStatusEnum}
     */
    private Integer status;
    /**
     * 标题
     */
    private String title;
    /**
     * 标签
     */
    private String label;
    /**
     * 计划内容
     */
    private String content;
    /**
     * 计划总结
     */
    private String summary;
    /**
     * 计划点评
     */
    private String comment;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

}
