package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - 云盘文件新增 Request VO")
@Data
public class OaFileNodeSaveReqVO {

    @Schema(description = "父目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "父目录编号不能为空")
    private Long parentId;

    @Schema(description = "节点类型：0 文件夹、1 文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "节点类型不能为空")
    @InEnum(value = OaFileNodeTypeEnum.class, message = "节点类型必须是 {value}")
    private Integer type;

    @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "办公用品盘点结果.pdf")
    @NotBlank(message = "节点名称不能为空")
    @Size(max = 255, message = "节点名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "上传文件地址，文件夹不传", example = "https://example.com/file.pdf")
    @Size(max = 2048, message = "文件地址长度不能超过 2048 个字符")
    private String url;

    @Schema(description = "文件大小，单位字节，文件夹不传", example = "1024")
    private Long size;

    @AssertTrue(message = "文件地址不能为空")
    @JsonIgnore
    public boolean isUrlValid() {
        return ObjUtil.notEqual(type, OaFileNodeTypeEnum.FILE.getType()) || StrUtil.isNotBlank(url);
    }

    @AssertTrue(message = "文件大小不能为空且不能小于 0")
    @JsonIgnore
    public boolean isSizeValid() {
        return ObjUtil.notEqual(type, OaFileNodeTypeEnum.FILE.getType()) || (size != null && size >= 0);
    }

}
