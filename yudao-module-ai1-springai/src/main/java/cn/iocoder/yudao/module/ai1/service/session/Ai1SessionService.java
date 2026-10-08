package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 会话 Service 接口
 *
 * 「我的」系列方法按登录用户校验归属，避免越权访问他人会话
 *
 * @author 芋道源码
 */
public interface Ai1SessionService {

    /**
     * 创建我的会话；后台会话不要求 Agent 已发布
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSessionMy(Long userId, @Valid Ai1SessionCreateMyReqVO createReqVO);

    /**
     * 修改我的会话标题
     *
     * @param userId      用户编号
     * @param updateReqVO 修改信息
     */
    void updateSessionMy(Long userId, @Valid Ai1SessionUpdateMyReqVO updateReqVO);

    /**
     * 删除我的会话，连带删除消息
     *
     * @param userId 用户编号
     * @param id     编号
     */
    void deleteSessionMy(Long userId, Long id);

    /**
     * 校验会话存在，且归属于指定用户
     *
     * @param userId 用户编号
     * @param id     编号
     * @return 会话
     */
    Ai1SessionDO validateSessionMy(Long userId, Long id);

    /**
     * 获得指定用户在指定 Agent 下的会话列表，按编号倒序
     *
     * @param agentId Agent 编号
     * @param userId  用户编号
     * @return 会话列表
     */
    List<Ai1SessionDO> getSessionListByAgentIdAndUserId(Long agentId, Long userId);

    /**
     * 获得会话分页（管理端查看 Agent 的全部会话）
     *
     * @param pageReqVO 分页查询
     * @return 会话分页
     */
    PageResult<Ai1SessionDO> getSessionPage(Ai1SessionPageReqVO pageReqVO);

    /**
     * 修改会话标题（首条消息发送时自动命名）
     *
     * @param id    编号
     * @param title 标题
     */
    void updateSessionTitle(Long id, String title);

    /**
     * 刷新会话的更新时间，即最近活跃时间
     *
     * @param id 编号
     */
    void touchSession(Long id);

    /**
     * 删除 Agent 下的全部会话与消息（Agent 删除时调用）
     *
     * @param agentIds Agent 编号集合
     */
    void deleteSessionListByAgentIds(Collection<Long> agentIds);

}
