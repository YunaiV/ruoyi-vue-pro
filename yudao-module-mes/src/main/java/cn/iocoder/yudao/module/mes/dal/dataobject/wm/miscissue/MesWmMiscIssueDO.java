package cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscIssueStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 杂项出库单 DO
 */
@TableName("mes_wm_misc_issue")
@KeySequence("mes_wm_misc_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmMiscIssueDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 出库单编号
     */
    private String code;
    /**
     * 出库单名称
     */
    private String name;
    /**
     * 杂项类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscIssueTypeEnum}
     */
    private Integer type;
    // TODO @芋艿：这里还没定，关联哪些；
    /**
     * 来源单据ID
     */
    private Long sourceDocId;
    /**
     * 来源单据编号
     */
    private String sourceDocCode;
    /**
     * 来源单据类型
     */
    private String sourceDocType;
    /**
     * 出库日期
     */
    private LocalDateTime issueDate;
    /**
     * 状态
     *
     * 枚举 {@link MesWmMiscIssueStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
