package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator.FmsFinanceIndicatorSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;

import java.util.List;

// TODO DONE @AI：Service 接口保留给 Controller、首页和账套初始化调用；方法文档已按 FMS 配置 Service 补齐。
/**
 * FMS 首页财务指标 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsFinanceIndicatorService {

    /**
     * 创建首页财务指标
     *
     * @param reqVO 创建信息
     * @param userId 当前用户编号
     * @return 指标编号
     */
    Long createFinanceIndicator(FmsFinanceIndicatorSaveReqVO reqVO, Long userId);

    /**
     * 更新首页财务指标
     *
     * @param reqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateFinanceIndicator(FmsFinanceIndicatorSaveReqVO reqVO, Long userId);

    /**
     * 删除首页财务指标
     *
     * @param accountSetId 账套编号
     * @param id 指标编号
     * @param userId 当前用户编号
     */
    void deleteFinanceIndicator(Long accountSetId, Long id, Long userId);

    /**
     * 获得首页财务指标
     *
     * @param accountSetId 账套编号
     * @param id 指标编号
     * @param userId 当前用户编号
     * @return 财务指标
     */
    FmsFinanceIndicatorDO getFinanceIndicator(Long accountSetId, Long id, Long userId);

    /**
     * 获得首页财务指标列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 财务指标列表
     */
    List<FmsFinanceIndicatorDO> getFinanceIndicatorList(Long accountSetId, Long userId);

    /**
     * 获得已启用的首页财务指标列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 已启用的财务指标列表
     */
    List<FmsFinanceIndicatorDO> getEnabledFinanceIndicatorList(Long accountSetId, Long userId);

    /**
     * 初始化账套的默认首页财务指标
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     */
    void initializeDefaultFinanceIndicators(Long accountSetId, Long userId);
}
