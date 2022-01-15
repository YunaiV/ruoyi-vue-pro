package cn.iocoder.yudao.adminserver.modules.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 部门 Service 接口
 *
 * @author 芋道源码
 */
public interface SysDeptService {

    /**
     * 初始化部门的本地缓存
     */
    void initLocalCache();

    /**
     * 创建部门
     *
     * @param reqVO 部门信息
     * @return 部门编号
     */
    Long createDept(SysDeptCreateReqVO reqVO);

    /**
     * 更新部门
     *
     * @param reqVO 部门信息
     */
    void updateDept(SysDeptUpdateReqVO reqVO);

    /**
     * 删除部门
     *
     * @param id 部门编号
     */
    void deleteDept(Long id);

    /**
     * 获得指定编号的部门列表
     *
     * @param ids 部门编号数组
     * @return 部门列表
     */
    List<SysDeptDO> getSimpleDepts(Collection<Long> ids);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, SysDeptDO> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SysDeptDO> list = getSimpleDepts(ids);
        return CollectionUtils.convertMap(list, SysDeptDO::getId);
    }

    /**
     * 筛选部门列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 部门列表
     */
    List<SysDeptDO> getSimpleDepts(SysDeptListReqVO reqVO);

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    SysDeptDO getDept(Long id);

    /**
     * 获得部门信息数组
     *
     * @param ids 部门编号数组
     * @return 部门信息数组
     */
    List<SysDeptDO> getDepts(Collection<Long> ids);

    /**
     * 获得所有子部门，从缓存中
     *
     * @param parentId 部门编号
     * @param recursive 是否递归获取所有
     * @return 子部门列表
     */
    List<SysDeptDO> getDeptsByParentIdFromCache(Long parentId, boolean recursive);

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validDepts(Collection<Long> ids);

}
