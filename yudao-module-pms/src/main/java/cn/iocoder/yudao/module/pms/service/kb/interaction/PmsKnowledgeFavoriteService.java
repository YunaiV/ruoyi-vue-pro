package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoritePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoriteSaveReqVO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * PMS 知识收藏（关注）Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeFavoriteService {

    /**
     * 关注知识对象
     *
     * @param saveReqVO 关注信息
     * @param userId 用户编号
     */
    void createFavorite(PmsKnowledgeFavoriteSaveReqVO saveReqVO, Long userId);

    /**
     * 取消关注，未关注时不做任何处理
     *
     * @param type 对象类型
     * @param entityId 对象编号
     * @param userId 用户编号
     */
    void deleteFavorite(Integer type, Long entityId, Long userId);

    /**
     * 获得当前用户的关注分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 关注分页结果
     */
    PageResult<PmsKnowledgeInteractionItemRespVO> getFavoritePage(PmsKnowledgeFavoritePageReqVO pageReqVO,
                                                                 Long userId);

    /**
     * 获得指定知识库内当前用户的关注列表
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 关注列表
     */
    List<PmsKnowledgeInteractionItemRespVO> getFavoriteListByLibraryId(Long libraryId, Long userId);

    /**
     * 获得当前用户已关注的对象编号集合
     *
     * @param type 对象类型
     * @param entityIds 对象编号集合
     * @param userId 用户编号
     * @return 已关注的对象编号集合
     */
    Set<Long> getFavoriteEntityIdSet(Integer type, Collection<Long> entityIds, Long userId);

    /**
     * 判断当前用户是否已关注对象
     *
     * @param type 对象类型
     * @param entityId 对象编号
     * @param userId 用户编号
     * @return 是否已关注
     */
    boolean isFavorite(Integer type, Long entityId, Long userId);

    /**
     * 删除知识库的全部关注关系
     *
     * @param libraryId 知识库编号
     */
    void deleteFavoritesByLibraryId(Long libraryId);

    /**
     * 删除文件夹和文档的关注关系
     *
     * @param folderIds 文件夹编号集合
     * @param documentIds 文档编号集合
     */
    void deleteFavoritesByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds);

    /**
     * 更新文件夹和文档关注关系的所属知识库
     *
     * @param folderIds 文件夹编号集合
     * @param documentIds 文档编号集合
     * @param libraryId 目标知识库编号
     */
    void updateFavoriteLibraryIdByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds,
                                            Long libraryId);

}
