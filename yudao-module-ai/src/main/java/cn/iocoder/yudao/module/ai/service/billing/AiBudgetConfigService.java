package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import jakarta.validation.Valid;

/**
 * AI 预算配置 Service 接口
 *
 * @author 芋道源码
 */
public interface AiBudgetConfigService {

    /**
     * 创建预算配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBudgetConfig(@Valid AiBudgetConfigSaveReqVO createReqVO);

    /**
     * 更新预算配置
     *
     * @param updateReqVO 更新信息
     */
    void updateBudgetConfig(@Valid AiBudgetConfigSaveReqVO updateReqVO);

    /**
     * 删除预算配置
     *
     * @param id 编号
     */
    void deleteBudgetConfig(Long id);

    /**
     * 获得预算配置
     *
     * @param id 编号
     * @return 预算配置
     */
    AiBudgetConfigDO getBudgetConfig(Long id);

    /**
     * 获得预算配置分页
     *
     * @param pageReqVO 分页查询
     * @return 预算配置分页
     */
    PageResult<AiBudgetConfigDO> getBudgetConfigPage(AiBudgetConfigPageReqVO pageReqVO);

    /**
     * 获得指定用户的预算配置
     *
     * @param userId     用户编号，0 表示租户级
     * @param periodType 周期类型
     * @return 预算配置，不存在返回 null
     */
    AiBudgetConfigDO getBudgetConfig(Long userId, String periodType);

}
