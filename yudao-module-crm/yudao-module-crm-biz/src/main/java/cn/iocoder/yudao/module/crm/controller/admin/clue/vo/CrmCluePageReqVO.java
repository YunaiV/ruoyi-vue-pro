package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Schema(description = "管理后台 - 线索分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCluePageReqVO extends PageParam {

    @Schema(description = "线索名称", example = "线索xxx")
    private String name;

    @Schema(description = "转化状态")
    private Boolean transformStatus;

    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    @Schema(description = "手机号", example = "18000000000")
    private String mobile;

    @Schema(description = "场景类型")
    @InEnum(CrmSceneTypeEnum.class)
    private Integer sceneType; // 场景类型，为 null 时则表示全部

    @Schema(description = "所属行业")
    private Integer industryId;

    @Schema(description = "客户等级")
    private Integer level;

    @Schema(description = "客户来源")
    private Integer source;

    @Schema(description = "跟进状态", example = "true")
    private Boolean followUpStatus;

    //公司介绍
    @Schema(description = "公司介绍", example = "北京")
    private String companyIntroduction;
    //官网
    @Schema(description = "官网", example = "www.baidu.com")
    private String companyWebsite;
}
