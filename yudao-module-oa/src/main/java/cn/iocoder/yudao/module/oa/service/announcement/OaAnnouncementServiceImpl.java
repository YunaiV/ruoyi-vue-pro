package cn.iocoder.yudao.module.oa.service.announcement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.announcement.OaAnnouncementMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.announcement.OaAnnouncementReceiverMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_ALREADY_FORWARDED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_RECEIVER_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_UNREAD_DELETE_DENIED;

/**
 * OA 公告 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaAnnouncementServiceImpl implements OaAnnouncementService {

    @Resource
    private OaAnnouncementMapper announcementMapper;
    @Resource
    private OaAnnouncementReceiverMapper announcementReceiverMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(OaAnnouncementSaveReqVO createReqVO, Long userId) {
        // 1. 查询发布人负责部门及其子部门下的用户，作为公告接收人
        List<Long> receiverUserIds = convertList(adminUserApi.getUserListBySubordinate(userId), AdminUserRespDTO::getId);

        // 2. 新增公告
        OaAnnouncementDO announcement = BeanUtils.toBean(createReqVO, OaAnnouncementDO.class);
        announcementMapper.insert(announcement);

        // 3. 新增公告接收关系，接收人在公告列表查看，不额外发送站内信
        createAnnouncementReceiverList(announcement.getId(), receiverUserIds);
        return announcement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(OaAnnouncementSaveReqVO updateReqVO, Long userId) {
        // 1. 校验公告属于当前发布人
        validateAnnouncementPublisher(updateReqVO.getId(), userId);

        // 2. 更新公告
        announcementMapper.updateById(BeanUtils.toBean(updateReqVO, OaAnnouncementDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long id, Long userId) {
        // 1. 校验公告属于当前发布人
        validateAnnouncementPublisher(id, userId);

        // 2. 删除公告接收关系和公告
        announcementReceiverMapper.deleteByAnnouncementId(id);
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReceivedAnnouncement(Long id, Long userId) {
        // 1.1 校验公告存在
        validateAnnouncementExists(id);
        // 1.2 校验当前用户是公告接收人
        OaAnnouncementReceiverDO receiver = validateAnnouncementReceiver(id, userId);
        // 1.3 阅读后才允许移除
        if (Boolean.FALSE.equals(receiver.getReadStatus())) {
            throw exception(ANNOUNCEMENT_UNREAD_DELETE_DENIED);
        }

        // 2. 仅删除当前用户的接收关系，不影响公告及其他接收人
        announcementReceiverMapper.deleteByIdPhysical(receiver.getId());
    }

    @Override
    public OaAnnouncementDO getAnnouncement(Long id, Long userId) {
        OaAnnouncementDO announcement = validateAnnouncementExists(id);
        if (ObjUtil.notEqual(announcement.getCreator(), userId.toString())
                && announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(id, userId) == null) {
            throw exception(ANNOUNCEMENT_ACCESS_DENIED);
        }
        return announcement;
    }

    @Override
    public PageResult<OaAnnouncementDO> getPublishedAnnouncementPage(OaAnnouncementPageReqVO pageReqVO, Long userId) {
        return announcementMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public PageResult<OaAnnouncementDO> getReceivedAnnouncementPage(OaAnnouncementPageReqVO pageReqVO, Long userId) {
        return announcementMapper.selectReceivedPage(pageReqVO, userId);
    }

    @Override
    public void updateAnnouncementReadStatus(Long id, Long userId) {
        // 1.1 校验公告存在
        validateAnnouncementExists(id);
        // 1.2 校验当前用户是公告接收人
        OaAnnouncementReceiverDO receiver = validateAnnouncementReceiver(id, userId);
        // 1.3 已读公告无需重复更新
        if (Boolean.TRUE.equals(receiver.getReadStatus())) {
            return;
        }

        // 2. 标记公告为已读
        announcementReceiverMapper.updateById(new OaAnnouncementReceiverDO()
                .setId(receiver.getId()).setReadStatus(true));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int forwardAnnouncement(Long id, Long userId) {
        // 1.1 锁定公告，串行处理同一公告的转发，避免并发请求同时通过未转发校验
        if (announcementMapper.selectByIdForUpdate(id) == null) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        // 1.2 校验当前用户的接收关系
        validateAnnouncementReceiver(id, userId);
        // 1.3 查询当前用户的下属
        List<Long> subordinateUserIds = convertList(adminUserApi.getUserListBySubordinate(userId), AdminUserRespDTO::getId);
        if (CollUtil.isEmpty(subordinateUserIds)) {
            return 0;
        }
        // 1.4 与列表已转发状态一致：任一下属已经接收，就不允许再次转发
        List<OaAnnouncementReceiverDO> receivers = announcementReceiverMapper.selectListByAnnouncementId(id);
        Set<Long> existingReceiverUserIds = convertSet(receivers,
                OaAnnouncementReceiverDO::getReceiverUserId);
        if (CollUtil.containsAny(subordinateUserIds, existingReceiverUserIds)) {
            throw exception(ANNOUNCEMENT_ALREADY_FORWARDED);
        }

        // 2. 新增下属的公告接收关系
        createAnnouncementReceiverList(id, subordinateUserIds);
        return subordinateUserIds.size();
    }

    @Override
    public List<OaAnnouncementReceiverDO> getAnnouncementReceiverList(Collection<Long> announcementIds) {
        if (CollUtil.isEmpty(announcementIds)) {
            return Collections.emptyList();
        }
        return announcementReceiverMapper.selectListByAnnouncementIds(announcementIds);
    }

    /**
     * 新增公告接收关系
     *
     * @param announcementId 公告编号
     * @param receiverUserIds 接收人用户编号列表
     */
    private void createAnnouncementReceiverList(Long announcementId, Collection<Long> receiverUserIds) {
        if (CollUtil.isEmpty(receiverUserIds)) {
            return;
        }
        List<OaAnnouncementReceiverDO> receivers = convertList(receiverUserIds,
                receiverUserId -> new OaAnnouncementReceiverDO().setAnnouncementId(announcementId)
                        .setReceiverUserId(receiverUserId).setReadStatus(false));
        announcementReceiverMapper.insertBatch(receivers);
    }

    /**
     * 校验公告属于指定发布人
     *
     * @param id 公告编号
     * @param userId 发布人用户编号
     * @return 公告
     */
    @SuppressWarnings("UnusedReturnValue")
    private OaAnnouncementDO validateAnnouncementPublisher(Long id, Long userId) {
        OaAnnouncementDO announcement = validateAnnouncementExists(id);
        if (ObjUtil.notEqual(announcement.getCreator(), userId.toString())) {
            throw exception(ANNOUNCEMENT_ACCESS_DENIED);
        }
        return announcement;
    }

    /**
     * 校验当前用户是公告接收人
     *
     * @param announcementId 公告编号
     * @param userId 用户编号
     * @return 公告接收关系
     */
    private OaAnnouncementReceiverDO validateAnnouncementReceiver(Long announcementId, Long userId) {
        OaAnnouncementReceiverDO receiver = announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(announcementId, userId);
        if (receiver == null) {
            throw exception(ANNOUNCEMENT_RECEIVER_NOT_EXISTS);
        }
        return receiver;
    }

    /**
     * 校验公告是否存在
     *
     * @param id 公告编号
     * @return 公告
     */
    private OaAnnouncementDO validateAnnouncementExists(Long id) {
        OaAnnouncementDO announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        return announcement;
    }

}
