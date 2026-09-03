package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeLibraryMoveGroupReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PMS 知识库分组 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeGroupService {

    /**
     * 创建知识库分组，初始化默认分组并追加到现有分组末尾
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 分组编号
     */
    Long createGroup(PmsKnowledgeGroupSaveReqVO saveReqVO, Long userId);

    /**
     * 更新知识库分组名称，默认分组不允许修改
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateGroup(PmsKnowledgeGroupSaveReqVO saveReqVO, Long userId);

    /**
     * 更新知识库分组显示顺序
     *
     * @param sortReqVO 排序信息
     * @param userId 用户编号
     */
    void updateGroupSort(PmsKnowledgeGroupSortReqVO sortReqVO, Long userId);

    /**
     * 删除知识库分组及其知识库关系，默认分组不允许删除
     *
     * @param id 分组编号
     * @param userId 用户编号
     */
    void deleteGroup(Long id, Long userId);

    /**
     * 获得当前用户的知识库分组列表，并统计各分组的知识库数量
     *
     * @param userId 用户编号
     * @return 分组列表
     */
    List<PmsKnowledgeGroupDO> getGroupList(Long userId);

    /**
     * 统计当前用户各分组中的可读知识库数量
     *
     * @param userId 用户编号
     * @param groupIds 用户分组编号集合
     * @return 分组编号到知识库数量
     */
    Map<Long, Integer> getGroupLibraryCountMap(Long userId, Collection<Long> groupIds);

    /**
     * 获得当前用户的知识库分组
     *
     * @param id 分组编号
     * @param userId 用户编号
     * @return 知识库分组
     */
    PmsKnowledgeGroupDO getGroup(Long id, Long userId);

    /**
     * 移动知识库到个人分组，目标分组为空或“未分组”时移出分组
     *
     * @param moveReqVO 移动信息
     * @param userId 用户编号
     */
    void moveLibraryToGroup(PmsKnowledgeLibraryMoveGroupReqVO moveReqVO, Long userId);

    /**
     * 按分组过滤知识库编号列表，“全部知识库”不额外过滤，“未分组”排除已分组知识库
     *
     * @param groupId 分组编号
     * @param userId 用户编号
     * @param libraryIds 待过滤的知识库编号集合
     * @return 过滤后的知识库编号列表
     */
    List<Long> filterLibraryIdListByGroup(Long groupId, Long userId, Collection<Long> libraryIds);

    /**
     * 删除知识库的全部个人分组关系
     *
     * @param libraryId 知识库编号
     */
    void deleteKnowledgeGroupRelationsByLibraryId(Long libraryId);

}
