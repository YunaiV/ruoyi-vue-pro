package cn.iocoder.yudao.module.system.service.dept;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO;
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
    List<DeptDO> getChildDeptList(Long id);

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

    /**
     * 获得指定部门的级别
     *
     * @param id,parentId 部门编号
     * @return 级别
     */
    Integer getDeptLevel(Long id);

    /**
     * 获得指定部门
     *
     * @param id 部门编号
     * @return 父级名称
     */
    String getParentNameById(Long id);

    /**
    * @Author Wqh
    * @Description 根据部门id溯源部门名称
    * @Date 14:25 2024/11/4
    * @Param [id]
    * @return java.util.TreeSet<cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelDTO>
    **/
    TreeSet<DeptLevelRespDTO> getDeptTreeLevel(Long id);

    /**
     * @Author Wqh
     * @Description 获得二级部门和一级部门（构建树结构）
     * @Date 15:26 2024/10/30
     * @Param []
     * @return java.util.List<cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO>
     **/
    List<DeptTreeRespVO> getTreeDeptList();
}
