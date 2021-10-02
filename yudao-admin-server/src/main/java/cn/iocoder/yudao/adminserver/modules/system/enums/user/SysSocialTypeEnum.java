package cn.iocoder.yudao.adminserver.modules.system.enums.user;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 社交用户的类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SysSocialTypeEnum implements IntArrayValuable {

    GITEE(10, "GITEE"), // https://gitee.com/api/v5/oauth_doc#/
    DINGTALK(20, "DINGTALK"), // https://developers.dingtalk.com/document/app/obtain-identity-credentials
    WECHAT_ENTERPRISE(30, "WECHAT_ENTERPRISE"), // https://xkcoding.com/2019/08/06/use-justauth-integration-wechat-enterprise.html
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SysSocialTypeEnum::getType).toArray();

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

    public static SysSocialTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }

}
