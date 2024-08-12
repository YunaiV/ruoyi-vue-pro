package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmProductRespVO implements VO {

    @Schema(description = "产品编号", example = "20529")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "好产品")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12306")
    @ExcelProperty("产品编码")
    private String no;

    @Schema(description = "单位", example = "2")
    @ExcelProperty(value = "单位", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CRM_PRODUCT_UNIT)
    private Integer unit;

    @Schema(description = "价格, 单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "8911")
    @ExcelProperty("价格，单位：分")
    private BigDecimal price;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "上架")
    @ExcelProperty(value = "单位", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CRM_PRODUCT_STATUS)
    private Integer status;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @Trans(type = TransType.SIMPLE, target = CrmProductCategoryDO.class, fields = "name", ref = "categoryName")
    private Long categoryId;
    @Schema(description = "产品分类名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "衣服")
    @ExcelProperty("产品分类")
    private String categoryName;

    @Schema(description = "产品描述", example = "你说的对")
    @ExcelProperty("产品描述")
    private String description;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31926")
    @Trans(type = TransType.SIMPLE, targetClassName = "cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO",
            fields = "nickname", ref = "ownerUserName")
    private Long ownerUserId;
    @Schema(description = "负责人的用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    @ExcelProperty("负责人")
    private String ownerUserName;

    @Schema(description = "创建人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @Trans(type = TransType.SIMPLE, targetClassName = "cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO",
            fields = "nickname", ref = "creatorName")
    private String creator;
    @Schema(description = "创建人名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

}
