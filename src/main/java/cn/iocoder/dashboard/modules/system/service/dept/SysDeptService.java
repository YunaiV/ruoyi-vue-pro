package cn.iocoder.dashboard.modules.system.service.dept;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;

import java.util.Collection;
import java.util.List;

/**
 * 部门 Service 接口
 *
 * @author 芋道源码
 */
public interface SysDeptService {

    /**
     * 初始化
     */
    void init();

    /**
     * 获得所有部门列表
     *
     * @return 部门列表
     */
    List<SysDeptDO> listDepts();

    /**
     * 获得指定编号的部门列表
     *
     * @param ids 部门编号数组
     * @return 部门列表
     */
    List<SysDeptDO> listDepts(Collection<Long> ids);

    /**
     * 筛选部门列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 部门列表
     */
    List<SysDeptDO> listDepts(SysDeptListReqVO reqVO);

    /**
     * 获得所有子部门，从缓存中
     *
     * @param parentId 部门编号
     * @param recursive 是否递归获取所有
     * @return 子部门列表
     */
    List<SysDeptDO> listDeptsByParentIdFromCache(Long parentId, boolean recursive);

}
