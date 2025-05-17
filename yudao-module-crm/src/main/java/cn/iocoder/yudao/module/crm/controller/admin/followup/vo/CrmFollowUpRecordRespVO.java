package cn.iocoder.yudao.module.crm.controller.admin.followup.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_FOLLOW_UP_TYPE;

@Schema(description = "管理后台 - 跟进记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmFollowUpRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28800")
    private Long id;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer bizType;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5564")
    private Long bizId;

    @Schema(description = "跟进类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @DictFormat(CRM_FOLLOW_UP_TYPE)
    private Integer type;

    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "下次联系时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime nextTime;

    @Schema(description = "关联的商机编号数组")
    private List<Long> businessIds;
    @Schema(description = "关联的商机数组")
    private List<CrmBusinessRespVO> businesses;

    @Schema(description = "关联的联系人编号数组")
    private List<Long> contactIds;
    @Schema(description = "关联的联系人名称数组")
    private List<CrmBusinessRespVO> contacts;

    @Schema(description = "图片")
    private List<String> picUrls;
    @Schema(description = "附件")
    private List<String> fileUrls;

    @Schema(description = "创建人", example = "1024")
    @ExcelProperty("创建人")
    private String creator;
    @Schema(description = "创建人名字", example = "芋道源码")
    @ExcelProperty("创建人名字")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}