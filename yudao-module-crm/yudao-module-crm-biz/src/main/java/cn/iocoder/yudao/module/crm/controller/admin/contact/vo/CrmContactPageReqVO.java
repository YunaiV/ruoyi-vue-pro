package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - CRM 联系人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmContactPageReqVO extends PageParam {

    @Schema(description = "姓名", example = "芋艿")
    private String name;

    @Schema(description = "客户编号", example = "10795")
    private Long customerId;

    @Schema(description = "手机号", example = "13898273941")
    private String mobile;

    @Schema(description = "电话", example = "021-383773")
    private String telephone;

    @Schema(description = "电子邮箱", example = "111@22.com")
    private String email;

    @Schema(description = "QQ", example = "3882872")
    private Long qq;

    @Schema(description = "微信", example = "zzZ98373")
    private String wechat;

}
