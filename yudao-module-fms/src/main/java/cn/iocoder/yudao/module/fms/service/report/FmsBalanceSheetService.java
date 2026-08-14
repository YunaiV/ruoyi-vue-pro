package cn.iocoder.yudao.module.fms.service.report;

import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * FMS 资产负债表 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsBalanceSheetService {

    /**
     * 获得资产负债表
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 资产负债表
     */
    List<FmsBalanceSheetRowRespVO> getBalanceSheet(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 获得资产负债行次金额 Map，供现金流量表取数
     *
     * 账套权限由调用方校验；报表快照按查询条件的期间初始化，行次金额按取数区间实时计算
     *
     * @param listReqVO 查询条件，用于初始化报表快照
     * @param startMonth 取数开始月份
     * @param endMonth 取数结束月份
     * @param userId 用户编号
     * @return 行次到报表项目的 Map
     */
    Map<Integer, FmsReportItemRespVO> getBalanceSheetLineMap(FmsReportListReqVO listReqVO,
            YearMonth startMonth, YearMonth endMonth, Long userId);

    /**
     * 检查资产负债表的平衡、初始余额、损益结转和公式覆盖状态
     *
     * @param listReqVO 查询条件
     * @param userId 用户编号
     * @return 检查结果
     */
    FmsBalanceSheetCheckRespVO checkBalanceSheet(FmsReportListReqVO listReqVO, Long userId);

    /**
     * 更新资产负债表公式
     *
     * @param updateReqVO 公式信息
     * @param userId 用户编号
     */
    void updateBalanceSheetFormula(FmsReportFormulaUpdateReqVO updateReqVO, Long userId);

}
