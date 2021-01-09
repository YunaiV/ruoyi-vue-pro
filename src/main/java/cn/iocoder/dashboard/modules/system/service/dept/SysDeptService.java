package cn.iocoder.dashboard.modules.system.service.dept;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;

import java.util.List;

/**
 * 部门 Service 接口
 *
 * @author 芋道源码
 */
public interface SysDeptService {

    /**
     * 获得所有部门列表
     *
     * @return 部门列表
     */
    List<SysDeptDO> listDepts();

    /**
     * 筛选部门列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 部门列表
     */
    List<SysDeptDO> listDepts(SysDeptListReqVO reqVO);

}
