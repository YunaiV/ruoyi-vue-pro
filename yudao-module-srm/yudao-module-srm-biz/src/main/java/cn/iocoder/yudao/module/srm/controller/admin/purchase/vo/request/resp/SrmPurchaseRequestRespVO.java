package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.resp;

import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmPurchaseRequestRespVO extends BaseVO {

    @Schema(description = "id")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "单据编号")
    @ExcelProperty("单据编号")
    private String no;

    @Schema(description = "申请人")
    @ExcelProperty("申请人")
    private String applicant;
    private Long applicantId;

    @Schema(description = "申请部门")
    @ExcelProperty("申请部门")
    private String applicationDept;
    private Long applicationDeptId;

    @Schema(description = "单据日期")
    @ExcelProperty("单据日期")
    private LocalDateTime billTime;
    // ========== 审核信息 ==========
    @Schema(description = "审核者id")
    @ExcelProperty("审核者id")
    private String auditor;

    @Schema(description = "审核者id")
    private Long auditorId;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime auditTime;

    // ========== 订单项列表 ==========
    @Schema(description = "采购订单项列表")
    private List<SrmPurchaseRequestItemRespVO> items;

    @Schema(description = "产品信息")
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "产品总数", example = "100")
    @ExcelProperty("产品总数")
    private Integer totalCount;

    // ========== 申请单创建和更新 ==========
    @Schema(description = "制单时间")
    @ExcelProperty("制单时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime createTime;

    // ========== 申请单计算 ==========
    // ========== 状态 ==========
    @Schema(description = "审核状态（待审核，审核通过，审核未通过）")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
//    @DictFormat(DictTypeConstants.AUDIT_STATUS)
    private Integer auditStatus;

    @Schema(description = "关闭状态（已关闭，已开启）")
    @ExcelProperty("关闭状态")
    private Integer offStatus;

    @Schema(description = "订购状态（部分订购，全部订购）")
    @ExcelProperty("订购状态")
    private Integer orderStatus;

    //收获地址
    @Schema(description = "收获地址")
    @ExcelProperty("收获地址")
    private String delivery;

    //supplierId
    @Schema(description = "供应商id")
    @ExcelProperty("供应商id")
    private Long supplierId;
    //supplierName
    @Schema(description = "供应商名称")
    @ExcelProperty("供应商名称")
    private String supplierName;
    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    @ExcelProperty("审核意见")
    private String reviewComment;


    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "版本号")
    private Long version;
//    /**
//     * 采购单ID
//     *
//     * @link {SrmPurchaseOrderDO}
//     */
//    private Long purchaseOrderId;
}
