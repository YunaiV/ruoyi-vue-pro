package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibrarySaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 知识库 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeLibraryService {

    /**
     * 创建知识库，并将创建人和初始成员加入知识库
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 知识库编号
     */
    Long createLibrary(PmsKnowledgeLibrarySaveReqVO saveReqVO, Long userId);

    /**
     * 更新知识库基本信息
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateLibrary(PmsKnowledgeLibrarySaveReqVO saveReqVO, Long userId);

    /**
     * 删除知识库，并将知识库及其内容移入回收站
     *
     * @param id 知识库编号
     * @param userId 用户编号
     */
    void deleteLibrary(Long id, Long userId);

    /**
     * 将知识库更新为已回收状态
     *
     * @param id 知识库编号
     * @param userId 删除用户编号
     * @param deleteTime 删除时间
     */
    void updateLibraryToRecycled(Long id, Long userId, LocalDateTime deleteTime);

    /**
     * 恢复知识库
     *
     * @param id 知识库编号
     */
    void restoreLibrary(Long id);

    /**
     * 彻底删除知识库
     *
     * @param id 知识库编号
     */
    void deleteLibraryPermanently(Long id);

    /**
     * 获得知识库
     *
     * @param id 知识库编号
     * @return 知识库
     */
    PmsKnowledgeLibraryDO getLibrary(Long id);

    /**
     * 获得当前用户可见的知识库分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 知识库分页
     */
    PageResult<PmsKnowledgeLibraryDO> getLibraryPage(PmsKnowledgeLibraryPageReqVO pageReqVO, Long userId);

    /**
     * 获得知识库列表
     *
     * @param ids 知识库编号集合
     * @return 知识库列表
     */
    List<PmsKnowledgeLibraryDO> getLibraryList(Collection<Long> ids);

    /**
     * 获得知识库 Map
     *
     * @param ids 知识库编号集合
     * @return 知识库编号到知识库的映射
     */
    default Map<Long, PmsKnowledgeLibraryDO> getLibraryMap(Collection<Long> ids) {
        return convertMap(getLibraryList(ids), PmsKnowledgeLibraryDO::getId);
    }

    /**
     * 获得符合公开状态的正常知识库编号列表
     *
     * @param openStatus 公开状态，为空时查询全部
     * @return 知识库编号列表
     */
    List<Long> getLibraryIdList(Boolean openStatus);

}
