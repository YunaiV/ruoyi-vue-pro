package cn.iocoder.yudao.module.fms.service.report;

import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;

import java.util.List;

/**
 * FMS 利润表 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsIncomeStatementService {

    /**
     * 获得利润表
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 利润表
     */
    List<FmsReportItemRespVO> getIncomeStatement(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 检查利润表与资产负债表的勾稽关系和公式覆盖状态
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 检查结果
     */
    FmsIncomeStatementCheckRespVO checkIncomeStatement(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 更新利润表公式
     *
     * @param updateReqVO 公式信息
     * @param userId 用户编号
     */
    void updateIncomeStatementFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId);

}
