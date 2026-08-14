package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo;

import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FMS 账簿凭证分录 VO
 *
 * @author 芋道源码
 */
@Data
public class FmsLedgerEntryVO {

    /**
     * 分录编号
     */
    private Long entryId;
    /**
     * 凭证编号
     */
    private Long voucherId;
    /**
     * 科目编号
     */
    private Long subjectId;
    /**
     * 凭证日期
     */
    private LocalDateTime voucherTime;
    /**
     * 凭证号
     */
    private Integer voucherNumber;
    /**
     * 凭证字名称
     */
    private String voucherWordName;
    /**
     * 摘要内容
     */
    private String digest;
    /**
     * 借方金额
     */
    private BigDecimal debitAmount;
    /**
     * 贷方金额
     */
    private BigDecimal creditAmount;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 辅助核算组合编号
     */
    private Long assistCombinationId;
    /**
     * 辅助核算项目数组
     */
    private List<FmsVoucherEntryDO.AuxiliaryItem> auxiliaries;

}
