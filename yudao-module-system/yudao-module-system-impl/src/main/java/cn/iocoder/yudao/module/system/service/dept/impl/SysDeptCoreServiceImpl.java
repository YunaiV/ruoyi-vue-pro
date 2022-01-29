package cn.iocoder.yudao.module.system.service.dept.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.SysDeptCoreMapper;
import cn.iocoder.yudao.module.system.service.dept.SysDeptCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.system.enums.SysErrorCodeConstants.DEPT_NOT_ENABLE;
import static cn.iocoder.yudao.module.system.enums.SysErrorCodeConstants.DEPT_NOT_FOUND;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 部门 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysDeptCoreServiceImpl implements SysDeptCoreService {

    @Resource
    private SysDeptCoreMapper deptCoreMapper;

    @Override
    public List<SysDeptDO> getDepts(Collection<Long> ids) {
        return deptCoreMapper.selectBatchIds(ids);
    }

    @Override
    public SysDeptDO getDept(Long id) {
        return deptCoreMapper.selectById(id);
    }

    @Override
    public void validDepts(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得科室信息
        List<SysDeptDO> depts = deptCoreMapper.selectBatchIds(ids);
        Map<Long, SysDeptDO> deptMap = CollectionUtils.convertMap(depts, SysDeptDO::getId);
        // 校验
        ids.forEach(id -> {
            SysDeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE, dept.getName());
            }
        });
    }

    @Override
    public List<SysDeptDO> getSimpleDepts(Collection<Long> ids) {
        return deptCoreMapper.selectBatchIds(ids);
    }

}
