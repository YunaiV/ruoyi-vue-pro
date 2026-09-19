package cn.iocoder.yudao.module.oa.dal.dataobject.seal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealApplyTypeEnum;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealUseModeEnum;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealUseStatusEnum;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 用印申请 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_seal_apply", autoResultMap = true)
@KeySequence("oa_seal_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaSealApplyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 申请单号
     */
    private String no;
    /**
     * 申请人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 申请部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 印章编号
     *
     * 关联 {@link OaSealDO#getId()}
     */
    private Long sealId;
    /**
     * 印章编号快照
     *
     * 冗余 {@link OaSealDO#getNo()}
     */
    private String sealNo;
    /**
     * 印章名称快照
     *
     * 冗余 {@link OaSealDO#getName()}
     */
    private String sealName;
    /**
     * 印章类型快照
     *
     * 冗余 {@link OaSealDO#getType()}
     */
    private Integer sealType;
    /**
     * 保管部门编号快照
     *
     * 冗余 {@link OaSealDO#getKeeperDeptId()}
     */
    private Long keeperDeptId;
    /**
     * 保管人用户编号快照
     *
     * 冗余 {@link OaSealDO#getKeeperUserId()}
     */
    private Long keeperUserId;
    /**
     * 用印事由
     */
    private String reason;
    /**
     * 用印类型
     *
     * 枚举 {@link OaSealApplyTypeEnum}
     * 字典 {@link DictTypeConstants#SEAL_APPLY_TYPE}
     */
    private Integer type;
    /**
     * 用印方式
     *
     * 枚举 {@link OaSealUseModeEnum}
     * 字典 {@link DictTypeConstants#SEAL_USE_MODE}
     */
    private Integer mode;
    /**
     * 文件标题
     */
    private String documentTitle;
    /**
     * 文件类型
     */
    private String documentType;
    /**
     * 文件份数
     */
    private Integer documentCount;
    /**
     * 合同金额
     */
    private BigDecimal contractPrice;
    /**
     * 合同对方
     */
    private String contractParty;
    /**
     * 预计用印时间
     */
    private LocalDateTime expectedUseTime;
    /**
     * 实际用印时间
     */
    private LocalDateTime actualUseTime;
    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnTime;
    /**
     * 实际归还时间
     */
    private LocalDateTime actualReturnTime;
    /**
     * 用印业务状态
     *
     * 枚举 {@link OaSealUseStatusEnum}
     * 字典 {@link DictTypeConstants#SEAL_USE_STATUS}
     */
    private Integer useStatus;
    /**
     * 审批状态，取 BPM 流程实例状态
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
    /**
     * 是否紧急
     */
    private Boolean urgent;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

}
