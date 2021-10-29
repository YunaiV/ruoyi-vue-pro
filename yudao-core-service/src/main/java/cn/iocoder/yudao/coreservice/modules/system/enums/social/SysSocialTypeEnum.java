package cn.iocoder.yudao.coreservice.modules.system.enums.social;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 社交平台的类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SysSocialTypeEnum implements IntArrayValuable {

    GITEE(10, "GITEE"), // https://gitee.com/api/v5/oauth_doc#/
    DINGTALK(20, "DINGTALK"), // https://developers.dingtalk.com/document/app/obtain-identity-credentials
    /**
     * 企业微信
     */
    WECHAT_ENTERPRISE(30, "WECHAT_ENTERPRISE"), // https://xkcoding.com/2019/08/06/use-justauth-integration-wechat-enterprise.html
    /**
     * 微信公众平台 - 移动端H5
     */
    WECHAT_MP(31, "WECHAT_MP"), // https://www.cnblogs.com/juewuzhe/p/11905461.html
    /**
     * 微信开放平台 - 网站应用 pc端扫码授权登录
     */
    WECHAT_OPEN(32, "WECHAT_OPEN"), // https://justauth.wiki/guide/oauth/wechat_open/#_2-%E7%94%B3%E8%AF%B7%E5%BC%80%E5%8F%91%E8%80%85%E8%B5%84%E8%B4%A8%E8%AE%A4%E8%AF%81
    /**
     * 微信小程序
     */
    WECHAT_MINI_PROGRAM(33, "WECHAT_MINI_PROGRAM"), // https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SysSocialTypeEnum::getType).toArray();

    public static final List<Integer> WECHAT_ALL = ListUtil.toList(WECHAT_ENTERPRISE.type, WECHAT_MP.type, WECHAT_OPEN.type,
            WECHAT_MINI_PROGRAM.type);

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

    public static List<Integer> getRelationTypes(Integer type) {
        if (WECHAT_ALL.contains(type)) {
            return WECHAT_ALL;
        }
        return ListUtil.toList(type);
    }

}
