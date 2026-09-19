package cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.officialdoc.OaOfficialDocHandleStatusEnum;
import cn.iocoder.yudao.module.oa.enums.officialdoc.OaOfficialDocReceiveTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 公文收文 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_official_doc_receive", autoResultMap = true)
@KeySequence("oa_official_doc_receive_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaOfficialDocReceiveDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 单据编号
     */
    private String no;
    /**
     * 来源发文编号
     *
     * 关联 {@link OaOfficialDocSendDO#getId()}，手工新增收文时为空
     */
    private Long sendId;
    /**
     * 收文类型
     *
     * 枚举 {@link OaOfficialDocReceiveTypeEnum}
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_RECEIVE_TYPE}
     */
    private Integer receiveType;
    /**
     * 标题
     *
     * 自动生成时冗余 {@link OaOfficialDocSendDO#getTitle()}，手工收文直接填写
     */
    private String title;
    /**
     * 来文字号
     *
     * 自动生成时冗余 {@link OaOfficialDocSendDO#getDocumentNo()}，手工收文直接填写
     */
    private String documentNo;
    /**
     * 密级
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_SECRET_LEVEL}
     * 自动生成时冗余 {@link OaOfficialDocSendDO#getSecrecyLevel()}，手工收文直接填写
     */
    private Integer secrecyLevel;
    /**
     * 紧急程度
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_URGENCY_LEVEL}
     * 自动生成时冗余 {@link OaOfficialDocSendDO#getUrgencyLevel()}，手工收文直接填写
     */
    private Integer urgencyLevel;
    /**
     * 收文时间
     */
    private LocalDateTime receiveTime;
    /**
     * 收文部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long receiveDeptId;
    /**
     * 主办人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long handlerUserId;
    /**
     * 领导批示
     */
    private String instruction;
    /**
     * 办理结果
     *
     * 收文办理的结果说明，独立于流程审批意见
     */
    private String result;
    /**
     * 办理期限
     */
    private LocalDateTime deadlineTime;
    /**
     * 内容摘要
     */
    private String summary;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件地址列表
     *
     * 收文自身附件，不等同于来源发文的附件列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 正式公文地址
     *
     * 冗余 {@link OaOfficialDocSendDO#getFormalFileUrl()}，独立于收文附件
     */
    private String formalFileUrl;
    /**
     * 办理状态
     *
     * 枚举 {@link OaOfficialDocHandleStatusEnum}
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_HANDLE_STATUS}
     */
    private Integer handleStatus;
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
