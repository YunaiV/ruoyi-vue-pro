package cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanParamDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.stocktaking.plan.MesWmStockTakingPlanParamMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_STOCK_TAKING_PLAN_PARAM_NOT_EXISTS;

/**
 * MES 盘点方案参数 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmStockTakingPlanParamServiceImpl implements MesWmStockTakingPlanParamService {

    @Resource
    private MesWmStockTakingPlanParamMapper stockTakingPlanParamMapper;
    @Resource
    @Lazy
    private MesWmStockTakingPlanService stockTakingPlanService;

    @Override
    public Long createStockTakingPlanParam(MesWmStockTakingPlanParamSaveReqVO createReqVO) {
        // 校验盘点方案是否可编辑
        stockTakingPlanService.validateStockTakingPlanEditable(createReqVO.getPlanId());

        // 创建盘点方案参数
        MesWmStockTakingPlanParamDO param = BeanUtils.toBean(createReqVO, MesWmStockTakingPlanParamDO.class);
        stockTakingPlanParamMapper.insert(param);
        return param.getId();
    }

    @Override
    public void updateStockTakingPlanParam(MesWmStockTakingPlanParamSaveReqVO updateReqVO) {
        // 校验盘点方案参数存在
        MesWmStockTakingPlanParamDO param = validateStockTakingPlanParamExists(updateReqVO.getId());
        // 校验盘点方案是否可编辑
        stockTakingPlanService.validateStockTakingPlanEditable(param.getPlanId());

        // 更新盘点方案参数
        MesWmStockTakingPlanParamDO updateObj = BeanUtils.toBean(updateReqVO, MesWmStockTakingPlanParamDO.class);
        updateObj.setPlanId(param.getPlanId());
        stockTakingPlanParamMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockTakingPlanParam(Long id) {
        // 校验盘点方案参数存在
        MesWmStockTakingPlanParamDO param = validateStockTakingPlanParamExists(id);
        // 校验盘点方案是否可编辑
        stockTakingPlanService.validateStockTakingPlanEditable(param.getPlanId());

        // 删除盘点方案参数
        stockTakingPlanParamMapper.deleteById(id);
    }

    @Override
    public MesWmStockTakingPlanParamDO getStockTakingPlanParam(Long id) {
        return stockTakingPlanParamMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmStockTakingPlanParamDO> getStockTakingPlanParamPage(MesWmStockTakingPlanParamPageReqVO pageReqVO) {
        return stockTakingPlanParamMapper.selectPage(pageReqVO);
    }

    @Override
    public void deleteStockTakingPlanParamByPlanId(Long planId) {
        stockTakingPlanParamMapper.deleteByPlanId(planId);
    }

    @Override
    public MesWmStockTakingPlanParamDO validateStockTakingPlanParamExists(Long id) {
        MesWmStockTakingPlanParamDO param = stockTakingPlanParamMapper.selectById(id);
        if (param == null) {
            throw exception(WM_STOCK_TAKING_PLAN_PARAM_NOT_EXISTS);
        }
        return param;
    }

    @Override
    public List<MesWmStockTakingPlanParamDO> getStockTakingPlanParamListByPlanId(Long planId) {
        return stockTakingPlanParamMapper.selectListByPlanId(planId);
    }

}
