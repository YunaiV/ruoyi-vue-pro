package cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 公文发文 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_official_doc_send", autoResultMap = true)
@KeySequence("oa_official_doc_send_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaOfficialDocSendDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 单据编号
     *
     * 发文业务单号，与 {@link #documentNo} 的公文文号独立
     */
    private String no;
    /**
     * 套红模板编号
     *
     * 关联 {@link OaOfficialDocTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 标题
     */
    private String title;
    /**
     * 发文字号前缀
     *
     * 初始取自 {@link OaOfficialDocTemplateDO#getNoPrefix()}，保存发文自身取值，不随模板修改联动
     */
    private String noPrefix;
    /**
     * 发文年度
     */
    private Integer year;
    /**
     * 发文序号
     */
    private Integer sequence;
    /**
     * 公文文号
     *
     * 冗余 {@link #noPrefix}、{@link #year}、{@link #sequence} 组成的完整文号，用于查询和展示
     */
    private String documentNo;
    /**
     * 密级
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_SECRET_LEVEL}
     */
    private Integer secrecyLevel;
    /**
     * 紧急程度
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_URGENCY_LEVEL}
     */
    private Integer urgencyLevel;
    /**
     * 公开类别
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_PUBLIC_CATEGORY}
     */
    private Integer disclosureType;
    /**
     * 发文时间
     */
    private LocalDateTime issueTime;
    /**
     * 发文部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long sendDeptId;
    /**
     * 主送部门编号列表
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> mainDeptIds;
    /**
     * 抄送部门编号列表
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> copyDeptIds;
    /**
     * 签发人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long signerUserId;
    /**
     * 正文
     */
    private String content;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;
    /**
     * 正式公文地址
     */
    private String formalFileUrl;
    /**
     * 附注
     */
    private String remark;
    /**
     * 审批状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     */
    private Integer status;
    /**
     * BPM 流程实例编号
     *
     * 关联 {@link HistoricProcessInstance#getId()}
     */
    private String processInstanceId;

}
