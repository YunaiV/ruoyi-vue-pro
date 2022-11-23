package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;

import java.util.Collection;
import java.util.List;

/**
 * 站内信 Service 接口
 *
 * @author xrcoder
 */
public interface NotifyMessageService {

    /**
     * 获得站内信
     *
     * @param id 编号
     * @return 站内信
     */
    NotifyMessageDO getNotifyMessage(Long id);

    /**
     * 获得站内信列表
     *
     * @param ids 编号
     * @return 站内信列表
     */
    List<NotifyMessageDO> getNotifyMessageList(Collection<Long> ids);

    /**
     * 获得站内信集合
     *
     * @param pageReqVO 分页查询
     * @return 站内信分页
     */
    List<NotifyMessageDO> getNotifyMessageList(NotifyMessagePageReqVO pageReqVO, Integer size);

    /**
     * 获得站内信分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信分页
     */
    PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO);

    /**
     * 统计用户未读站内信条数
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 返回未读站内信条数
     */
    Long getUnreadNotifyMessageCount(Long userId, Integer userType);

    /**
     * 修改站内信阅读状态
     *
     * @param id 站内信编号
     * @param status 状态
     */
    void updateNotifyMessageReadStatus(Long id, Boolean status);

    /**
     * 批量修改站内信阅读状态
     *
     * @param ids 站内信编号集合
     * @param userId 用户ID
     */
    void batchUpdateNotifyMessageReadStatus(Collection<Long> ids, Long userId);

    /**
     * 批量修改用户所有未读消息标记已读
     *
     * @param userId 用户ID
     * @param userType 用户类型
     */
    void batchUpdateAllNotifyMessageReadStatus(Long userId, Integer userType);
}
