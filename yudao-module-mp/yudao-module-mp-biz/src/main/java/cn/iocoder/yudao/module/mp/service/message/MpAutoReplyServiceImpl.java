package cn.iocoder.yudao.module.mp.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply.MpAutoReplyCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply.MpAutoReplyUpdateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.message.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.convert.message.MpAutoReplyConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.dal.mysql.message.MpAutoReplyMapper;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyTypeEnum;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 公众号的自动回复 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MpAutoReplyServiceImpl implements MpAutoReplyService {

    @Resource
    private MpMessageService mpMessageService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MpAccountService mpAccountService;

    @Resource
    private Validator validator;

    @Resource
    private MpAutoReplyMapper mpAutoReplyMapper;

    @Override
    public PageResult<MpAutoReplyDO> getAutoReplyPage(MpMessagePageReqVO pageVO) {
        return mpAutoReplyMapper.selectPage(pageVO);
    }

    @Override
    public MpAutoReplyDO getAutoReply(Long id) {
        return mpAutoReplyMapper.selectById(id);
    }

    @Override
    public Long createAutoReply(MpAutoReplyCreateReqVO createReqVO) {
        // 第一步，校验数据
        if (createReqVO.getResponseMessageType() != null) {
            MpUtils.validateMessage(validator, createReqVO.getResponseMessageType(), createReqVO);
        }
        validateAutoReplyConflict(null, createReqVO.getAccountId(), createReqVO.getType(),
                createReqVO.getRequestKeyword(), createReqVO.getRequestMessageType());

        // 第二步，插入数据
        MpAccountDO account = mpAccountService.getRequiredAccount(createReqVO.getAccountId());
        MpAutoReplyDO autoReply = MpAutoReplyConvert.INSTANCE.convert(createReqVO)
                .setAppId(account.getAppId());
        mpAutoReplyMapper.insert(autoReply);
        return autoReply.getId();
    }

    @Override
    public void updateAutoReply(MpAutoReplyUpdateReqVO updateReqVO) {
        // 第一步，校验数据
        if (updateReqVO.getResponseMessageType() != null) {
            MpUtils.validateMessage(validator, updateReqVO.getResponseMessageType(), updateReqVO);
        }
        MpAutoReplyDO autoReply = validateAutoReplyExists(updateReqVO.getId());
        validateAutoReplyConflict(updateReqVO.getId(), autoReply.getAccountId(), updateReqVO.getType(),
                updateReqVO.getRequestKeyword(), updateReqVO.getRequestMessageType());

        // 第二步，更新数据
        MpAutoReplyDO updateObj = MpAutoReplyConvert.INSTANCE.convert(updateReqVO)
                .setAccountId(null).setAppId(null); // 避免前端传递，更新着两个字段
        mpAutoReplyMapper.updateById(updateObj);
    }

    /**
     * 校验自动回复是否冲突
     *
     * 不同的 type，会有不同的逻辑：
     * 1. type = SUBSCRIBE 时，不允许有其他的自动回复
     * 2. type = MESSAGE 时，校验 requestMessageType 已经存在自动回复
     * 3. type = KEYWORD 时，校验 keyword 已经存在自动回复
     *
     * @param id 自动回复编号
     * @param accountId 公众号账号的编号
     * @param type 类型
     * @param requestKeyword 请求关键词
     * @param requestMessageType 请求消息类型
     */
    private void validateAutoReplyConflict(Long id, Long accountId, Integer type,
                                           String requestKeyword, String requestMessageType) {
        // 获得已经存在的自动回复
        MpAutoReplyDO autoReply = null;
        ErrorCode errorCode = null;
        if (MpAutoReplyTypeEnum.SUBSCRIBE.getType().equals(type)) {
            autoReply = mpAutoReplyMapper.selectByAccountIdAndSubscribe(accountId);
            errorCode = AUTO_REPLY_ADD_SUBSCRIBE_FAIL_EXISTS;
        } else if (MpAutoReplyTypeEnum.MESSAGE.getType().equals(type)) {
            autoReply = mpAutoReplyMapper.selectByAccountIdAndMessage(accountId, requestMessageType);
            errorCode = AUTO_REPLY_ADD_MESSAGE_FAIL_EXISTS;
        } else if (MpAutoReplyTypeEnum.KEYWORD.getType().equals(type)) {
            autoReply = mpAutoReplyMapper.selectByAccountIdAndKeyword(accountId, requestKeyword);
            errorCode = AUTO_REPLY_ADD_KEYWORD_FAIL_EXISTS;
        }
        if (autoReply == null) {
            return;
        }

        // 存在冲突，抛出业务异常
        if (id == null // 情况一，新增（id == null），存在记录，说明冲突
            || ObjUtil.notEqual(id, autoReply.getId())) { // 情况二，修改（id != null），id 不匹配，说明冲突
            throw exception(errorCode);
        }
    }

    @Override
    public void deleteAutoReply(Long id) {
        // 校验粉丝存在
        validateAutoReplyExists(id);

        // 删除自动回复
        mpAutoReplyMapper.deleteById(id);
    }

    private MpAutoReplyDO validateAutoReplyExists(Long id) {
        MpAutoReplyDO autoReply = mpAutoReplyMapper.selectById(id);
        if (autoReply == null) {
            throw exception(AUTO_REPLY_NOT_EXISTS);
        }
        return autoReply;
    }

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
        MpAutoReplyDO reply = CollUtil.getFirst(replies);

        // 第二步，基于自动回复，创建消息
        MpMessageSendOutReqBO sendReqBO = MpAutoReplyConvert.INSTANCE.convert(wxMessage.getFromUser(), reply);
        return mpMessageService.sendOutMessage(sendReqBO);
    }

    @Override
    public WxMpXmlOutMessage replyForSubscribe(String appId, WxMpXmlMessage wxMessage) {
        // 第一步，匹配自动回复
        List<MpAutoReplyDO> replies = mpAutoReplyMapper.selectListByAppIdAndSubscribe(appId);
        MpAutoReplyDO reply = CollUtil.isNotEmpty(replies) ? CollUtil.getFirst(replies)
                : buildDefaultSubscribeAutoReply(appId); // 如果不存在，提供一个默认末班

        // 第二步，基于自动回复，创建消息
        MpMessageSendOutReqBO sendReqBO = MpAutoReplyConvert.INSTANCE.convert(wxMessage.getFromUser(), reply);
        return mpMessageService.sendOutMessage(sendReqBO);
    }

    private MpAutoReplyDO buildDefaultSubscribeAutoReply(String appId) {
        MpAccountDO account = mpAccountService.getAccountFromCache(appId);
        Assert.notNull(account, "公众号账号({}) 不存在", appId);
        // 构建默认的【关注】自动回复
        return new MpAutoReplyDO().setAppId(appId).setAccountId(account.getId())
                .setType(MpAutoReplyTypeEnum.SUBSCRIBE.getType())
                .setResponseMessageType(WxConsts.XmlMsgType.TEXT).setResponseContent("感谢关注");
    }

}
