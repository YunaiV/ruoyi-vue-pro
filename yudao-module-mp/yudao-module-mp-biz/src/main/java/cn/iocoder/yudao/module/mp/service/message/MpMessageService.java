package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 粉丝消息表 Service 接口
 *
 * @author 芋道源码
 */
public interface MpMessageService {

    // TODO 芋艿：方法名要优化下
    /**
     * 获得粉丝消息表分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝消息表 分页
     */
    PageResult<MpMessageDO> getWxFansMsgPage(MpMessagePageReqVO pageReqVO);

    /**
     * 保存粉丝消息，来自用户发送
     *
     * @param appId 微信公众号 appId
     * @param wxMessage 消息
     */
    void createFromUser(String appId, WxMpXmlMessage wxMessage);

    /**
     * 创建粉丝消息，通过自动回复
     *
     * @param openid 公众号粉丝 openid
     * @param reply 自动回复
     * @return 微信回复消息 XML
     */
    WxMpXmlOutMessage createFromAutoReply(String openid, MpAutoReplyDO reply);

}
