package cn.iocoder.yudao.module.report.service.statisticsdata;

import cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo.EkycChannelStatusSummaryRespVO;

import java.time.LocalDate;

/**
 * 通用统计数据 Service 接口
 *
 * @author 芋道源码
 */
public interface EkycFaceCheckService {
    EkycChannelStatusSummaryRespVO getChannelStatusSummary(LocalDate startDate, LocalDate endDate);

}
