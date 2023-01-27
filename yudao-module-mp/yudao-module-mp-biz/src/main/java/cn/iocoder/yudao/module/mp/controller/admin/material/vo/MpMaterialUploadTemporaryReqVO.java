package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号素材上传临时 Request VO")
@Data
public class MpMaterialUploadTemporaryReqVO {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    @ApiModelProperty(value = "文件类型", required = true, example = "image", notes = "参见 WxConsts.MediaFileType 枚举")
    @NotEmpty(message = "文件类型不能为空")
    private String type;

    @ApiModelProperty(value = "文件附件", required = true)
    @NotNull(message = "文件不能为空")
    @JsonIgnore // 避免被操作日志，进行序列化，导致报错
    private MultipartFile file;

}
