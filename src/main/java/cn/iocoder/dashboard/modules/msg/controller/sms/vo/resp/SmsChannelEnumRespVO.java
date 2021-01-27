package cn.iocoder.dashboard.modules.msg.controller.sms.vo.resp;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@ApiModel("用户分页 Request VO")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class SmsChannelEnumRespVO implements Serializable {

    private String code;

    private String name;

}
