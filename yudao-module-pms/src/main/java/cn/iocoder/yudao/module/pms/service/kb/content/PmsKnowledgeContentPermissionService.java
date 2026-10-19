package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.permission.PmsKnowledgeContentPermissionUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionMemberDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PMS 知识内容协作权限 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeContentPermissionService {

    /**
     * 创建默认内容权限，公开仅可预览，创建人固定拥有管理权限
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容权限编号
     */
    Long createDefaultContentPermission(Long libraryId, Long userId);

    /**
     * 复制内容权限及其协作者到目标知识库，同一原权限对应同一新权限
     *
     * @param permissionIds 内容权限编号集合
     * @param targetLibraryId 目标知识库编号
     * @return 原权限编号到新权限编号的映射
     */
    Map<Long, Long> cloneContentPermissions(Collection<Long> permissionIds, Long targetLibraryId);

    /**
     * 校验当前用户对内容有读取权限
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容等级
     */
    Integer validateContentPermissionReadable(Long permissionId, Long libraryId, Long userId);

    /**
     * 校验当前用户对内容有编辑权限
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容等级
     */
    Integer validateContentPermissionWritable(Long permissionId, Long libraryId, Long userId);

    /**
     * 校验当前用户对内容有删除权限
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容等级
     */
    Integer validateContentPermissionDeletable(Long permissionId, Long libraryId, Long userId);

    /**
     * 校验当前用户对内容有管理权限
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容等级
     */
    Integer validateContentPermissionManageable(Long permissionId, Long libraryId, Long userId);

    /**
     * 判断当前用户对内容是否有读取权限
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 是否可读
     */
    boolean isContentReadable(Long permissionId, Long libraryId, Long userId);

    /**
     * 获得内容权限，校验当前用户有读取权限
     *
     * @param permissionId 内容权限编号
     * @param userId 用户编号
     * @return 内容权限
     */
    PmsKnowledgeContentPermissionDO getContentPermission(Long permissionId, Long userId);

    /**
     * 获得内容权限的协作者列表，校验当前用户有读取权限
     *
     * @param permissionId 内容权限编号
     * @param userId 用户编号
     * @return 协作者列表
     */
    List<PmsKnowledgeContentPermissionMemberDO> getContentPermissionMemberList(Long permissionId, Long userId);

    /**
     * 获得当前用户对内容的协作等级，无权限时返回 {@code null}
     *
     * @param permissionId 内容权限编号
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容等级
     */
    Integer getCurrentUserContentPermissionLevel(Long permissionId, Long libraryId, Long userId);

    /**
     * 获得当前用户对一批内容的协作等级 Map，无权限的权限编号对应 {@code null}
     *
     * @param permissionIds 内容权限编号集合
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 内容权限编号到等级的映射
     */
    Map<Long, Integer> getCurrentUserContentPermissionLevelMap(Collection<Long> permissionIds, Long libraryId, Long userId);

    /**
     * 获得当前用户在知识库中可读的内容权限编号集合
     *
     * @param libraryIds 知识库编号集合
     * @param userId 用户编号
     * @return 可读内容权限编号集合
     */
    Set<Long> getReadableContentPermissionIdSet(Collection<Long> libraryIds, Long userId);

    /**
     * 更新内容权限的公开设置和协作者，保留创建人的管理权限
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateContentPermission(PmsKnowledgeContentPermissionUpdateReqVO updateReqVO, Long userId);

    /**
     * 删除不再被任何文件夹或文档引用的内容权限
     *
     * @param permissionIds 内容权限编号集合
     */
    void deleteUnusedContentPermissions(Set<Long> permissionIds);

    /**
     * 删除知识库的全部内容权限
     *
     * @param libraryId 知识库编号
     */
    void deleteContentPermissionsByLibraryId(Long libraryId);

    /**
     * 删除知识库内容权限中指定用户和部门的协作者
     *
     * @param libraryId 知识库编号
     * @param userIds 用户编号集合
     * @param deptIds 部门编号集合
     */
    void deleteContentPermissionMembersByLibraryId(Long libraryId, Collection<Long> userIds, Collection<Long> deptIds);

}
