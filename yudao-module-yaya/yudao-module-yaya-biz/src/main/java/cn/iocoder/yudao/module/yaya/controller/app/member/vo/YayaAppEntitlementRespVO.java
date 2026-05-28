package cn.iocoder.yudao.module.yaya.controller.app.member.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class YayaAppEntitlementRespVO {

    private Boolean active;
    private String planKey;
    private LocalDateTime endsAt;

}
