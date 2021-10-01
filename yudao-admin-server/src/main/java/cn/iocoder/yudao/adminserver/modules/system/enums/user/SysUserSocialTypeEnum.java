package cn.iocoder.yudao.adminserver.modules.system.enums.user;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.adminserver.modules.system.enums.errorcode.SysErrorCodeTypeEnum;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import jodd.util.ArraysUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户的三方平台的类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SysUserSocialTypeEnum implements IntArrayValuable {

    GITEE(10, "GITEE"), // https://gitee.com/api/v5/oauth_doc#/
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SysUserSocialTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型的标识
     */
    private final String source;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static SysUserSocialTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }

}
