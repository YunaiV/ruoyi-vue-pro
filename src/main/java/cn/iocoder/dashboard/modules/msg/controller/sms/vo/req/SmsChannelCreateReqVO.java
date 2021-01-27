package cn.iocoder.dashboard.modules.msg.controller.sms.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@ApiModel("消息渠道创建 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SmsChannelCreateReqVO implements Serializable {

    @ApiModelProperty("编码(来自枚举类 阿里、华为、七牛等)")
    private String code;

    @ApiModelProperty("渠道账号id")
    private String apiKey;

    @ApiModelProperty("渠道账号秘钥")
    private String apiSecret;

    @ApiModelProperty("优先级(存在多个签名时,选择值最小的,渠道不可用时,按优先级从小到大切换)")
    private Integer priority;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("签名值")
    private String signature;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("启用状态（0正常 1停用）")
    private Integer status;

}
