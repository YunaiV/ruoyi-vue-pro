package cn.iocoder.yudao.module.system.service.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialClientDO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.xingyuv.jushauth.model.AuthUser;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;

import java.util.List;

/**
 * 社交应用 Service 接口
 *
 * @author 芋道源码
 */
public interface SocialClientService {

    /**
     * 获得社交平台的授权 URL
     *
     * @param socialType  社交平台的类型 {@link SocialTypeEnum}
     * @param userType    用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);

    /**
     * 请求社交平台，获得授权的用户
     *
     * @param socialType 社交平台的类型
     * @param userType   用户类型
     * @param code       授权码
     * @param state      授权 state
     * @return 授权的用户
     */
    AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state);

    // =================== 微信公众号独有 ===================

    /**
     * 创建微信公众号的 JS SDK 初始化所需的签名
     *
     * @param userType 用户类型
     * @param url      访问的 URL 地址
     * @return 签名
     */
    WxJsapiSignature createWxMpJsapiSignature(Integer userType, String url);

    // =================== 微信小程序独有 ===================

    /**
     * 获得微信小程序的手机信息
     *
     * @param userType  用户类型
     * @param phoneCode 手机授权码
     * @return 手机信息
     */
    WxMaPhoneNumberInfo getWxMaPhoneNumberInfo(Integer userType, String phoneCode);

    /**
     * 获得小程序二维码
     *
     * @param reqVO 请求信息
     * @return 小程序二维码
     */
    byte[] getWxaQrcode(SocialWxQrcodeReqDTO reqVO);

    /**
     * 获得微信小程订阅模板
     *
     * 缓存的目的：考虑到微信小程序订阅消息选择好模版后几乎不会变动，缓存增加查询效率
     *
     * @param userType 用户类型
     * @return 微信小程订阅模板
     */
    List<TemplateInfo> getSubscribeTemplateList(Integer userType);

    /**
     * 发送微信小程序订阅消息
     *
     * @param reqDTO     请求
     * @param templateId 模版编号
     * @param openId     会员 openId
     */
    void sendSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO, String templateId, String openId);

    // =================== 客户端管理 ===================

    /**
     * 创建社交客户端
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSocialClient(@Valid SocialClientSaveReqVO createReqVO);

    /**
     * 更新社交客户端
     *
     * @param updateReqVO 更新信息
     */
    void updateSocialClient(@Valid SocialClientSaveReqVO updateReqVO);

    /**
     * 删除社交客户端
     *
     * @param id 编号
     */
    void deleteSocialClient(Long id);

    /**
     * 获得社交客户端
     *
     * @param id 编号
     * @return 社交客户端
     */
    SocialClientDO getSocialClient(Long id);

    /**
     * 获得社交客户端分页
     *
     * @param pageReqVO 分页查询
     * @return 社交客户端分页
     */
    PageResult<SocialClientDO> getSocialClientPage(SocialClientPageReqVO pageReqVO);

}
