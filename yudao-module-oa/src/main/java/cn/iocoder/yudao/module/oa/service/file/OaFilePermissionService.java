package cn.iocoder.yudao.module.oa.service.file;

import cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission.OaFilePermissionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;

import java.util.Collection;
import java.util.List;

/**
 * 云盘共享权限 Service 接口
 *
 * @author 芋道源码
 */
public interface OaFilePermissionService {

    /**
     * 获得指定节点的共享权限，供节点列表批量计算权限
     *
     * @param nodeIds 节点编号集合
     * @return 共享权限列表
     */
    List<OaFilePermissionDO> getFilePermissionListByNodeIds(Collection<Long> nodeIds);

    /**
     * 获得当前用户及所属部门尚未到期的共享权限
     *
     * @param userId 用户编号
     * @param deptId 部门编号，允许为空
     * @return 有效共享权限列表
     */
    List<OaFilePermissionDO> getValidFilePermissionList(Long userId, Long deptId);

    /**
     * 获得节点共享权限
     *
     * @param nodeId 节点编号
     * @param userId 当前用户
     * @return 共享权限列表
     */
    List<OaFilePermissionDO> getFilePermissionList(Long nodeId, Long userId);

    /**
     * 保存共享权限，同一主体覆盖更新
     *
     * @param reqVO 共享信息
     * @param userId 当前用户
     * @return 权限编号
     */
    Long saveFilePermission(OaFilePermissionSaveReqVO reqVO, Long userId);

    /**
     * 取消共享
     *
     * @param id 权限编号
     * @param userId 当前用户
     */
    void deleteFilePermission(Long id, Long userId);

    /**
     * 清理节点的全部共享权限
     *
     * @param nodeIds 节点编号集合
     */
    void deleteFilePermissionsByNodeIds(Collection<Long> nodeIds);

}
