package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;

/**
 * 站内信日志 Service 接口
 *
 * @author LuoWenFeng
 */
public interface NotifyLogService {

    // TODO @LuoWenFeng：NotifyLogService=》NotifyMessageService

    /**
     * 获得站内信发送分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信分页
     */
    PageResult<NotifyMessageDO> getNotifyMessageSendPage(NotifyLogPageReqVO pageReqVO);

}
