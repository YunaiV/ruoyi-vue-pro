package cn.iocoder.yudao.module.fms.service.home;

import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeMetricDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeRespVO;

/**
 * FMS 首页 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsHomeService {

    /**
     * 获得首页
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 首页信息
     */
    FmsHomeRespVO getHome(Long accountSetId, Long userId);

    /**
     * 获得财务指标明细
     *
     * @param accountSetId 账套编号
     * @param metricKey 指标标识
     * @param userId 当前用户编号
     * @return 财务指标明细
     */
    FmsHomeMetricDetailRespVO getMetricDetail(Long accountSetId, String metricKey, Long userId);

}
