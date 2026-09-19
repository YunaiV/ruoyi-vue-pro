package cn.iocoder.yudao.module.oa.service.note;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNotePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteReceiverDO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OA 笔记 Service 接口
 *
 * @author 芋道源码
 */
public interface OaNoteService {

    /**
     * 创建笔记
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 笔记编号
     */
    Long createNote(OaNoteSaveReqVO createReqVO, Long userId);

    /**
     * 更新笔记
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateNote(OaNoteSaveReqVO updateReqVO, Long userId);

    /**
     * 删除本人笔记，共享笔记仅移除本人持有关系
     *
     * @param id 笔记编号
     * @param userId 用户编号
     */
    void deleteNote(Long id, Long userId);

    /**
     * 移除本人收到的共享笔记，不删除正文
     *
     * @param id 笔记编号
     * @param userId 接收人用户编号
     */
    void deleteReceivedNote(Long id, Long userId);

    /**
     * 获得笔记
     *
     * @param id 笔记编号
     * @param userId 用户编号
     * @return 笔记
     */
    OaNoteDO getNote(Long id, Long userId);

    /**
     * 获得我的笔记分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 笔记分页
     */
    PageResult<OaNoteDO> getMyNotePage(OaNotePageReqVO pageReqVO, Long userId);

    /**
     * 获得共享给我的笔记分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 笔记分页
     */
    PageResult<OaNoteDO> getReceivedNotePage(OaNotePageReqVO pageReqVO, Long userId);

    /**
     * 更新笔记收藏状态
     *
     * @param id 笔记编号
     * @param favorite 是否收藏
     * @param userId 用户编号
     */
    void updateNoteFavorite(Long id, Boolean favorite, Long userId);

    /**
     * 更新笔记共享接收人，不修改笔记内容
     *
     * @param id 笔记编号
     * @param receiverUserIds 接收人编号，空集合取消对其他人的共享并保留本人持有关系
     * @param userId 操作用户编号
     */
    void updateNoteShare(Long id, List<Long> receiverUserIds, Long userId);

    /**
     * 获得笔记持有人用户编号 Map，包含创建人
     *
     * @param noteIds 笔记编号集合
     * @return 笔记编号与持有人用户编号列表的 Map
     */
    default Map<Long, List<Long>> getNoteReceiverUserIdListMap(Collection<Long> noteIds) {
        List<OaNoteReceiverDO> receivers = getNoteReceiverList(noteIds);
        return CollectionUtils.convertMultiMap(receivers, OaNoteReceiverDO::getNoteId, OaNoteReceiverDO::getUserId);
    }

    /**
     * 获得笔记持有关系列表，包含创建人
     *
     * @param noteIds 笔记编号集合
     * @return 持有关系列表
     */
    List<OaNoteReceiverDO> getNoteReceiverList(Collection<Long> noteIds);

    /**
     * 删除指定目录下的笔记
     *
     * @param categoryId 目录编号
     * @param userId 用户编号
     */
    void deleteNotesByCategoryId(Long categoryId, Long userId);

}
