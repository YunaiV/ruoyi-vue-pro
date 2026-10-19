package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - PMS 知识库文档全局搜索分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeDocumentSearchPageReqVO extends PageParam {

    @Schema(description = "搜索关键字", example = "产品方案")
    private String keyword;

    @Schema(description = "知识库编号", example = "1024")
    private Long libraryId;

    @Schema(description = "创建人用户编号", example = "1")
    private Long creatorUserId;

    @Schema(description = "更新时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

}
