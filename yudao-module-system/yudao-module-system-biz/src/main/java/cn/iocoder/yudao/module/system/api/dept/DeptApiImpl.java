package cn.iocoder.yudao.module.system.api.dept;

import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * 部门 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class DeptApiImpl implements DeptApi {

    @Resource
    private DeptService deptService;

    private DeptConvert deptConvert = DeptConvert.INSTANCE;

    @Override
    public DeptRespDTO getDept(Long id) {
        DeptDO dept = deptService.getDept(id);
        return deptConvert.toRespDTO(dept);
    }

    @Override
    public List<DeptRespDTO> getDeptList(Collection<Long> ids) {
        List<DeptDO> depts = deptService.getDeptList(ids);
        return deptConvert.toRespDTOs(depts);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        deptService.validateDeptList(ids);
    }

    @Override
    public List<DeptRespDTO> getChildDeptList(Long id) {
        List<DeptDO> childDeptList = deptService.getChildDeptList(id);
        return deptConvert.toRespDTOs(childDeptList);
    }

    @Override
    public Integer getDeptLevel(Long id) {
        return deptService.getDeptLevel(id);
    }

    @Override
    public String getParentNameById(Long id) {
        return deptService.getParentNameById(id);
    }

    @Override
    public TreeSet<DeptLevelRespDTO> getDeptTreeLevel(Long deptId) {
        return deptService.getDeptTreeLevel(deptId);
    }

    @Override
    public void updateDept(DeptSaveReqDTO dept) {
        deptService.updateDept(dept);
    }

    @Override
    public Long createDept(DeptSaveReqDTO dept) {
        return deptService.createDept(dept);
    }


}
