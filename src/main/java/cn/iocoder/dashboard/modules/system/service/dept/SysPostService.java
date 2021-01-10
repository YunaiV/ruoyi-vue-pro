package cn.iocoder.dashboard.modules.system.service.dept;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * 岗位 Service 接口
 *
 * @author 芋道源码
 */
public interface SysPostService {

    /**
     * 获得所有岗位列表
     *
     * @param ids 岗位编号数组。如果为空，不进行筛选
     * @param statuses 状态数组。如果为空，不进行筛选
     * @return 部门列表
     */
    List<SysPostDO> listPosts(@Nullable Collection<Long> ids, @Nullable Collection<Integer> statuses);

}
