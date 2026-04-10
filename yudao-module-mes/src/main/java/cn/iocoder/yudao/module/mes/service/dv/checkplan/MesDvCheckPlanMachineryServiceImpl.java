package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.machinery.MesDvCheckPlanMachinerySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanMachineryDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.checkplan.MesDvCheckPlanMachineryMapper;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 点检保养方案设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvCheckPlanMachineryServiceImpl implements MesDvCheckPlanMachineryService {

    @Resource
    private MesDvCheckPlanMachineryMapper checkPlanMachineryMapper;

    @Resource
    @Lazy
    private MesDvCheckPlanService checkPlanService;
    @Resource
    private MesDvMachineryService machineryService;

    @Override
    public Long createCheckPlanMachinery(MesDvCheckPlanMachinerySaveReqVO createReqVO) {
        // 1.1 校验方案草稿状态
        MesDvCheckPlanDO currentPlan = checkPlanService.validateCheckPlanPrepare(createReqVO.getPlanId());
        // 1.2 校验设备存在
        machineryService.validateMachineryExists(createReqVO.getMachineryId());
        // 1.3 校验同一方案下设备不重复
        if (checkPlanMachineryMapper.selectByPlanIdAndMachineryId(createReqVO.getPlanId(), createReqVO.getMachineryId()) != null) {
            throw exception(DV_CHECK_PLAN_MACHINERY_DUPLICATE);
        }
        // 1.4 跨方案类型唯一性校验（设备不能存在于同类型多个方案中）
        List<MesDvCheckPlanMachineryDO> existingMachineryList = getCheckPlanMachineryListByMachineryId(createReqVO.getMachineryId());
        if (CollUtil.isNotEmpty(existingMachineryList)) {
            List<Long> existingPlanIds = existingMachineryList.stream().map(MesDvCheckPlanMachineryDO::getPlanId).collect(Collectors.toList());
            List<MesDvCheckPlanDO> existingPlans = checkPlanService.getCheckPlanList(existingPlanIds);
            for (MesDvCheckPlanDO existPlan : existingPlans) {
                // 如果存在不同于当前方案、但类型一致的计划方案，则拦截（一机多计划，但不允许同一机多同类型计划）
                if (ObjUtil.notEqual(existPlan.getId(), currentPlan.getId())
                        && ObjUtil.equal(existPlan.getType(), currentPlan.getType())) {
                    throw exception(DV_CHECK_PLAN_MACHINERY_EXISTS_IN_SAME_TYPE);
                }
            }
        }

        // 2. 插入
        MesDvCheckPlanMachineryDO planMachinery = BeanUtils.toBean(createReqVO, MesDvCheckPlanMachineryDO.class);
        checkPlanMachineryMapper.insert(planMachinery);
        return planMachinery.getId();
    }

    @Override
    public void deleteCheckPlanMachinery(Long id) {
        // 1.1 校验存在
        MesDvCheckPlanMachineryDO existRecord = checkPlanMachineryMapper.selectById(id);
        if (existRecord == null) {
            throw exception(DV_CHECK_PLAN_MACHINERY_NOT_EXISTS);
        }
        // 1.2 校验方案草稿状态
        checkPlanService.validateCheckPlanPrepare(existRecord.getPlanId());

        // 2. 删除
        checkPlanMachineryMapper.deleteById(id);
    }

    @Override
    public List<MesDvCheckPlanMachineryDO> getCheckPlanMachineryListByPlanId(Long planId) {
        return checkPlanMachineryMapper.selectListByPlanId(planId);
    }

    @Override
    public Long getCheckPlanMachineryCountByPlanId(Long planId) {
        return checkPlanMachineryMapper.selectCountByPlanId(planId);
    }

    @Override
    public void deleteByPlanId(Long planId) {
        checkPlanMachineryMapper.deleteByPlanId(planId);
    }

    @Override
    public Long getCheckPlanMachineryCountByMachineryId(Long machineryId) {
        return checkPlanMachineryMapper.selectCountByMachineryId(machineryId);
    }

    @Override
    public List<MesDvCheckPlanMachineryDO> getCheckPlanMachineryListByMachineryId(Long machineryId) {
        return checkPlanMachineryMapper.selectListByMachineryId(machineryId);
    }

}
