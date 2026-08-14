package cn.iocoder.yudao.module.fms.dal.dataobject.voucher;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FMS 凭证 DO
 *
 * @author 芋道源码
 */
@TableName(value = "fms_voucher", autoResultMap = true)
@KeySequence("fms_voucher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsVoucherDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 凭证字编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO#getId()}
     */
    private Long voucherWordId;
    /**
     * 凭证号
     */
    private Integer voucherNumber;
    /**
     * 凭证日期
     */
    private LocalDateTime voucherTime;
    /**
     * 附件地址
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachmentUrls;
    /**
     * 附单据张数
     */
    private Integer attachmentCount;
    /**
     * 借方金额
     */
    private BigDecimal debitAmount;
    /**
     * 贷方金额
     */
    private BigDecimal creditAmount;
    /**
     * 合计金额
     */
    private BigDecimal total;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherStatusEnum}
     */
    private Integer status;
    /**
     * 审核人后台用户编号
     *
     * 关联 {@link cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO#getId()}
     */
    private Long reviewerUserId;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;

}
