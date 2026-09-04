package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member.PmsKnowledgeLibraryUpdateMemberListReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PMS 知识库成员 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeLibraryMemberService {

    /**
     * 校验知识库可读
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 知识库
     */
    PmsKnowledgeLibraryDO validateLibraryReadable(Long libraryId, Long userId);

    /**
     * 校验知识库可写
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 知识库
     */
    PmsKnowledgeLibraryDO validateLibraryWritable(Long libraryId, Long userId);

    /**
     * 校验知识库管理员权限
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 知识库
     */
    PmsKnowledgeLibraryDO validateLibraryAdmin(Long libraryId, Long userId);

    /**
     * 校验知识库创建人权限
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 知识库
     */
    PmsKnowledgeLibraryDO validateLibraryCreator(Long libraryId, Long userId);

    /**
     * 判断知识库是否可写
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 是否可写
     */
    boolean isLibraryWritable(Long libraryId, Long userId);

    /**
     * 判断是否为知识库管理员
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 是否为管理员
     */
    boolean isLibraryAdmin(Long libraryId, Long userId);

    /**
     * 获得已加入的有效知识库编号列表
     *
     * @param userId 用户编号
     * @return 知识库编号列表
     */
    List<Long> getJoinedLibraryIdList(Long userId);

    /**
     * 获得可读知识库编号列表
     *
     * @param userId 用户编号
     * @return 知识库编号列表
     */
    List<Long> getReadableLibraryIdList(Long userId);

    /**
     * 创建知识库的创建人和初始成员
     *
     * @param libraryId 知识库编号
     * @param creatorUserId 创建人用户编号
     * @param adminUserIds 初始管理员用户编号集合
     * @param memberUserIds 初始普通成员用户编号集合
     */
    void createLibraryMemberList(Long libraryId, Long creatorUserId,
                                 Collection<Long> adminUserIds, Collection<Long> memberUserIds);

    /**
     * 获得知识库成员列表
     *
     * @param libraryId 知识库编号
     * @param userId 当前用户编号
     * @return 成员列表
     */
    List<PmsKnowledgeLibraryMemberDO> getLibraryMemberList(Long libraryId, Long userId);

    /**
     * 更新知识库成员，保留创建人并清理已移除成员的内容级协作权限
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateLibraryMemberList(PmsKnowledgeLibraryUpdateMemberListReqVO updateReqVO, Long userId);

    /**
     * 退出知识库，创建人不允许退出
     *
     * @param libraryId 知识库编号
     * @param userId 当前用户编号
     */
    void exitLibrary(Long libraryId, Long userId);

    /**
     * 删除知识库的全部成员关系
     *
     * @param libraryId 知识库编号
     */
    void deleteLibraryMembersByLibraryId(Long libraryId);

    /**
     * 获得知识库成员列表 Map
     *
     * @param libraryIds 知识库编号集合
     * @return 知识库编号到成员列表的映射
     */
    Map<Long, List<PmsKnowledgeLibraryMemberDO>> getLibraryMemberListMap(Collection<Long> libraryIds);

    /**
     * 获得用户在知识库中的直接成员关系
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 成员关系
     */
    PmsKnowledgeLibraryMemberDO getMemberByLibraryIdAndUserId(Long libraryId, Long userId);

    /**
     * 获得部门在知识库中的成员关系
     *
     * @param libraryId 知识库编号
     * @param deptId 部门编号
     * @return 成员关系
     */
    PmsKnowledgeLibraryMemberDO getMemberByLibraryIdAndDeptId(Long libraryId, Long deptId);

    /**
     * 获得用户或部门加入的知识库编号列表
     *
     * @param userId 用户编号
     * @param deptId 部门编号
     * @return 知识库编号列表
     */
    List<Long> getLibraryIdListByUserIdOrDeptId(Long userId, Long deptId);

}
