package cn.iocoder.yudao.module.system.api.dept;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
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

    @Override
    public DeptRespDTO getDept(Long id) {
        DeptDO dept = deptService.getDept(id);
        return BeanUtils.toBean(dept, DeptRespDTO.class);
    }

    @Override
    public List<DeptRespDTO> getDeptList(Collection<Long> ids) {
        List<DeptDO> depts = deptService.getDeptList(ids);
        return BeanUtils.toBean(depts, DeptRespDTO.class);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        deptService.validateDeptList(ids);
    }

    @Override
    public List<DeptRespDTO> getChildDeptList(Long id) {
        List<DeptDO> childDeptList = deptService.getChildDeptList(id);
        return BeanUtils.toBean(childDeptList, DeptRespDTO.class);
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
    public TreeSet<DeptLevelRespDTO> getDeptTreeLevel(Long productDeptId) {
        return deptService.getDeptTreeLevel(productDeptId);
    }

    @Override
    public void updateDept(DeptReqDTO erpDepartment) {
        DeptSaveReqVO deptSaveReqVO = BeanUtils.toBean(erpDepartment, DeptSaveReqVO.class);
        deptService.updateDept(deptSaveReqVO);
    }

    @Override
    public Long createDept(DeptReqDTO erpDepartment) {
        DeptSaveReqVO deptSaveReqVO = BeanUtils.toBean(erpDepartment, DeptSaveReqVO.class);
        return deptService.createDept(deptSaveReqVO);
    }


}
