package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * FMS 结账期间 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsClosingPeriodService {

    /**
     * 校验业务时间所属期间未结账
     *
     * @param accountSetId 账套编号
     * @param businessTime 业务时间
     */
    void validatePeriodOpen(Long accountSetId, LocalDateTime businessTime);

    /**
     * 判断业务时间所属期间是否已结账
     *
     * @param accountSetId 账套编号
     * @param businessTime 业务时间
     * @return 是否已结账
     */
    boolean isPeriodClosed(Long accountSetId, LocalDateTime businessTime);

    /**
     * 获得当前会计期间
     *
     * @param accountSetId 账套编号
     * @param startTime 账套启用时间
     * @return 当前会计期间
     */
    YearMonth getCurrentMonth(Long accountSetId, LocalDateTime startTime);

    /**
     * 获得当前会计期间
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 当前会计期间
     */
    String getCurrentMonth(Long accountSetId, Long userId);

    /**
     * 获得结账概况
     *
     * @param queryReqVO 查询参数
     * @param userId 用户编号
     * @return 结账概况
     */
    FmsClosingOverviewRespVO getClosingOverview(FmsClosingQueryReqVO queryReqVO, Long userId);

    /**
     * 结账
     *
     * @param queryReqVO 结账期间
     * @param userId 用户编号
     */
    void closePeriod(FmsClosingQueryReqVO queryReqVO, Long userId);

    /**
     * 反结账
     *
     * @param queryReqVO 结账期间
     * @param userId 用户编号
     */
    void cancelClosePeriod(FmsClosingQueryReqVO queryReqVO, Long userId);

    /**
     * 判断指定会计期间是否已结账
     *
     * @param accountSetId 账套编号
     * @param month 会计期间
     * @return 是否已结账
     */
    boolean isPeriodClosed(Long accountSetId, String month);

}
