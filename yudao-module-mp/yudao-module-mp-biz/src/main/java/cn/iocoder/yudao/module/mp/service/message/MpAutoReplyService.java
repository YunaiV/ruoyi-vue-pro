package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.message.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 公众号的自动回复 Service 接口
 *
 * @author 芋道源码
 */
public interface MpAutoReplyService {

    /**
     * 获得公众号自动回复分页
     *
     * @param pageVO 分页请求
     * @return 自动回复分页结果
     */
    PageResult<MpAutoReplyDO> getAutoReplyPage(MpMessagePageReqVO pageVO);

    /**
     * 获得公众号自动回复
     *
     * @param id 编号
     * @return 自动回复
     */
    MpAutoReplyDO getAutoReply(Long id);

    /**
     * 当收到消息时，自动回复
     *
     * @param appId 微信公众号 appId
     * @param wxMessage 消息
     * @return 回复的消息
     */
    WxMpXmlOutMessage replyForMessage(String appId, WxMpXmlMessage wxMessage);

    /**
     * 当用户关注时，自动回复
     *
     * @param appId 微信公众号 appId
     * @param wxMessage 消息
     * @return 回复的消息
     */
    WxMpXmlOutMessage replyForSubscribe(String appId, WxMpXmlMessage wxMessage);
}
