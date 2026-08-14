package cn.iocoder.yudao.module.fms.service.ledger;

import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerAuxiliaryListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarybalance.FmsLedgerAuxiliaryBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarydetail.FmsLedgerAuxiliaryDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.general.FmsLedgerGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitydetail.FmsLedgerQuantityDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitygeneral.FmsLedgerQuantityGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * FMS 账簿 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsLedgerService {

    // ==================== 公共查询 ====================

    /**
     * 获得指定期间的账簿凭证分录列表
     *
     * @param accountSetId 账套编号
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户编号
     * @return 凭证分录列表
     */
    List<FmsLedgerEntryVO> getEntryList(Long accountSetId, LocalDateTime beginTime,
            LocalDateTime endTime, Long userId);

    /**
     * 获得指定辅助核算组合的余额
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @param subjectId 科目编号
     * @param auxiliaryItemIds 辅助核算项目编号数组
     * @param userId 用户编号
     * @return 借方为正、贷方为负的余额
     */
    BigDecimal getAuxiliaryCombinationBalance(Long accountSetId, String month, Long subjectId,
            Collection<Long> auxiliaryItemIds, Long userId);

    // ==================== 明细账 ====================

    /**
     * 获得明细账列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 明细账列表
     */
    List<FmsLedgerDetailRespVO> getDetailList(FmsLedgerListReqVO listReqVO, Long userId);

    /**
     * 获得指定期间有发生额的明细账科目树
     *
     * @param listReqVO 查询参数
     * @param userId 用户编号
     * @return 科目列表
     */
    List<FmsSubjectDO> getDetailSubjectList(FmsLedgerListReqVO listReqVO, Long userId);

    // ==================== 总账 ====================

    /**
     * 获得总账列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 总账列表
     */
    List<FmsLedgerGeneralRespVO> getGeneralList(FmsLedgerListReqVO listReqVO, Long userId);

    // ==================== 科目余额表 ====================

    /**
     * 获得科目余额表列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 科目余额表列表
     */
    List<FmsLedgerSubjectBalanceRespVO> getSubjectBalanceList(
            FmsLedgerListReqVO listReqVO, Long userId);

    // ==================== 多栏账 ====================

    /**
     * 获得多栏账
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 多栏账
     */
    FmsLedgerMultiColumnRespVO getMultiColumn(FmsLedgerListReqVO listReqVO, Long userId);

    // ==================== 核算项目明细账 ====================

    /**
     * 获得核算项目明细账列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 核算项目明细账列表
     */
    List<FmsLedgerAuxiliaryDetailRespVO> getAuxiliaryDetailList(
            FmsLedgerAuxiliaryListReqVO listReqVO, Long userId);

    // ==================== 核算项目余额表 ====================

    /**
     * 获得核算项目余额表列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 核算项目余额表列表
     */
    List<FmsLedgerAuxiliaryBalanceRespVO> getAuxiliaryBalanceList(
            FmsLedgerAuxiliaryListReqVO listReqVO, Long userId);

    // ==================== 数量金额明细账 ====================

    /**
     * 获得数量金额明细账列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 数量金额明细账列表
     */
    List<FmsLedgerQuantityDetailRespVO> getQuantityDetailList(FmsLedgerListReqVO listReqVO, Long userId);

    // ==================== 数量金额总账 ====================

    /**
     * 获得数量金额总账列表
     *
     * @param listReqVO 列表查询参数
     * @param userId 用户编号
     * @return 数量金额总账列表
     */
    List<FmsLedgerQuantityGeneralRespVO> getQuantityGeneralList(FmsLedgerListReqVO listReqVO, Long userId);

}
