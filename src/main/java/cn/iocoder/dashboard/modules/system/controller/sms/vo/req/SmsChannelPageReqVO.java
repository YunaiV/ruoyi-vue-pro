package cn.iocoder.dashboard.modules.system.controller.sms.vo.req;

import cn.iocoder.dashboard.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@ApiModel("消息渠道分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsChannelPageReqVO extends PageParam {

    @ApiModelProperty(value = "渠道名", example = "阿里", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "签名值", example = "源码", notes = "模糊匹配")
    private String signature;

}
