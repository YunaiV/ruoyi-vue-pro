package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateListReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.template.MpMessageTemplateSendReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageTemplateDO;

import java.util.List;

/**
 * 公众号模版消息 Service 接口
 *
 * @author dengsl
 */
public interface MpMessageTemplateService {

    /**
     * 删除模版消息
     *
     * @param id 编号
     */
    void deleteMessageTemplate(Long id);

    /**
     * 获得模版消息
     *
     * @param id 编号
     * @return 模版消息
     */
    MpMessageTemplateDO getMessageTemplate(Long id);

    /**
     * 获得模版消息列表
     *
     * @param listReqVO 查询条件
     * @return 模版消息列表
     */
    List<MpMessageTemplateDO> getMessageTemplateList(MpMessageTemplateListReqVO listReqVO);

    /**
     * 同步公众号已添加的模版消息
     *
     * @param accountId 公众号账号的编号
     */
    void syncMessageTemplate(Long accountId);

    /**
     * 使用公众号，给粉丝发送【模版】消息
     *
     * @param sendReqVO 消息内容
     */
    void sendMessageTempalte(MpMessageTemplateSendReqVO sendReqVO);

}