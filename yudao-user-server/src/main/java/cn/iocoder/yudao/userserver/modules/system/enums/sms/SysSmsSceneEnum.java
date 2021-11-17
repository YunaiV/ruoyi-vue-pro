package cn.iocoder.yudao.userserver.modules.system.enums.sms;

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

    LOGIN_BY_SMS(1, "手机号登陆"),
    CHANGE_MOBILE_BY_SMS(2, "更换手机号"),
    FORGET_MOBILE_BY_SMS(3, "忘记密码"),
    CHECK_CODE_BY_SMS(4, "审核验证码"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SysSmsSceneEnum::getScene).toArray();

    private final Integer scene;
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
