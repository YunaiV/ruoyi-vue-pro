package cn.iocoder.yudao.module.tms.service.vessel.tracking;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.log.TmsVesselTrackingLogDO;
import cn.iocoder.yudao.module.tms.dal.mysql.vessel.tracking.TmsVesselTrackingMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.vessel.tracking.log.TmsVesselTrackingLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.VESSEL_TRACKING_LOG_NOT_EXISTS;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.VESSEL_TRACKING_NOT_EXISTS;

/**
 * 出运跟踪信息表（由外部API更新，船） Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsVesselTrackingServiceImpl implements TmsVesselTrackingService {

    @Resource
    private TmsVesselTrackingMapper vesselTrackingMapper;
    @Resource
    private TmsVesselTrackingLogMapper vesselTrackingLogMapper;

    @Override
    public Long createVesselTracking(@Validated TmsVesselTrackingSaveReqVO createReqVO) {
        // 插入
        TmsVesselTrackingDO vesselTracking = BeanUtils.toBean(createReqVO, TmsVesselTrackingDO.class);
        vesselTrackingMapper.insert(vesselTracking);
        // 返回
        return vesselTracking.getId();
    }

    @Override
    public void updateVesselTracking(TmsVesselTrackingSaveReqVO updateReqVO) {
        // 校验存在
        validateVesselTrackingExists(updateReqVO.getId());
        // 更新
        TmsVesselTrackingDO updateObj = BeanUtils.toBean(updateReqVO, TmsVesselTrackingDO.class);
        vesselTrackingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVesselTracking(Long id) {
        // 校验存在
        validateVesselTrackingExists(id);
        // 删除
        vesselTrackingMapper.deleteById(id);

        // 删除子表
        deleteVesselTrackingLogByTrackingId(id);
    }

    @Override
    public void deleteVesselTracking(Long upstreamId, Integer billType) {
        vesselTrackingMapper.deleteByUpstreamIdAndUpstreamType(upstreamId, billType);
    }

    private void validateVesselTrackingExists(Long id) {
        if (vesselTrackingMapper.selectById(id) == null) {
            throw exception(VESSEL_TRACKING_NOT_EXISTS);
        }
    }

    @Override
    public TmsVesselTrackingDO getVesselTracking(Long id) {
        return vesselTrackingMapper.selectById(id);
    }

    /**
     * 根据上游ID+上游单据类型获得 出运跟踪信息
     *
     * @param upstreamId   上游ID
     *                     upstreamType 上游单据类型
     * @param upstreamType 上游单据类型
     */
    @Override
    public TmsVesselTrackingDO getVesselTrackingByUpstreamIdAndUpstreamType(Long upstreamId, Integer upstreamType) {
        return vesselTrackingMapper.getVesselTrackingByUpstreamIdAndUpstreamType(upstreamId, upstreamType);
    }

    @Override
    public PageResult<TmsVesselTrackingDO> getVesselTrackingPage(TmsVesselTrackingPageReqVO pageReqVO) {
        return vesselTrackingMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（出运轨迹日志表（记录多次事件节点）） ====================

    @Override
    public PageResult<TmsVesselTrackingLogDO> getVesselTrackingLogPage(PageParam pageReqVO, Long trackingId) {
        return vesselTrackingLogMapper.selectPage(pageReqVO, trackingId);
    }

    @Override
    public Long createVesselTrackingLog(TmsVesselTrackingLogDO vesselTrackingLog) {
        vesselTrackingLogMapper.insert(vesselTrackingLog);
        return vesselTrackingLog.getId();
    }

    @Override
    public void updateVesselTrackingLog(TmsVesselTrackingLogDO vesselTrackingLog) {
        // 校验存在
        validateVesselTrackingLogExists(vesselTrackingLog.getId());
        // 更新
        vesselTrackingLog.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        vesselTrackingLogMapper.updateById(vesselTrackingLog);
    }

    @Override
    public void deleteVesselTrackingLog(Long id) {
        // 校验存在
        validateVesselTrackingLogExists(id);
        // 删除
        vesselTrackingLogMapper.deleteById(id);
    }

    @Override
    public TmsVesselTrackingLogDO getVesselTrackingLog(Long id) {
        return vesselTrackingLogMapper.selectById(id);
    }

    private void validateVesselTrackingLogExists(Long id) {
        if (vesselTrackingLogMapper.selectById(id) == null) {
            throw exception(VESSEL_TRACKING_LOG_NOT_EXISTS);
        }
    }

    private void deleteVesselTrackingLogByTrackingId(Long trackingId) {
        vesselTrackingLogMapper.deleteByTrackingId(trackingId);
    }

}