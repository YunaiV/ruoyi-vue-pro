package cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate;

import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 招聘候选人 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_recruit_candidate", autoResultMap = true)
@KeySequence("hrm_recruit_candidate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmRecruitCandidateDO extends BaseDO {

    /**
     * 候选人编号
     */
    @TableId
    private Long id;
    /**
     * 候选人姓名
     */
    private String name;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 性别
     *
     * 枚举 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 应聘职位编号
     *
     * 关联 {@link HrmRecruitPostDO#getId()}
     */
    private Long postId;
    /**
     * 面试轮次
     */
    private Integer stageNumber;
    /**
     * 工作年限，单位：年
     */
    private Integer workTime;
    /**
     * 学历
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_CANDIDATE_EDUCATION}
     */
    private Integer education;
    /**
     * 毕业院校
     */
    private String graduateSchool;
    /**
     * 最近工作单位
     */
    private String latestWorkPlace;
    /**
     * 招聘渠道编号
     *
     * 关联 {@link HrmRecruitChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 候选人状态
     *
     * 枚举 {@link HrmRecruitCandidateStatusEnum}
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_CANDIDATE_STATUS}
     */
    private Integer status;
    /**
     * 淘汰原因
     */
    private String eliminate;
    /**
     * 状态更新时间
     */
    private LocalDateTime statusUpdateTime;
    /**
     * 入职时间
     */
    private LocalDateTime entryTime;
    /**
     * 简历附件地址数组
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> resumeUrls;

}
