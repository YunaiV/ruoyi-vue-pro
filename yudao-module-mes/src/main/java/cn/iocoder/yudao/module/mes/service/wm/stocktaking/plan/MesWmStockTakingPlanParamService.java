package cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanParamDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 盘点方案参数 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmStockTakingPlanParamService {

    /**
     * 创建盘点方案参数
     *
     * @param createReqVO 创建信息
     * @return 参数编号
     */
    Long createStockTakingPlanParam(@Valid MesWmStockTakingPlanParamSaveReqVO createReqVO);

    /**
     * 更新盘点方案参数
     *
     * @param updateReqVO 更新信息
     */
    void updateStockTakingPlanParam(@Valid MesWmStockTakingPlanParamSaveReqVO updateReqVO);

    /**
     * 删除盘点方案参数
     *
     * @param id 参数编号
     */
    void deleteStockTakingPlanParam(Long id);

    /**
     * 获得盘点方案参数
     *
     * @param id 参数编号
     * @return 盘点方案参数
     */
    MesWmStockTakingPlanParamDO getStockTakingPlanParam(Long id);

    /**
     * 分页查询盘点方案参数
     *
     * @param pageReqVO 分页查询条件
     * @return 盘点方案参数分页结果
     */
    PageResult<MesWmStockTakingPlanParamDO> getStockTakingPlanParamPage(MesWmStockTakingPlanParamPageReqVO pageReqVO);

    /**
     * 根据盘点方案编号删除参数
     *
     * @param planId 盘点方案编号
     */
    void deleteStockTakingPlanParamByPlanId(Long planId);

    /**
     * 校验盘点方案参数是否存在
     *
     * @param id 参数编号
     * @return 盘点方案参数
     */
    MesWmStockTakingPlanParamDO validateStockTakingPlanParamExists(Long id);

    /**
     * 根据盘点方案编号获取参数列表
     *
     * @param planId 盘点方案编号
     * @return 盘点方案参数列表
     */
    List<MesWmStockTakingPlanParamDO> getStockTakingPlanParamListByPlanId(Long planId);

}
