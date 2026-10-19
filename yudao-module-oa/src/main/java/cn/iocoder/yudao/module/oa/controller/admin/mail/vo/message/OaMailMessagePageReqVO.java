package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailFolderKeyEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 邮件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaMailMessagePageReqVO extends PageParam {

    @Schema(description = "邮箱账号编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "邮箱账号编号不能为空")
    private Long accountId;

    @Schema(description = "文件夹类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "INBOX")
    @NotBlank(message = "文件夹标识不能为空")
    private String folderKey;

    @Schema(description = "主题或发件人", example = "项目评审")
    @Size(max = 200, message = "搜索关键字不能超过 200 个字符")
    private String keyword;

    @Schema(description = "是否已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "是否有附件", example = "false")
    private Boolean hasAttach;

    /**
     * 校验文件夹入口
     *
     * @return 是否为标准入口或合法的自定义目录编号
     */
    @AssertTrue(message = "文件夹标识不正确")
    @JsonIgnore
    public boolean isFolderKeyValid() {
        if (StrUtil.isBlank(folderKey) || ArrayUtil.contains(OaMailFolderKeyEnum.ARRAYS, folderKey)) {
            return true; // 空值由 NotBlank 校验
        }
        return folderKey.matches("[1-9][0-9]{0,17}");
    }

}
