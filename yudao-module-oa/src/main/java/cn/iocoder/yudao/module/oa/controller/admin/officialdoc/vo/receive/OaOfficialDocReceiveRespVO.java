package cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 公文收文 Response VO")
@Data
public class OaOfficialDocReceiveRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "来源发文编号", example = "1024")
    private Long sendId;

    @Schema(description = "发文部门", example = "行政部")
    private String sendDeptName;

    @Schema(description = "发文日期", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime issueTime;

    @Schema(description = "签发人", example = "张三")
    private String signerName;

    @Schema(description = "公开类别", example = "0")
    private Integer disclosureType;

    @Schema(description = "收文类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer receiveType;

    @Schema(description = "收文时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime receiveTime;

    @Schema(description = "收文部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long receiveDeptId;

    @Schema(description = "主办人用户编号", example = "1024")
    private Long handlerUserId;

    @Schema(description = "领导批示", example = "请行政部牵头办理")
    private String instruction;

    @Schema(description = "办理结果", example = "已完成盘点并归档")
    private String result;

    @Schema(description = "办理期限", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime deadlineTime;

    @Schema(description = "内容摘要", example = "本周已完成办公用品盘点")
    private String summary;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "单据编号", example = "CC202609140001")
    private String no;

    @Schema(description = "公文标题", example = "关于开展办公用品盘点的通知")
    private String title;

    @Schema(description = "来文字号", example = "芋办〔2026〕1号")
    private String documentNo;

    @Schema(description = "密级", example = "0")
    private Integer secrecyLevel;

    @Schema(description = "紧急程度", example = "0")
    private Integer urgencyLevel;

    @Schema(description = "正式公文地址", example = "https://example.com/file.pdf")
    private String formalFileUrl;

    @Schema(description = "收文部门", example = "行政部")
    private String receiveDeptName;

    @Schema(description = "主办人", example = "张三")
    private String handlerName;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "办理状态", example = "0")
    private Integer handleStatus;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "创建人编号", example = "1024")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
