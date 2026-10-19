package cn.iocoder.yudao.module.oa.controller.admin.contact.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 外部联系人新增/修改 Request VO")
@Data
public class OaContactSaveReqVO {

    @Schema(description = "联系人编号", example = "1024")
    private Long id;

    @Schema(description = "分类编号", example = "1024")
    private Long categoryId;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50 个字符")
    private String name;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "手机号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "手机号码不能为空")
    @Mobile
    @Size(max = 20, message = "手机号码长度不能超过 20 个字符")
    private String mobile;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @Schema(description = "地址", example = "上海市浦东新区世纪大道 100 号")
    @Size(max = 255, message = "地址长度不能超过 255 个字符")
    private String address;

    @Schema(description = "公司名称", example = "芋道科技有限公司")
    @Size(max = 100, message = "公司名称长度不能超过 100 个字符")
    private String companyName;

    @Schema(description = "公司电话", example = "021-12345678")
    @Size(max = 30, message = "公司电话长度不能超过 30 个字符")
    private String companyPhone;

    @Schema(description = "头像地址", example = "https://example.com/image.png")
    @Size(max = 512, message = "头像地址长度不能超过 512 个字符")
    private String avatar;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
