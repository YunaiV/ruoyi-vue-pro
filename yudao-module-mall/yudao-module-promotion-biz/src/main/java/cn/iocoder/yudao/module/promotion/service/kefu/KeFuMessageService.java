package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessageSendReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import jakarta.validation.Valid;

/**
 * 客服消息 Service 接口
 *
 * @author HUIHUI
 */
public interface KeFuMessageService {

    /**
     * 【管理员】发送客服消息
     *
     * @param sendReqVO 信息
     * @return 编号
     */
    Long sendKefuMessage(@Valid KeFuMessageSendReqVO sendReqVO);

    /**
     * 【会员】发送客服消息
     *
     * @param sendReqVO 信息
     * @return 编号
     */
    Long sendKefuMessage(AppKeFuMessageSendReqVO sendReqVO);

    /**
     * 【管理员】更新消息已读状态
     *
     * @param conversationId 会话编号
     * @param userId         用户编号
     * @param userType       用户类型
     */
    void updateKeFuMessageReadStatus(Long conversationId, Long userId, Integer userType);

    /**
     * 获得客服消息分页
     *
     * @param pageReqVO 分页查询
     * @return 客服消息分页
     */
    PageResult<KeFuMessageDO> getKeFuMessagePage(KeFuMessagePageReqVO pageReqVO);

    /**
     * 【会员】获得客服消息分页
     *
     * @param pageReqVO 请求
     * @param userId    用户编号
     * @return 客服消息分页
     */
    PageResult<KeFuMessageDO> getKeFuMessagePage(AppKeFuMessagePageReqVO pageReqVO, Long userId);

}