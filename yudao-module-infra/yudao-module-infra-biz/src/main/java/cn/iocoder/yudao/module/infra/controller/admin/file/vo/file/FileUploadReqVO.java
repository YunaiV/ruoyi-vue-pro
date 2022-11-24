package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@ApiModel(value = "管理后台 - 上传文件 Request VO")
@Data
public class FileUploadReqVO {

    @ApiModelProperty(value = "文件附件", required = true)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @ApiModelProperty(value = "文件附件", example = "yudaoyuanma.png")
    private String path;

}
