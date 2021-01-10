package cn.iocoder.dashboard.modules.system.service.dept.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept.SysDeptMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysDeptServiceImpl implements SysDeptService {

    /**
     * 部门缓存
     * key：部门编号 {@link SysDeptDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, SysDeptDO> deptCache;
    /**
     * 父部门缓存
     * key：部门编号 {@link SysDeptDO#getParentId()}
     * value: 直接子部门列表
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, SysDeptDO> parentDeptCache;

    @Resource
    private SysDeptMapper deptMapper;

    @Override
    @PostConstruct
    public void init() {
        // 从数据库中读取
        List<SysDeptDO> sysDeptDOList = deptMapper.selectList();
        // 构建缓存
        ImmutableMap.Builder<Long, SysDeptDO> builder = ImmutableMap.builder();
        ImmutableMultimap.Builder<Long, SysDeptDO> parentBuilder = ImmutableMultimap.builder();
        sysDeptDOList.forEach(sysRoleDO -> {
            builder.put(sysRoleDO.getId(), sysRoleDO);
            parentBuilder.put(sysRoleDO.getParentId(), sysRoleDO);
        });
        // 设置缓存
        deptCache = builder.build();
        parentDeptCache = parentBuilder.build();
        log.info("[init][初始化 Dept 数量为 {}]", sysDeptDOList.size());
    }

    @Override
    public List<SysDeptDO> listDepts() {
        return deptMapper.selectList();
    }

    @Override
    public List<SysDeptDO> listDepts(Collection<Long> ids) {
        return deptMapper.selectBatchIds(ids);
    }

    @Override
    public List<SysDeptDO> listDepts(SysDeptListReqVO reqVO) {
        return deptMapper.selectList(reqVO);
    }

    @Override
    public List<SysDeptDO> listDeptsByParentIdFromCache(Long parentId, boolean recursive) {
        List<SysDeptDO> result = new ArrayList<>();
        // 递归，简单粗暴
        this.listDeptsByParentIdFromCache(result, parentId,
                recursive ? Integer.MAX_VALUE : 1, // 如果递归获取，则无限；否则，只递归 1 次
                parentDeptCache);
        return result;
    }

    /**
     * 递归获取所有的子部门，添加到 result 结果
     *
     * @param result 结果
     * @param parentId 父编号
     * @param recursiveCount 递归次数
     * @param parentDeptMap 父部门 Map，使用缓存，避免变化
     */
    private void listDeptsByParentIdFromCache(List<SysDeptDO> result, Long parentId, int recursiveCount,
                                              Multimap<Long, SysDeptDO> parentDeptMap) {
        // 递归次数为 0，结束！
        if (recursiveCount == 0) {
            return;
        }
        // 获得子部门
        Collection<SysDeptDO> depts = parentDeptMap.get(parentId);
        if (CollUtil.isEmpty(depts)) {
            return;
        }
        result.addAll(depts);
        // 继续递归
        depts.forEach(dept -> listDeptsByParentIdFromCache(result, dept.getId(),
                recursiveCount - 1, parentDeptMap));
    }

    @Override
    public SysDeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

}
