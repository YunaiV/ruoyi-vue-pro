package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyMessageConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTIFY_MESSAGE_NOT_EXISTS;

/**
 * 站内信 Service 实现类
 *
 * @author xrcoder
 */
@Service
@Validated
public class NotifyMessageServiceImpl implements NotifyMessageService {

    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @Override
    public Long createNotifyMessage(NotifyMessageCreateReqVO createReqVO) {
        // 插入
        NotifyMessageDO notifyMessage = NotifyMessageConvert.INSTANCE.convert(createReqVO);
        notifyMessageMapper.insert(notifyMessage);
        // 返回
        return notifyMessage.getId();
    }

    @Override
    public void updateNotifyMessage(NotifyMessageUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateNotifyMessageExists(updateReqVO.getId());
        // 更新
        NotifyMessageDO updateObj = NotifyMessageConvert.INSTANCE.convert(updateReqVO);
        notifyMessageMapper.updateById(updateObj);
    }

    @Override
    public void deleteNotifyMessage(Long id) {
        // 校验存在
        this.validateNotifyMessageExists(id);
        // 删除
        notifyMessageMapper.deleteById(id);
    }

    private void validateNotifyMessageExists(Long id) {
        if (notifyMessageMapper.selectById(id) == null) {
            throw exception(NOTIFY_MESSAGE_NOT_EXISTS);
        }
    }

    @Override
    public NotifyMessageDO getNotifyMessage(Long id) {
        return notifyMessageMapper.selectById(id);
    }

    @Override
    public List<NotifyMessageDO> getNotifyMessageList(Collection<Long> ids) {
        return notifyMessageMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<NotifyMessageDO> getNotifyMessagePage(NotifyMessagePageReqVO pageReqVO) {
        return notifyMessageMapper.selectPage(pageReqVO);
    }
}
