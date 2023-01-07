package cn.iocoder.yudao.module.mp.service.statistics;

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
     * 获取用户增减数据
     *
     * @param id 公众号账号编号
     * @param date 时间区间
     * @return 用户增减数据
     */
    List<WxDataCubeUserSummary> getUserSummary(Long id, LocalDateTime[] date);

}
