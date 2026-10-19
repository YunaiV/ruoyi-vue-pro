package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 公文发文 Response VO")
@Data
public class OaOfficialDocSendRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "套红模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long templateId;

    @Schema(description = "公文标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "关于开展办公用品盘点的通知")
    private String title;

    @Schema(description = "字号", example = "芋办")
    private String noPrefix;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "第几号文", example = "1")
    private Integer sequence;

    @Schema(description = "密级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer secrecyLevel;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer urgencyLevel;

    @Schema(description = "公开类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer disclosureType;

    @Schema(description = "发文日期", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime issueTime;

    @Schema(description = "发文部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long sendDeptId;

    @Schema(description = "主送部门编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    private List<Long> mainDeptIds;

    @Schema(description = "抄送部门编号列表", example = "[1, 2]")
    private List<Long> copyDeptIds;

    @Schema(description = "公文正文", example = "请各部门于本周五前完成办公用品盘点并提交结果。")
    private String content;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "正式公文地址", example = "https://example.com/file.pdf")
    private String formalFileUrl;

    @Schema(description = "附注", example = "请按要求办理并反馈结果")
    private String remark;

    @Schema(description = "单据编号", example = "CC202609140001")
    private String no;

    @Schema(description = "公文文号", example = "芋办〔2026〕1号")
    private String documentNo;

    @Schema(description = "签发人编号", example = "1024")
    private Long signerUserId;

    @Schema(description = "签发人", example = "张三")
    private String signerName;

    @Schema(description = "发文部门", example = "行政部")
    private String sendDeptName;

    @Schema(description = "主送部门", example = "[\"行政部\"]")
    private List<String> mainDeptNames;

    @Schema(description = "抄送部门", example = "[\"财务部\"]")
    private List<String> copyDeptNames;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "创建人编号", example = "1024")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
