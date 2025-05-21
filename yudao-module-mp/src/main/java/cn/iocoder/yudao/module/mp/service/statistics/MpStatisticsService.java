package cn.iocoder.yudao.module.mp.service.statistics;

import me.chanjar.weixin.mp.bean.datacube.WxDataCubeInterfaceResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeMsgResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserCumulate;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserSummary;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公众号统计 Service 接口
 *
 * @author 芋道源码
 */
public interface MpStatisticsService {

    /**
     * 获取粉丝增减数据
     *
     * @param accountId 公众号账号编号
     * @param date 时间区间
     * @return 粉丝增减数据
     */
    List<WxDataCubeUserSummary> getUserSummary(Long accountId, LocalDateTime[] date);

    /**
     * 获取粉丝累计数据
     *
     * @param accountId 公众号账号编号
     * @param date 时间区间
     * @return 粉丝累计数据
     */
    List<WxDataCubeUserCumulate> getUserCumulate(Long accountId, LocalDateTime[] date);

    /**
     * 获取消息发送概况数据
     *
     * @param accountId 公众号账号编号
     * @param date 时间区间
     * @return 消息发送概况数据
     */
    List<WxDataCubeMsgResult> getUpstreamMessage(Long accountId, LocalDateTime[] date);

    /**
     * 获取接口分析数据
     *
     * @param accountId 公众号账号编号
     * @param date 时间区间
     * @return 接口分析数据
     */
    List<WxDataCubeInterfaceResult> getInterfaceSummary(Long accountId, LocalDateTime[] date);

}
