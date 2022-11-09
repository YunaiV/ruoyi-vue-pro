package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "管理后台 - 文件 Response VO", description = "不返回 content 字段，太大")
@Data
public class FileRespVO {

    @ApiModelProperty(value = "文件编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "配置编号", required = true, example = "11")
    private Long configId;

    @ApiModelProperty(value = "文件路径", required = true, example = "yudao.jpg")
    private String path;

    @ApiModelProperty(value = "原文件名", required = true, example = "yudao.jpg")
    private String name;

    @ApiModelProperty(value = "文件 URL", required = true, example = "https://www.iocoder.cn/yudao.jpg")
    private String url;

    @ApiModelProperty(value = "文件MIME类型", example = "application/octet-stream")
    private String type;

    @ApiModelProperty(value = "文件大小", example = "2048", required = true)
    private Integer size;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
