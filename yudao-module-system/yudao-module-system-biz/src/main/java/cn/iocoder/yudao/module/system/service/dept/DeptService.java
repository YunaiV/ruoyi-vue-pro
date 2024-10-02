package cn.iocoder.yudao.module.system.service.dept;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;

import java.util.*;

/**
 * 部门 Service 接口
 *
 * @author 芋道源码
 */
public interface DeptService {

    /**
     * 创建部门
     *
     * @param createReqVO 部门信息
     * @return 部门编号
     */
    Long createDept(DeptSaveReqVO createReqVO);

    /**
     * 更新部门
     *
     * @param updateReqVO 部门信息
     */
    void updateDept(DeptSaveReqVO updateReqVO);

    /**
     * 删除部门
     *
     * @param id 部门编号
     */
    void deleteDept(Long id);

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    DeptDO getDept(Long id);

    /**
     * 获得部门信息数组
     *
     * @param ids 部门编号数组
     * @return 部门信息数组
     */
    List<DeptDO> getDeptList(Collection<Long> ids);

    /**
     * 筛选部门列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 部门列表
     */
    List<DeptDO> getDeptList(DeptListReqVO reqVO);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, DeptDO> getDeptMap(Collection<Long> ids) {
        List<DeptDO> list = getDeptList(ids);
        return CollectionUtils.convertMap(list, DeptDO::getId);
    }

    /**
     * 获得指定部门的所有子部门
     *
     * @param id 部门编号
     * @return 子部门列表
     */
    default List<DeptDO> getChildDeptList(Long id) {
        return getChildDeptList(Collections.singleton(id));
    }

    /**
     * 获得指定部门的所有子部门
     *
     * @param ids 部门编号数组
     * @return 子部门列表
     */
    List<DeptDO> getChildDeptList(Collection<Long> ids);

    /**
     * 获得指定领导者的部门列表
     *
     * @param id 领导者编号
     * @return 部门列表
     */
    List<DeptDO> getDeptListByLeaderUserId(Long id);

    /**
     * 获得所有子部门，从缓存中
     *
     * @param id 父部门编号
     * @return 子部门列表
     */
    Set<Long> getChildDeptIdListFromCache(Long id);

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validateDeptList(Collection<Long> ids);

}
