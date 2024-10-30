package cn.iocoder.yudao.module.system.api.dept;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门 API 接口
 *
 * @author 芋道源码
 */
public interface DeptApi {

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    DeptRespDTO getDept(Long id);

    /**
     * 获得部门信息数组
     *
     * @param ids 部门编号数组
     * @return 部门信息数组
     */
    List<DeptRespDTO> getDeptList(Collection<Long> ids);

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validateDeptList(Collection<Long> ids);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, DeptRespDTO> getDeptMap(Collection<Long> ids) {
        List<DeptRespDTO> list = getDeptList(ids);
        return CollectionUtils.convertMap(list, DeptRespDTO::getId);
    }

    /**
     * 获得指定部门的所有子部门
     *
     * @param id 部门编号
     * @return 子部门列表
     */
    List<DeptRespDTO> getChildDeptList(Long id);

    /**
    * 根据部门id来获取该部门属于第几层级
     *
    * @Param 部门id
    * @return 层级
    **/
    Integer getDeptLevel(Long id);


    /**
     * 根据部门id来获取该部门父亲部门的名称
     *
     * @Param 部门id
     * @return 服部门名称
     **/
    String getParentNameById(Long id);


}
