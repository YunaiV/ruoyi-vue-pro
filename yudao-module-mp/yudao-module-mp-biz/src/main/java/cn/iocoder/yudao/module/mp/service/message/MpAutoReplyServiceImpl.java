package cn.iocoder.yudao.module.mp.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.dal.mysql.message.MpAutoReplyMapper;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyMatchEnum;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公众号的自动回复 Service 实现类
 *
 * @author 芋道源码
 */
@Resource
@Validated
public class MpAutoReplyServiceImpl implements MpAutoReplyService {

    @Resource
    private MpAutoReplyService mpAutoReplyService;

    @Resource
    private MpAutoReplyMapper mpAutoReplyMapper;

    @Override
    public WxMpXmlOutMessage replyForMessage(String appId, WxMpXmlMessage wxMessage) {
        // 第一步，匹配自动回复
        List<MpAutoReplyDO> replies = null;
        // 1.1 关键字
        if (wxMessage.getMsgType().equals(WxConsts.XmlMsgType.TEXT)) {
            // 完全匹配
            replies = mpAutoReplyMapper.selectListByAppIdAndKeywordAll(appId, wxMessage.getContent());
            if (CollUtil.isEmpty(replies)) {
                // 模糊匹配
                replies = mpAutoReplyMapper.selectListByAppIdAndKeywordLike(appId, wxMessage.getContent());
            }
        }
        // 1.2 消息类型
        if (CollUtil.isEmpty(replies)) {
            replies = mpAutoReplyMapper.selectListByAppIdAndMessage(appId, wxMessage.getMsgType());
        }
        if (CollUtil.isEmpty(replies)) {
            return null;
        }

        // 第二步，基于自动回复，创建消息


        // 第三步，将消息转换成 WxMpXmlOutMessage 返回
        return null;
    }

}
