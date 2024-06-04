package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import jakarta.validation.Valid;

/**
 * 客服消息 Service 接口
 *
 * @author HUIHUI
 */
public interface KeFuMessageService {

    /**
     * 发送客服消息
     *
     * @param sendReqVO 信息
     * @return 编号
     */
    Long sendKefuMessage(@Valid KeFuMessageSendReqVO sendReqVO);

    /**
     * 更新消息已读状态
     *
     * @param conversationId 会话编号
     * @param receiverId     用户编号
     */
    void updateKefuMessageReadStatus(Long conversationId, Long receiverId);

    /**
     * 获得客服消息分页
     *
     * @param pageReqVO 分页查询
     * @return 客服消息分页
     */
    PageResult<KeFuMessageDO> getKefuMessagePage(KeFuMessagePageReqVO pageReqVO);

}