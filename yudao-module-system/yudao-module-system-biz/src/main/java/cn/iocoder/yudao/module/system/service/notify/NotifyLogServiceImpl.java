package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * <p>
 * 站内信日志 Service 实现类
 *
 * </p>
 *
 * @author LuoWenFeng
 */
@Service
@Validated
public class NotifyLogServiceImpl implements NotifyLogService {

    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @Override
    public PageResult<NotifyMessageDO> getNotifyMessageSendPage(NotifyLogPageReqVO pageReqVO) {
        return notifyMessageMapper.selectSendPage(pageReqVO, getLoginUserId());
    }


}
