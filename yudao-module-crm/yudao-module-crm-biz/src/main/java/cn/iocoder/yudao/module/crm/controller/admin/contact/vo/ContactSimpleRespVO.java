package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - CRM 联系人 Response VO")
@Data
@ToString(callSuper = true)
public class ContactSimpleRespVO {
    @Schema(description = "姓名", example = "芋艿") // TODO @zyna：requiredMode = Schema.RequiredMode.REQUIRED；需要空一行；字段的顺序改下，id 在 name 前面，会更干净
    private String name;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3167")
    private Long id;

}
