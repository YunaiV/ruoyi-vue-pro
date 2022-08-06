package cn.iocoder.yudao.module.system.service.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyMessageConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import cn.iocoder.yudao.module.system.enums.notify.NotifyReadStatusEnum;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

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

    @Resource
    private NotifyTemplateService notifyTemplateService;


    @VisibleForTesting
    public NotifyTemplateDO checkNotifyTemplateValid(String templateCode) {
        // 获得站内信模板。考虑到效率，从缓存中获取
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplateByCodeFromCache(templateCode);
        // 站内信模板不存在
        if (template == null) {
            throw exception(NOTIFY_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 将参数模板，处理成有序的 KeyValue 数组
     *
     * @param template       站内信模板
     * @param templateParams 原始参数
     * @return 处理后的参数
     */
    @VisibleForTesting
    public List<KeyValue<String, Object>> buildTemplateParams(NotifyTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(NOTIFY_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }

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

    /**
     * 统计用户未读站内信条数
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 返回未读站内信条数
     */
    @Override
    public Long getUnreadNotifyMessageCount(Long userId, Integer userType) {
        return notifyMessageMapper.selectUnreadCountByUserIdAndUserType(userId, userType);
    }

    /**
     * 修改站内信阅读状态
     *
     * @param id     站内信编号
     * @param status 状态
     */
    @Override
    public void updateNotifyMessageReadStatus(Long id, Integer status) {
        // 校验消息是否存在
        this.validateNotifyMessageExists(id);
        // 更新状态
        batchUpdateReadStatus(CollectionUtils.singleton(id));
    }

    /**
     * 批量修改站内信阅读状态
     *
     * @param ids    站内信编号集合
     * @param userId 用户ID
     */
    @Override
    public void batchUpdateNotifyMessageReadStatus(Collection<Long> ids, Long userId) {
        List<NotifyMessageDO> list = getNotifyMessageList(ids);
        if (CollUtil.isEmpty(list)) {
            throw exception(NOTIFY_MESSAGE_NOT_EXISTS);
        }
        // 验证站内信是否是属于用户
        for (NotifyMessageDO messageDO : list) {
            checkNotifyMessageIdValid(messageDO, userId);
        }
        batchUpdateReadStatus(ids);
    }

    @VisibleForTesting
    public void checkNotifyMessageIdValid(NotifyMessageDO notifyMessageDO, Long userId) {
        if (!NumberUtil.equals(notifyMessageDO.getUserId(), userId)) {
            throw exception(NOTIFY_MESSAGE_ID_PARAM_ERROR);
        }
    }

    /**
     * 批量修改用户所有未读消息标记已读
     *
     * @param userId   用户ID
     * @param userType 用户类型
     */
    @Override
    public void batchUpdateAllNotifyMessageReadStatus(Long userId, Integer userType) {
        List<NotifyMessageDO> list = notifyMessageMapper.selectUnreadListByUserIdAndUserType(userId, userType);
        if (CollUtil.isNotEmpty(list)) {
            batchUpdateReadStatus(CollectionUtils.convertList(list, NotifyMessageDO::getId));

        }
    }

    private void batchUpdateReadStatus(Collection<Long> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                NotifyMessageDO updateObj = new NotifyMessageDO();
                updateObj.setId(id);
                updateObj.setReadStatus(NotifyReadStatusEnum.READ.getStatus());
                notifyMessageMapper.updateById(updateObj);
            }
        }

    }
}
