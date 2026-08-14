package cn.iocoder.yudao.module.fms.service.report;

import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowStatementUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;

import java.util.List;

/**
 * FMS 现金流量表 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsCashFlowStatementService {

    /**
     * 获得现金流量表
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 现金流量表
     */
    List<FmsReportItemRespVO> getCashFlowStatement(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 更新现金流量表
     *
     * @param updateReqVO 修改信息
     * @param userId 用户编号
     */
    void updateCashFlowStatement(FmsCashFlowStatementUpdateReqVO updateReqVO, Long userId);

    /**
     * 检查现金流量表取数依赖的资产负债表状态
     *
     * 现金流量表本身无独立平衡关系，检查结果复用资产负债表检查，并汇总是否满足取数条件
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 检查结果
     */
    FmsCashFlowCheckRespVO checkCashFlowStatement(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 获得现金流量辅助数据列表
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 现金流量辅助数据列表
     */
    List<FmsCashFlowAdjustmentRespVO> getCashFlowAdjustmentList(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 更新现金流量辅助数据的人工调整金额
     *
     * @param updateReqVO 辅助数据
     * @param userId 用户编号
     */
    void updateCashFlowAdjustment(FmsCashFlowAdjustmentUpdateReqVO updateReqVO, Long userId);

    /**
     * 更新现金流量辅助数据公式
     *
     * @param updateReqVO 公式信息
     * @param userId 用户编号
     */
    void updateCashFlowAdjustmentFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId);

}
