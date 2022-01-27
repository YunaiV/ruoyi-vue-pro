package cn.iocoder.yudao.module.member.enums.sms;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户短信验证码发送场景的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SysSmsSceneEnum implements IntArrayValuable {

    LOGIN_BY_SMS(1,SysSmsTemplateCodeConstants.USER_SMS_LOGIN, "手机号登陆"),
    CHANGE_MOBILE_BY_SMS(2,SysSmsTemplateCodeConstants.USER_SMS_UPDATE_MOBILE, "更换手机号"),
    FORGET_MOBILE_BY_SMS(3,SysSmsTemplateCodeConstants.USER_SMS_RESET_PASSWORD, "忘记密码"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SysSmsSceneEnum::getScene).toArray();

    /**
     * 验证那场景编号
     */
    private final Integer scene;

    /**
     * 模版编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static String getCodeByScene(Integer scene){
        for (SysSmsSceneEnum value : values()) {
            if (value.getScene().equals(scene)){
                return value.getCode();
            }
        }
        return null;
    }

}
