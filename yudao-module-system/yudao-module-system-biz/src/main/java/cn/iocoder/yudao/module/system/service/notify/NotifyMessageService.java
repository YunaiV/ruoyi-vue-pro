package cn.iocoder.yudao.module.system.service.notify;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 站内信 Service 接口
 *
 * @author xrcoder
 */
public interface NotifyMessageService {

    /**
     * 创建站内信
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createNotifyMessage(@Valid NotifyMessageCreateReqVO createReqVO);

    /**
     * 更新站内信
     *
     * @param updateReqVO 更新信息
     */
    void updateNotifyMessage(@Valid NotifyMessageUpdateReqVO updateReqVO);

    /**
     * 删除站内信
     *
     * @param id 编号
     */
    void deleteNotifyMessage(Long id);

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
     * 获得站内信分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信分页
     */
    PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO);

}
