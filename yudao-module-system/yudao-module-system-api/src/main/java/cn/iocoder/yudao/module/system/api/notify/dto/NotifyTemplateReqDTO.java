package cn.iocoder.yudao.module.system.api.notify.dto;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class NotifyTemplateReqDTO {

    @NotEmpty(message = "模版名称不能为空")
    private String name;

    @NotNull(message = "模版编码不能为空")
    private String code;

    @NotNull(message = "模版类型不能为空")
    private Integer type;

    @NotEmpty(message = "发送人名称不能为空")
    private String nickname;

    @NotEmpty(message = "模版内容不能为空")
    private String content;

    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    private String remark;

}
