package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.product.CrmBusinessProductSaveReqVO;
import cn.iocoder.yudao.module.crm.enums.business.CrmBizEndStatus;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @ljileo：DiffLogField function 完善一下
@Schema(description = "管理后台 - CRM 商机创建/更新 Request VO")
@Data
public class CrmBusinessSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32129")
    private Long id;

    @Schema(description = "商机名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @DiffLogField(name = "商机名称")
    @NotNull(message = "商机名称不能为空")
    private String name;

    @Schema(description = "商机状态类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25714")
    @DiffLogField(name = "商机状态")
    @NotNull(message = "商机状态类型不能为空")
    private Long statusTypeId;

    @Schema(description = "商机状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @DiffLogField(name = "商机状态")
    @NotNull(message = "商机状态不能为空")
    private Long statusId;

    @Schema(description = "下次联系时间")
    @DiffLogField(name = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10299")
    @DiffLogField(name = "客户", function = CrmCustomerParseFunction.NAME)
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @Schema(description = "预计成交日期")
    @DiffLogField(name = "预计成交日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime dealTime;

    @Schema(description = "商机金额", example = "12371")
    @DiffLogField(name = "商机金额")
    private Integer price;

    // TODO @ljileo：折扣使用 Integer 类型，存储时，默认 * 100；展示的时候，前端需要 / 100；避免精度丢失问题
    @Schema(description = "整单折扣")
    @DiffLogField(name = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "12025")
    @DiffLogField(name = "产品总金额")
    private BigDecimal productPrice;

    @Schema(description = "备注", example = "随便")
    @DiffLogField(name = "备注")
    private String remark;
    // TODO @ljileo：修改的时候，应该可以传递添加的产品；
    @Schema(description = "联系人编号", example = "110")
    @NotNull(message = "联系人编号不能为空")
    private Long contactId;

    @Schema(description = "1赢单2输单3无效", example = "1")
    @InEnum(CrmBizEndStatus.class)
    private Integer endStatus;

    @Schema(description = "商机产品列表", example = "")
    private List<CrmBusinessProductSaveReqVO> products = new ArrayList<>();


}
