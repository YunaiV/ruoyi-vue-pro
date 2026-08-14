package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;

import java.util.Collection;
import java.util.List;

/**
 * FMS 初始余额 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsInitialBalanceService {

    /**
     * 获得初始余额列表
     *
     * 返回按科目层级展开的平铺列表，父级科目排在子级之前，父级余额由末级科目逐级汇总
     *
     * @param accountSetId 账套编号
     * @param subjectType 科目类别
     * @param userId 用户编号
     * @return 初始余额列表
     */
    List<FmsInitialBalanceRespVO> getInitialBalanceList(Long accountSetId, Integer subjectType, Long userId);

    /**
     * 保存初始余额
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void saveInitialBalance(FmsInitialBalanceSaveReqVO saveReqVO, Long userId);

    /**
     * 获得试算平衡结果
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 试算平衡结果
     */
    FmsTrialBalanceRespVO getTrialBalance(Long accountSetId, Long userId);

    /**
     * 导入初始余额
     *
     * @param accountSetId 账套编号
     * @param rows Excel 数据
     * @param userId 用户编号
     * @return 导入数量
     */
    int importInitialBalance(Long accountSetId, List<FmsInitialBalanceExcelVO> rows, Long userId);

    /**
     * 获得指定科目的初始余额数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 初始余额数量
     */
    Long getInitialBalanceCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 获得指定科目包含数量数据的初始余额数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 初始余额数量
     */
    Long getInitialBalanceQuantityCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 迁移科目的初始余额
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @param targetSubjectId 目标科目编号
     */
    void updateInitialBalanceSubject(Long accountSetId, Long subjectId, Long targetSubjectId);

    /**
     * 获得引用指定辅助核算项目的初始余额数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryItemIds 辅助核算项目编号数组
     * @return 初始余额数量
     */
    Long getInitialBalanceCountByAuxiliaryItemIds(Long accountSetId, Collection<Long> auxiliaryItemIds);

    /**
     * 获得引用指定辅助核算类别的初始余额数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @return 初始余额数量
     */
    Long getInitialBalanceCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId);

}
