package cn.iocoder.yudao.module.mp.service.statistics;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserSummary;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.STATISTICS_GET_USER_SUMMARY_FAIL;

/**
 * 公众号统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class MpStatisticsServiceImpl implements MpStatisticsService {

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpServiceFactory mpServiceFactory;

    @Override
    public List<WxDataCubeUserSummary> getUserSummary(Long id, LocalDateTime[] date) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(id);
        try {
            return mpService.getDataCubeService().getUserSummary(
                    DateUtil.date(date[0]), DateUtil.date(date[1]));
        } catch (WxErrorException e) {
            throw exception(STATISTICS_GET_USER_SUMMARY_FAIL, e.getError().getErrorMsg());
        }
    }


}
