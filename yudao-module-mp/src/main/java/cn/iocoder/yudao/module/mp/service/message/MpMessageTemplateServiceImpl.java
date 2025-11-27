package cn.iocoder.yudao.module.mp.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateListReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateSendReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageTemplateDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.dal.mysql.message.MpMessageTemplateMapper;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.template.WxMpTemplate;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 公众号模版消息 Service 实现类
 *
 * @author dengsl
 */
@Service
@Validated
@Slf4j
public class MpMessageTemplateServiceImpl implements MpMessageTemplateService {

    @Resource
    @Lazy // 延迟加载，为了解决延迟加载
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpMessageTemplateMapper messageTemplateMapper;

    @Resource
    private MpAccountService mpAccountService;

    @Resource
    private MpUserService mpUserService;

    @Override
    public void deleteMessageTemplate(Long id) {
        // 校验存在
        MpMessageTemplateDO template = validateMsgTemplateExists(id);

        // 第一步，删除模板到公众号平台
        try {
            mpServiceFactory.getRequiredMpService(template.getAppId())
                    .getTemplateMsgService().delPrivateTemplate(template.getTemplateId());
        } catch (WxErrorException e) {
            throw exception(MESSAGE_TEMPLATE_DELETE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，删除模板到数据库
        messageTemplateMapper.deleteById(id);
    }

    private MpMessageTemplateDO validateMsgTemplateExists(Long id) {
        MpMessageTemplateDO template = messageTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(MESSAGE_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public MpMessageTemplateDO getMessageTemplate(Long id) {
        return messageTemplateMapper.selectById(id);
    }

    @Override
    public List<MpMessageTemplateDO> getMessageTemplateList(MpMessageTemplateListReqVO listReqVO) {
        return messageTemplateMapper.selectList(listReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncMessageTemplate(Long accountId) {
        MpAccountDO account = mpAccountService.getRequiredAccount(accountId);

        // 第一步，从公众号平台获取最新的模板列表
        List<WxMpTemplate> wxTemplates;
        try {
            wxTemplates = mpServiceFactory.getRequiredMpService(accountId)
                    .getTemplateMsgService().getAllPrivateTemplate();
        } catch (WxErrorException e) {
            throw exception(MESSAGE_TEMPLATE_SYNC_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，合并更新回自己的数据库
        Map<String, MpMessageTemplateDO> templateMap = convertMap(
                messageTemplateMapper.selectList(new LambdaQueryWrapperX<MpMessageTemplateDO>()
                        .eq(MpMessageTemplateDO::getAppId, account.getAppId())),
                MpMessageTemplateDO::getTemplateId);
        wxTemplates.forEach(wxTemplate -> {
            MpMessageTemplateDO template = templateMap.remove(wxTemplate.getTemplateId());
            // 情况一，不存在，新增
            if (template == null) {
                template = new MpMessageTemplateDO().setAccountId(account.getId()).setAppId(account.getAppId())
                        .setTemplateId(wxTemplate.getTemplateId()).setTitle(wxTemplate.getTitle())
                        .setContent(wxTemplate.getContent()).setExample(wxTemplate.getExample())
                        .setPrimaryIndustry(wxTemplate.getPrimaryIndustry()).setDeputyIndustry(wxTemplate.getDeputyIndustry());
                messageTemplateMapper.insert(template);
                return;
            }
            // 情况二，存在，则更新
            messageTemplateMapper.updateById(new MpMessageTemplateDO().setId(template.getId())
                    .setTitle(wxTemplate.getTitle()).setContent(wxTemplate.getContent()).setExample(wxTemplate.getExample())
                    .setPrimaryIndustry(wxTemplate.getPrimaryIndustry()).setDeputyIndustry(wxTemplate.getDeputyIndustry()));
        });
        // 情况三，部分模板已经不存在了，删除
        if (CollUtil.isNotEmpty(templateMap)) {
            messageTemplateMapper.deleteByIds(convertList(templateMap.values(), MpMessageTemplateDO::getId));
        }
    }

    @Override
    public void sendMessageTempalte(MpMessageTemplateSendReqVO sendReqVO) {
        // 获得关联信息
        MpUserDO user = mpUserService.getRequiredUser(sendReqVO.getUserId());
        MpMessageTemplateDO template = validateMsgTemplateExists(sendReqVO.getId());

        // 发送模版消息
        WxMpTemplateMessage templateMessage = buildTemplateMessage(template, user, sendReqVO);
        try {
            mpServiceFactory.getRequiredMpService(template.getAppId())
                    .getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            throw exception(MESSAGE_TEMPLATE_SEND_FAIL, e.getError().getErrorMsg());
        }

        // 不用记录 MpMessageDO 记录，因为，微信会主动推送，可见文档 https://developers.weixin.qq.com/doc/service/guide/product/template_message/Template_Message_Interface.html
    }

    private WxMpTemplateMessage buildTemplateMessage(MpMessageTemplateDO msgTemplateDO, MpUserDO user,
                                                     MpMessageTemplateSendReqVO sendReqVO) {
        List<WxMpTemplateData> data = new ArrayList<>();
        WxMpTemplateMessage.WxMpTemplateMessageBuilder builder = WxMpTemplateMessage.builder()
                .templateId(msgTemplateDO.getTemplateId())
                .data(data)
                .toUser(user.getOpenid());
        // 设置跳转链接
        if (StrUtil.isNotBlank(sendReqVO.getUrl())) {
            builder.url(sendReqVO.getUrl());
        }
        // 设置小程序跳转
        if (StrUtil.isNotBlank(sendReqVO.getMiniprogram())) {
            // https://developers.weixin.qq.com/doc/service/api/notify/template/api_sendtemplatemessage.html#Body__miniprogram
            builder.miniProgram(JsonUtils.parseObject(sendReqVO.getMiniprogram(), WxMpTemplateMessage.MiniProgram.class));
        }
        // 设置模板数据
        if (sendReqVO.getData() != null) {
            sendReqVO.getData().forEach((key, value) -> data.add(new WxMpTemplateData(key, value)));
        }
        return builder.build();
    }

}