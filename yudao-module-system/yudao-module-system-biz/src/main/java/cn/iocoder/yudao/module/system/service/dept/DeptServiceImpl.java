package cn.iocoder.yudao.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.enums.dept.DeptIdEnum;
import cn.iocoder.yudao.module.system.mq.producer.dept.DeptProducer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class DeptServiceImpl implements DeptService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 部门缓存
     * key：部门编号 {@link DeptDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @SuppressWarnings("FieldCanBeLocal")
    private volatile Map<Long, DeptDO> deptCache;
    /**
     * 父部门缓存
     * key：部门编号 {@link DeptDO#getParentId()}
     * value: 直接子部门列表
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, DeptDO> parentDeptCache;
    /**
     * 缓存部门的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private DeptProducer deptProducer;

    /**
     * 初始化 {@link #parentDeptCache} 和 {@link #deptCache} 缓存
     */
    @Override
    @PostConstruct
    public synchronized void initLocalCache() {
        initLocalCacheIfUpdate(null);
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCacheIfUpdate(this.maxUpdateTime);
    }

    /**
     * 刷新本地缓存
     *
     * @param maxUpdateTime 最大更新时间
     *                      1. 如果 maxUpdateTime 为 null，则“强制”刷新缓存
     *                      2. 如果 maxUpdateTime 不为 null，判断自 maxUpdateTime 是否有数据发生变化，有的情况下才刷新缓存
     */
    private void initLocalCacheIfUpdate(LocalDateTime maxUpdateTime) {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 第一步：基于 maxUpdateTime 判断缓存是否刷新。
            // 如果没有增量的数据变化，则不进行本地缓存的刷新
            if (maxUpdateTime != null
                    && deptMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                log.info("[initLocalCacheIfUpdate][数据未发生变化({})，本地缓存不刷新]", maxUpdateTime);
                return;
            }
            List<DeptDO> depts = deptMapper.selectList();
            log.info("[initLocalCacheIfUpdate][缓存部门，数量为:{}]", depts.size());

            // 第二步：构建缓存。创建或更新支付 Client
            // 构建缓存
            ImmutableMap.Builder<Long, DeptDO> builder = ImmutableMap.builder();
            ImmutableMultimap.Builder<Long, DeptDO> parentBuilder = ImmutableMultimap.builder();
            depts.forEach(sysRoleDO -> {
                builder.put(sysRoleDO.getId(), sysRoleDO);
                parentBuilder.put(sysRoleDO.getParentId(), sysRoleDO);
            });
            // 设置缓存
            deptCache = builder.build();
            parentDeptCache = parentBuilder.build();

            // 第三步：设置最新的 maxUpdateTime，用于下次的增量判断。
            this.maxUpdateTime = CollectionUtils.getMaxValue(depts, DeptDO::getUpdateTime);
        });
    }

    @Override
    public Long createDept(DeptCreateReqVO reqVO) {
        // 校验正确性
        if (reqVO.getParentId() == null) {
            reqVO.setParentId(DeptIdEnum.ROOT.getId());
        }
        checkCreateOrUpdate(null, reqVO.getParentId(), reqVO.getName());
        // 插入部门
        DeptDO dept = DeptConvert.INSTANCE.convert(reqVO);
        deptMapper.insert(dept);
        // 发送刷新消息
        deptProducer.sendDeptRefreshMessage();
        return dept.getId();
    }

    @Override
    public void updateDept(DeptUpdateReqVO reqVO) {
        // 校验正确性
        if (reqVO.getParentId() == null) {
            reqVO.setParentId(DeptIdEnum.ROOT.getId());
        }
        checkCreateOrUpdate(reqVO.getId(), reqVO.getParentId(), reqVO.getName());
        // 更新部门
        DeptDO updateObj = DeptConvert.INSTANCE.convert(reqVO);
        deptMapper.updateById(updateObj);
        // 发送刷新消息
        deptProducer.sendDeptRefreshMessage();
    }

    @Override
    public void deleteDept(Long id) {
        // 校验是否存在
        checkDeptExists(id);
        // 校验是否有子部门
        if (deptMapper.selectCountByParentId(id) > 0) {
            throw ServiceExceptionUtil.exception(DEPT_EXITS_CHILDREN);
        }
        // 删除部门
        deptMapper.deleteById(id);
        // 发送刷新消息
        deptProducer.sendDeptRefreshMessage();
    }

    @Override
    public List<DeptDO> getSimpleDepts(DeptListReqVO reqVO) {
        return deptMapper.selectList(reqVO);
    }

    @Override
    public List<DeptDO> getDeptsByParentIdFromCache(Long parentId, boolean recursive) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        List<DeptDO> result = new ArrayList<>(); // TODO 芋艿：待优化，新增缓存，避免每次遍历的计算
        // 递归，简单粗暴
        this.getDeptsByParentIdFromCache(result, parentId,
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
    private void getDeptsByParentIdFromCache(List<DeptDO> result, Long parentId, int recursiveCount,
                                             Multimap<Long, DeptDO> parentDeptMap) {
        // 递归次数为 0，结束！
        if (recursiveCount == 0) {
            return;
        }

        // 获得子部门
        Collection<DeptDO> depts = parentDeptMap.get(parentId);
        if (CollUtil.isEmpty(depts)) {
            return;
        }
        // 针对多租户，过滤掉非当前租户的部门
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            depts = CollUtil.filterNew(depts, dept -> tenantId.equals(dept.getTenantId()));
        }
        result.addAll(depts);

        // 继续递归
        depts.forEach(dept -> getDeptsByParentIdFromCache(result, dept.getId(),
                recursiveCount - 1, parentDeptMap));
    }

    private void checkCreateOrUpdate(Long id, Long parentId, String name) {
        // 校验自己存在
        checkDeptExists(id);
        // 校验父部门的有效性
        checkParentDeptEnable(id, parentId);
        // 校验部门名的唯一性
        checkDeptNameUnique(id, parentId, name);
    }

    private void checkParentDeptEnable(Long id, Long parentId) {
        if (parentId == null || DeptIdEnum.ROOT.getId().equals(parentId)) {
            return;
        }
        // 不能设置自己为父部门
        if (parentId.equals(id)) {
            throw ServiceExceptionUtil.exception(DEPT_PARENT_ERROR);
        }
        // 父岗位不存在
        DeptDO dept = deptMapper.selectById(parentId);
        if (dept == null) {
            throw ServiceExceptionUtil.exception(DEPT_PARENT_NOT_EXITS);
        }
        // 父部门被禁用
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_ENABLE);
        }
        // 父部门不能是原来的子部门
        List<DeptDO> children = this.getDeptsByParentIdFromCache(id, true);
        if (children.stream().anyMatch(dept1 -> dept1.getId().equals(parentId))) {
            throw ServiceExceptionUtil.exception(DEPT_PARENT_IS_CHILD);
        }
    }

    private void checkDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND);
        }
    }

    private void checkDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO menu = deptMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的岗位
        if (id == null) {
            throw ServiceExceptionUtil.exception(DEPT_NAME_DUPLICATE);
        }
        if (!menu.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public List<DeptDO> getDepts(Collection<Long> ids) {
        return deptMapper.selectBatchIds(ids);
    }

    @Override
    public DeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public void validDepts(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得科室信息
        List<DeptDO> depts = deptMapper.selectBatchIds(ids);
        Map<Long, DeptDO> deptMap = CollectionUtils.convertMap(depts, DeptDO::getId);
        // 校验
        ids.forEach(id -> {
            DeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE, dept.getName());
            }
        });
    }

    @Override
    public List<DeptDO> getSimpleDepts(Collection<Long> ids) {
        return deptMapper.selectBatchIds(ids);
    }

}
