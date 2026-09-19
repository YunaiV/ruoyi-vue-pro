package cn.iocoder.yudao.module.oa.service.note;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNotePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteReceiverMapper;
import cn.iocoder.yudao.module.oa.enums.note.OaNoteTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_NOT_EXISTS;

/**
 * OA 笔记 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaNoteServiceImpl implements OaNoteService {

    @Resource
    private OaNoteMapper noteMapper;
    @Resource
    private OaNoteReceiverMapper noteReceiverMapper;
    @Resource
    @Lazy
    private OaNoteCategoryService noteCategoryService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNote(OaNoteSaveReqVO createReqVO, Long userId) {
        // 1. 校验笔记目录属于当前用户
        noteCategoryService.validateNoteCategoryOwner(createReqVO.getCategoryId(), userId);

        // 2. 新增笔记
        OaNoteDO note = BeanUtils.toBean(createReqVO, OaNoteDO.class)
                .setFavorite(false);
        noteMapper.insert(note);

        // 3. 创建人也通过持有关系访问笔记
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(userId));
        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(OaNoteSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验笔记属于当前用户
        validateNoteOwner(updateReqVO.getId(), userId);
        // 1.2 校验笔记目录属于当前用户
        noteCategoryService.validateNoteCategoryOwner(updateReqVO.getCategoryId(), userId);

        // 2. 更新笔记
        noteMapper.updateForSave(BeanUtils.toBean(updateReqVO, OaNoteDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoteShare(Long id, List<Long> receiverUserIds, Long userId) {
        // 1.1 校验笔记属于当前用户
        validateNoteOwner(id, userId);
        // 1.2 校验接收人存在
        adminUserApi.validateUserList(receiverUserIds);

        // 2.1 查询原接收关系并计算差异
        List<OaNoteReceiverDO> oldReceivers = noteReceiverMapper.selectListByNoteId(id);
        Set<Long> holderUserIds = new HashSet<>(receiverUserIds);
        holderUserIds.add(userId); // 清空共享名单也保留创建人自己的持有关系
        List<OaNoteReceiverDO> newReceivers = convertList(holderUserIds,
                receiverUserId -> new OaNoteReceiverDO().setNoteId(id).setUserId(receiverUserId));
        List<List<OaNoteReceiverDO>> diffReceivers = diffList(oldReceivers, newReceivers,
                (oldReceiver, newReceiver) -> ObjectUtil.equal(oldReceiver.getUserId(), newReceiver.getUserId()));
        // 2.2 新增接收关系
        if (CollUtil.isNotEmpty(diffReceivers.get(0))) {
            noteReceiverMapper.insertBatch(diffReceivers.get(0));
        }
        // 2.3 删除取消的接收关系，创建人自身关系不参与撤销
        if (CollUtil.isNotEmpty(diffReceivers.get(2))) {
            noteReceiverMapper.deleteByNoteIdAndUserIds(id, convertList(diffReceivers.get(2), OaNoteReceiverDO::getUserId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long id, Long userId) {
        // 1. 校验笔记属于当前用户
        OaNoteDO note = validateNoteOwner(id, userId);

        // 2. 共享笔记只退出本人持有，保留正文及其他接收人的访问
        if (ObjectUtil.equal(note.getType(), OaNoteTypeEnum.SHARED.getType())
                || CollUtil.findOne(noteReceiverMapper.selectListByNoteId(id),
                        receiver -> ObjectUtil.notEqual(receiver.getUserId(), userId)) != null) {
            noteReceiverMapper.deleteByNoteIdAndUserIds(id, Collections.singletonList(userId));
            return;
        }

        // 3.1 删除笔记接收人
        noteReceiverMapper.deleteByNoteId(id);
        // 3.2 删除笔记
        noteMapper.deleteById(id);
    }

    @Override
    public void deleteReceivedNote(Long id, Long userId) {
        // 1.1 创建人通过本人笔记删除入口操作
        OaNoteDO note = validateNoteExists(id);
        if (ObjectUtil.equal(NumberUtils.parseLong(note.getCreator()), userId)) {
            throw exception(NOTE_ACCESS_DENIED);
        }
        // 1.2 校验当前用户是共享接收人
        OaNoteReceiverDO receiver = noteReceiverMapper.selectByNoteIdAndUserId(id, userId);
        if (receiver == null) {
            throw exception(NOTE_ACCESS_DENIED);
        }

        // 2. 仅移除本人的接收关系，保留正文及其他用户的访问权限
        noteReceiverMapper.deleteById(receiver.getId());
    }

    @Override
    public OaNoteDO getNote(Long id, Long userId) {
        // 1. 校验笔记存在
        OaNoteDO note = validateNoteExists(id);

        // 2. 创建人和接收人均须仍持有笔记
        if (noteReceiverMapper.selectByNoteIdAndUserId(id, userId) == null) {
            throw exception(NOTE_ACCESS_DENIED);
        }
        return note;
    }

    @Override
    public PageResult<OaNoteDO> getMyNotePage(OaNotePageReqVO pageReqVO, Long userId) {
        // 1. 查询当前用户仍持有的笔记
        List<Long> noteIds = convertList(noteReceiverMapper.selectListByUserId(userId), OaNoteReceiverDO::getNoteId);
        if (CollUtil.isEmpty(noteIds)) {
            return PageResult.empty();
        }

        // 2. 仅展示本人创建且仍持有的笔记
        return noteMapper.selectMyPage(pageReqVO, userId, noteIds);
    }

    @Override
    public PageResult<OaNoteDO> getReceivedNotePage(OaNotePageReqVO pageReqVO, Long userId) {
        // 1. 查询当前用户的接收关系
        List<OaNoteReceiverDO> receivers = noteReceiverMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(receivers)) {
            return PageResult.empty();
        }
        // 2. 查询共享给本人的笔记
        List<Long> noteIds = convertList(receivers, OaNoteReceiverDO::getNoteId);
        return noteMapper.selectReceivedPage(pageReqVO, noteIds, userId);
    }

    @Override
    public void updateNoteFavorite(Long id, Boolean favorite, Long userId) {
        // 1. 校验当前用户仍持有笔记
        getNote(id, userId);

        // 2. 持有人共用笔记的收藏状态
        noteMapper.updateById(new OaNoteDO().setId(id).setFavorite(favorite));
    }

    @Override
    public List<OaNoteReceiverDO> getNoteReceiverList(Collection<Long> noteIds) {
        if (CollUtil.isEmpty(noteIds)) {
            return Collections.emptyList();
        }
        return noteReceiverMapper.selectListByNoteIds(noteIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotesByCategoryId(Long categoryId, Long userId) {
        // 1.1 查询本人仍持有的目录笔记，已退出的正文不随目录删除
        List<Long> heldNoteIds = convertList(noteReceiverMapper.selectListByUserId(userId), OaNoteReceiverDO::getNoteId);
        if (CollUtil.isEmpty(heldNoteIds)) {
            return;
        }
        List<OaNoteDO> notes = noteMapper.selectListByCategoryIdAndIds(categoryId, heldNoteIds);
        if (CollUtil.isEmpty(notes)) {
            return;
        }
        // 1.2 校验笔记均属于当前用户
        if (CollUtil.findOne(notes, note -> ObjectUtil.notEqual(NumberUtils.parseLong(note.getCreator()), userId)) != null) {
            throw exception(NOTE_ACCESS_DENIED);
        }

        // 2.1 删除笔记接收人
        Collection<Long> noteIds = convertSet(notes, OaNoteDO::getId);
        noteReceiverMapper.deleteByNoteIds(noteIds);
        // 2.2 删除笔记
        noteMapper.deleteByIds(noteIds);
    }

    /**
     * 校验笔记属于指定用户
     *
     * @param id 笔记编号
     * @param userId 用户编号
     * @return 笔记
     */
    private OaNoteDO validateNoteOwner(Long id, Long userId) {
        OaNoteDO note = getNote(id, userId);
        if (ObjectUtil.notEqual(NumberUtils.parseLong(note.getCreator()), userId)) {
            throw exception(NOTE_ACCESS_DENIED);
        }
        return note;
    }

    /**
     * 校验笔记存在
     *
     * @param id 笔记编号
     * @return 笔记
     */
    private OaNoteDO validateNoteExists(Long id) {
        OaNoteDO note = noteMapper.selectById(id);
        if (note == null) {
            throw exception(NOTE_NOT_EXISTS);
        }
        return note;
    }

}
