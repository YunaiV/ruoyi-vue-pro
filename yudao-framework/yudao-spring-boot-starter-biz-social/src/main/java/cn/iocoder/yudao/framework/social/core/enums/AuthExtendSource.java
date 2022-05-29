package cn.iocoder.yudao.framework.social.core.enums;

import me.zhyd.oauth.config.AuthSource;

/**
 * 拓展 JustAuth 各 api 需要的 url， 用枚举类分平台类型管理
 *
 * 默认配置 {@link me.zhyd.oauth.config.AuthDefaultSource}
 *
 * @author timfruit
 */
public enum AuthExtendSource implements AuthSource {

    /**
     * 微信小程序授权登录
     */
    WECHAT_MINI_PROGRAM {

        @Override
        public String authorize() {
            // https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html
            throw new UnsupportedOperationException("不支持获取授权url, 请使用小程序内置函数wx.login()登录获取code");
        }

        @Override
        public String accessToken() {
            // https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
            // 获取openid, unionid , session_key
            return "https://api.weixin.qq.com/sns/jscode2session";
        }

        @Override
        public String userInfo() {
            //https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
            throw new UnsupportedOperationException("不支持获取用户信息url, 请使用小程序内置函数wx.getUserProfile()获取用户信息");
        }
    }

}
