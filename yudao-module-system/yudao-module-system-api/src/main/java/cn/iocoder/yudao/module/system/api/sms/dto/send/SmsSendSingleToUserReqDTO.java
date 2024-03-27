package cn.iocoder.yudao.module.system.api.sms.dto.send;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * 短信发送给 Admin 或者 Member 用户
 *
 * @author 芋道源码
 */
@Data
public class SmsSendSingleToUserReqDTO {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 手机号
     */
    @Mobile
    private String mobile;
    /**
     * 短信模板编号
     */
    @NotEmpty(message = "短信模板编号不能为空")
    private String templateCode;
    /**
     * 短信模板参数
     */
    private Map<String, Object> templateParams;

}
