package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.resp;

import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseRequestRespVO extends BaseVO {

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
    private LocalDateTime requestTime;
    // ========== 审核信息 ==========
    @Schema(description = "审核者")
    @ExcelProperty("审核者")
    private String auditor;
    private Long auditorId;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime auditTime;

    // ========== 订单项列表 ==========
    @Schema(description = "采购订单项列表")
    private List<ErpPurchaseRequestItemRespVO> items;

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
    private Integer status;

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
}
