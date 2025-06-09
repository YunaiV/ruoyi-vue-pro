package cn.iocoder.yudao.module.tms.dal.mysql.vessel.tracking.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.log.TmsVesselTrackingLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出运轨迹日志表（记录多次事件节点） Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsVesselTrackingLogMapper extends BaseMapperX<TmsVesselTrackingLogDO> {

    default PageResult<TmsVesselTrackingLogDO> selectPage(PageParam reqVO, Long trackingId) {
        return selectPage(reqVO,
            new LambdaQueryWrapperX<TmsVesselTrackingLogDO>().eq(TmsVesselTrackingLogDO::getTrackingId, trackingId).orderByDesc(TmsVesselTrackingLogDO::getId));
    }

    default int deleteByTrackingId(Long trackingId) {
        return delete(TmsVesselTrackingLogDO::getTrackingId, trackingId);
    }

}
