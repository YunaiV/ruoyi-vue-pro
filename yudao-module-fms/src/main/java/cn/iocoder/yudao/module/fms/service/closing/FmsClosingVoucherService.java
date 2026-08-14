package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingVoucherGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossGenerateReqVO;

import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * FMS 结转凭证 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsClosingVoucherService {

    /**
     * 获得指定期间的结转凭证关联列表
     *
     * @param accountSetId 账套编号
     * @param beginTime 期间开始时间
     * @param endTime 期间结束时间
     * @return 结转凭证关联列表
     */
    List<FmsClosingVoucherDO> getClosingVoucherListByPeriod(Long accountSetId,
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得指定方案和期间的结转凭证关联列表
     *
     * @param closingId 结账方案编号
     * @param beginTime 开始时间，为空时不限制
     * @param endTime 结束时间，为空时不限制
     * @return 结转凭证关联列表
     */
    List<FmsClosingVoucherDO> getClosingVoucherListByClosingIdAndPeriod(Long closingId,
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 更新指定期间的结转凭证结账状态
     *
     * @param accountSetId 账套编号
     * @param beginTime 期间开始时间
     * @param endTime 期间结束时间
     * @param closed 是否已结账
     */
    void updateClosingVoucherClosedByPeriod(Long accountSetId, LocalDateTime beginTime,
            LocalDateTime endTime, Boolean closed);

    /**
     * 获得方案已生成的结转凭证数量
     *
     * @param closingId 方案编号
     * @return 结转凭证数量
     */
    Long getClosingVoucherCountByClosingId(Long closingId);

    /**
     * 获得结转凭证编号集合
     *
     * @param accountSetId 账套编号
     * @param voucherIds 凭证编号数组
     * @return 结转凭证编号集合
     */
    Set<Long> getClosingVoucherIdSet(Long accountSetId, Collection<Long> voucherIds);

    /**
     * 生成结转损益凭证
     *
     * @param generateReqVO 生成参数
     * @param userId 用户编号
     * @return 凭证编号
     */
    Long generateProfitLossVoucher(FmsProfitLossGenerateReqVO generateReqVO, Long userId);

    /**
     * 生成结账方案凭证
     *
     * @param generateReqVO 生成参数
     * @param userId 用户编号
     * @return 凭证编号
     */
    Long generateClosingSchemeVoucher(FmsClosingSchemeGenerateReqVO generateReqVO, Long userId);

    /**
     * 批量生成结转凭证
     *
     * @param generateReqVO 生成参数
     * @param userId 用户编号
     * @return 凭证编号数组
     */
    List<Long> generateClosingVoucherList(FmsClosingVoucherGenerateReqVO generateReqVO, Long userId);

}
