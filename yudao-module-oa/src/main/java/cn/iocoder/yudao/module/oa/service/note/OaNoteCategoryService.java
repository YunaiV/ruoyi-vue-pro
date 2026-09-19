package cn.iocoder.yudao.module.oa.service.note;

import cn.iocoder.yudao.module.oa.controller.admin.note.vo.category.OaNoteCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteCategoryDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * OA 笔记目录 Service 接口
 *
 * @author 芋道源码
 */
public interface OaNoteCategoryService {

    /**
     * 创建笔记目录
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 目录编号
     */
    Long createNoteCategory(OaNoteCategorySaveReqVO createReqVO, Long userId);

    /**
     * 更新笔记目录
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateNoteCategory(OaNoteCategorySaveReqVO updateReqVO, Long userId);

    /**
     * 删除笔记目录
     *
     * @param id 目录编号
     * @param userId 用户编号
     */
    void deleteNoteCategory(Long id, Long userId);

    /**
     * 获得用户的笔记目录列表
     *
     * @param userId 用户编号
     * @return 笔记目录列表
     */
    List<OaNoteCategoryDO> getNoteCategoryList(Long userId);

    /**
     * 获得笔记目录
     *
     * @param id 目录编号
     * @param userId 用户编号
     * @return 笔记目录
     */
    OaNoteCategoryDO getNoteCategory(Long id, Long userId);

    /**
     * 校验笔记目录归属当前用户
     *
     * @param id 目录编号
     * @param userId 用户编号
     * @return 笔记目录
     */
    OaNoteCategoryDO validateNoteCategoryOwner(Long id, Long userId);

    /**
     * 获得笔记目录列表
     *
     * @param ids 目录编号集合
     * @return 笔记目录列表
     */
    List<OaNoteCategoryDO> getNoteCategoryList(Collection<Long> ids);

    /**
     * 获得笔记目录 Map
     *
     * @param ids 目录编号集合
     * @return 笔记目录 Map
     */
    default Map<Long, OaNoteCategoryDO> getNoteCategoryMap(Collection<Long> ids) {
        List<OaNoteCategoryDO> categories = getNoteCategoryList(ids);
        return convertMap(categories, OaNoteCategoryDO::getId);
    }

}
