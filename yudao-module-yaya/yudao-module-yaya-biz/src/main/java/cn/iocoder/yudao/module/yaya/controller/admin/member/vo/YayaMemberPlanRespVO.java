package cn.iocoder.yudao.module.yaya.controller.admin.member.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaMemberPlanRespVO {

    private Long id;
    private String planKey;
    private String name;
    private String description;
    private Long priceCent;
    private String currency;
    private Integer durationDays;
    private Integer active;
    private Map<String, Object> benefits;

}
