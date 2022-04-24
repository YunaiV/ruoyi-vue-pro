package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "管理后台 - 简单上传文件 VO", description = "简单上传文件, 不需要 path")
public class SimpleUploadRespVO {

    @ApiModelProperty(value = "文件名", required = true, example = "yudao.jpg")
    private String fileName;

    @ApiModelProperty(value = "文件 URL", required = true, example = "https://www.iocoder.cn/yudao.jpg")
    private String fileUrl;
}
