package cn.iocoder.dashboard.modules.system.service.dept.impl;

import cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept.SysDeptMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 部门 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysDeptServiceImpl implements SysDeptService {

    @Resource
    private SysDeptMapper deptMapper;

    @Override
    public List<SysDeptDO> listDepts() {
        return deptMapper.selectList();
    }

}
