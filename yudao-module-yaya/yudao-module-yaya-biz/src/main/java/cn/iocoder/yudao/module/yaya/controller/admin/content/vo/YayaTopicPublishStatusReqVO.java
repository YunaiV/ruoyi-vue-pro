package cn.iocoder.yudao.module.yaya.controller.admin.content.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaTopicPublishStatusReqVO {

    @NotBlank(message = "publishStatus 不能为空")
    private String publishStatus;

}
