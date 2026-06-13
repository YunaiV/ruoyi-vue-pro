package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.process.MesProRouteProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.route.MesProRouteProcessMapper;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工艺路线工序 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProRouteProcessServiceImpl implements MesProRouteProcessService {

    @Resource
    private MesProRouteProcessMapper routeProcessMapper;

    @Resource
    @Lazy
    private MesProRouteProductService routeProductService;
    @Resource
    @Lazy
    private MesProProcessService processService;
    @Resource
    @Lazy
    private MesProRouteService routeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRouteProcess(MesProRouteProcessSaveReqVO createReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(createReqVO.getRouteId());
        // 1.1 校验工艺路线、工序存在
        validateRouteAndProcessExists(createReqVO.getRouteId(), createReqVO.getProcessId());
        // 1.2 校验唯一性
        validateSortUnique(null, createReqVO.getRouteId(), createReqVO.getSort());
        validateProcessUnique(null, createReqVO.getRouteId(), createReqVO.getProcessId());
        validateKeyProcessUnique(null, createReqVO.getRouteId(), createReqVO.getKeyFlag());

        // 2. 插入
        MesProRouteProcessDO routeProcess = BeanUtils.toBean(createReqVO, MesProRouteProcessDO.class);
        routeProcessMapper.insert(routeProcess);

        // 3. 更新工序链
        rebuildProcessChain(createReqVO.getRouteId());
        return routeProcess.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRouteProcess(MesProRouteProcessSaveReqVO updateReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(updateReqVO.getRouteId());
        // 1.1 校验存在
        validateRouteProcessExists(updateReqVO.getId());
        // 1.2 校验工艺路线、工序存在
        validateRouteAndProcessExists(updateReqVO.getRouteId(), updateReqVO.getProcessId());
        // 1.3 校验唯一性
        validateSortUnique(updateReqVO.getId(), updateReqVO.getRouteId(), updateReqVO.getSort());
        validateProcessUnique(updateReqVO.getId(), updateReqVO.getRouteId(), updateReqVO.getProcessId());
        validateKeyProcessUnique(updateReqVO.getId(), updateReqVO.getRouteId(), updateReqVO.getKeyFlag());

        // 2. 更新
        MesProRouteProcessDO updateObj = BeanUtils.toBean(updateReqVO, MesProRouteProcessDO.class);
        routeProcessMapper.updateById(updateObj);

        // 3. 重建工序链
        rebuildProcessChain(updateReqVO.getRouteId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteProcess(Long id) {
        // 1.1 校验存在
        MesProRouteProcessDO routeProcess = validateRouteProcessExists(id);
        // 1.2 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(routeProcess.getRouteId());

        // 2. 删除
        routeProcessMapper.deleteById(id);

        // 3. 重建工序链
        rebuildProcessChain(routeProcess.getRouteId());
    }

    /**
     * 重建工序链（更新所有工序的 nextProcessId）
     */
    private void rebuildProcessChain(Long routeId) {
        List<MesProRouteProcessDO> list = routeProcessMapper.selectListByRouteId(routeId);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 按 sort 排序
        list.sort(Comparator.comparing(MesProRouteProcessDO::getSort));
        for (int i = 0; i < list.size(); i++) {
            MesProRouteProcessDO item = list.get(i);
            if (i + 1 < list.size()) {
                item.setNextProcessId(list.get(i + 1).getProcessId());
            } else {
                item.setNextProcessId(null);
            }
            routeProcessMapper.updateById(item);
        }
    }

    private MesProRouteProcessDO validateRouteProcessExists(Long id) {
        MesProRouteProcessDO routeProcess = routeProcessMapper.selectById(id);
        if (routeProcess == null) {
            throw exception(PRO_ROUTE_PROCESS_NOT_EXISTS);
        }
        return routeProcess;
    }

    private void validateRouteAndProcessExists(Long routeId, Long processId) {
        if (routeService.getRoute(routeId) == null) {
            throw exception(PRO_ROUTE_NOT_EXISTS);
        }
        if (processService.getProcess(processId) == null) {
            throw exception(PRO_ROUTE_PROCESS_NOT_EXISTS);
        }
    }

    private void validateSortUnique(Long id, Long routeId, Integer sort) {
        MesProRouteProcessDO existing = routeProcessMapper.selectByRouteIdAndSort(routeId, sort);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(PRO_ROUTE_PROCESS_SORT_DUPLICATE);
        }
    }

    private void validateProcessUnique(Long id, Long routeId, Long processId) {
        MesProRouteProcessDO existing = routeProcessMapper.selectByRouteIdAndProcessId(routeId, processId);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(PRO_ROUTE_PROCESS_DUPLICATE);
        }
    }

    private void validateKeyProcessUnique(Long id, Long routeId, Boolean keyFlag) {
        if (!Boolean.TRUE.equals(keyFlag)) {
            return;
        }
        MesProRouteProcessDO existing = routeProcessMapper.selectKeyProcessByRouteId(routeId);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(PRO_ROUTE_PROCESS_KEY_DUPLICATE);
        }
    }

    @Override
    public MesProRouteProcessDO getRouteProcess(Long id) {
        return routeProcessMapper.selectById(id);
    }

    @Override
    public List<MesProRouteProcessDO> getRouteProcessListByRouteId(Long routeId) {
        return routeProcessMapper.selectListByRouteId(routeId);
    }

    @Override
    public List<MesProRouteProcessDO> getRouteProcessListByRouteIds(Collection<Long> routeIds) {
        if (CollUtil.isEmpty(routeIds)) {
            return Collections.emptyList();
        }
        return routeProcessMapper.selectListByRouteIds(routeIds);
    }

    @Override
    public MesProRouteProcessDO getRouteProcessByRouteIdAndProcessId(Long routeId, Long processId) {
        return routeProcessMapper.selectByRouteIdAndProcessId(routeId, processId);
    }

    @Override
    public List<MesProRouteProcessDO> getRouteProcessListByProductId(Long productId) {
        // 1. 根据产品查找关联的工艺路线产品记录
        MesProRouteProductDO routeProduct = routeProductService.getRouteProductByItemId(productId);
        if (routeProduct == null) {
            return Collections.emptyList();
        }
        // 2. 返回该工艺路线的工序列表
        return routeProcessMapper.selectListByRouteId(routeProduct.getRouteId());
    }

    @Override
    public List<MesProRouteProcessDO> getRouteProcessListByProcessId(Long processId) {
        return routeProcessMapper.selectListByProcessId(processId);
    }

    @Override
    public void deleteRouteProcessByRouteId(Long routeId) {
        routeProcessMapper.deleteByRouteId(routeId);
    }

}
