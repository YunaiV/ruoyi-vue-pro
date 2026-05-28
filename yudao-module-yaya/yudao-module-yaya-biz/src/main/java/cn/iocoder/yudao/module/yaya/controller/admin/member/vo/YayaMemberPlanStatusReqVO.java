package cn.iocoder.yudao.module.yaya.controller.admin.member.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaMemberPlanStatusReqVO {

    @NotNull(message = "active 不能为空")
    private Integer active;

}
