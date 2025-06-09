package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO.PURCHASE_REQUEST_NO_PREFIX;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class SrmPurchaseRequestSaveReqVO {

    @Schema(description = "id")
    @Null(groups = Validation.OnCreate.class, message = "创建时，申请单id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，申请单id不能为空")
    @DiffLogField(name = "申请单编号")
    private Long id;

    @Schema(description = "申请人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请人不能为空")
    @DiffLogField(name = "申请人编号")
    private Long applicantId;

    @Schema(description = "申请部门id")
    @NotNull(message = "申请部门不能为空")
    @DiffLogField(name = "申请部门编号")
    private Long applicationDeptId;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DiffLogField(name = "单据日期")
    private LocalDateTime billTime;

    @Pattern(regexp = "^" + PURCHASE_REQUEST_NO_PREFIX + "-\\d{8}-\\d{6}$",
        message = "单据编号格式不正确，正确格式如：" + PURCHASE_REQUEST_NO_PREFIX + "-20250108-000001")
    @Pattern(regexp = "^" + PURCHASE_REQUEST_NO_PREFIX + "-\\d{8}-[0-8]\\d{5}$",
        message = "单据编号格式不正确，注意后6位序号中不能以9开头,正确格式:" + PURCHASE_REQUEST_NO_PREFIX + "-20250108-000001")
    @Schema(description = "单据编号", example = "CGDD-20250108-000027")
    @DiffLogField(name = "单据编号")
    private String code;

    @Schema(description = "单据标签")
    @DiffLogField(name = "单据标签")
    private String tag;

    @Schema(description = "供应商id")
    @DiffLogField(name = "供应商编号")
    private Long supplierId;

    @Schema(description = "收货地址")
    @DiffLogField(name = "收货地址")
    private String delivery;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "商品信息")
    @NotNull(message = "商品信息不能为空")
    @Size(min = 1, message = "商品信息至少一个")
    @DiffLogField(name = "商品信息")
    private List<@Valid SrmPurchaseRequestItemsSaveReqVO> items;
}

