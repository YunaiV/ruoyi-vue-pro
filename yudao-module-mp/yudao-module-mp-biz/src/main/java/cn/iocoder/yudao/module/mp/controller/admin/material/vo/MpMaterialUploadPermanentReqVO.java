package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 公众号素材上传永久 Request VO")
@Data
public class MpMaterialUploadPermanentReqVO {

    @Schema(description = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    @Schema(description = "文件类型 参见 WxConsts.MediaFileType 枚举", required = true, example = "image")
    @NotEmpty(message = "文件类型不能为空")
    private String type;

    @Schema(description = "文件附件", required = true)
    @NotNull(message = "文件不能为空")
    @JsonIgnore // 避免被操作日志，进行序列化，导致报错
    private MultipartFile file;

    @Schema(description = "名字 如果 name 为空，则使用 file 文件名", example = "wechat.mp")
    private String name;

    @Schema(description = "视频素材的标题 文件类型为 video 时，必填", example = "视频素材的标题")
    private String title;
    @Schema(description = "视频素材的描述 文件类型为 video 时，必填", example = "视频素材的描述")
    private String introduction;

    @AssertTrue(message = "标题不能为空")
    public boolean isTitleValid() {
        // 生成场景为管理后台时，必须设置上级菜单，不然生成的菜单 SQL 是无父级菜单的
        return ObjectUtil.notEqual(type, WxConsts.MediaFileType.VIDEO)
                || title != null;
    }

    @AssertTrue(message = "描述不能为空")
    public boolean isIntroductionValid() {
        // 生成场景为管理后台时，必须设置上级菜单，不然生成的菜单 SQL 是无父级菜单的
        return ObjectUtil.notEqual(type, WxConsts.MediaFileType.VIDEO)
                || introduction != null;
    }

}
