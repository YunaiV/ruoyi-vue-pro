package cn.iocoder.yudao.module.oa.controller.admin.contact.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - OA 外部联系人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaContactPageReqVO extends PageParam {

    @Schema(description = "姓名、拼音、手机或公司名称关键字", example = "张三")
    private String keyword;

    @Schema(description = "分类编号", example = "1024")
    private Long categoryId;

    @Schema(description = "是否已处理", example = "false")
    private Boolean handleStatus;

    @Schema(description = "姓名拼音首字母，空字符串表示全部", example = "A")
    @Pattern(regexp = "^[A-Z]?$", message = "姓名拼音首字母必须为 A-Z")
    private String alphabet;


}
