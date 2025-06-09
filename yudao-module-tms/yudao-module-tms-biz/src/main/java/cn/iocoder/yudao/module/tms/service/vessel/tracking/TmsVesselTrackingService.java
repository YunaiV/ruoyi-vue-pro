package cn.iocoder.yudao.module.tms.service.vessel.tracking;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.log.TmsVesselTrackingLogDO;
import jakarta.validation.Valid;

/**
 * 出运跟踪信息表（由外部API更新，船） Service 接口
 *
 * @author wdy
 */
public interface TmsVesselTrackingService {

    /**
     * 创建出运跟踪信息表（由外部API更新）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVesselTracking(@Valid TmsVesselTrackingSaveReqVO createReqVO);

    /**
     * 更新出运跟踪信息表（由外部API更新）
     *
     * @param updateReqVO 更新信息
     */
    void updateVesselTracking(@Valid TmsVesselTrackingSaveReqVO updateReqVO);

    /**
     * 删除出运跟踪信息表（由外部API更新）
     *
     * @param id 编号
     */
    void deleteVesselTracking(Long id);

    void deleteVesselTracking(Long upstreamId, Integer billType);
    /**
     * 获得出运跟踪信息表（由外部API更新）
     *
     * @param id 编号
     * @return 出运跟踪信息表（由外部API更新）
     */
    TmsVesselTrackingDO getVesselTracking(Long id);

    /**
     * 根据上游ID+上游单据类型获得 出运跟踪信息
     *
     * @param upstreamId   上游ID
     * @param upstreamType 上游单据类型
     */
    TmsVesselTrackingDO getVesselTrackingByUpstreamIdAndUpstreamType(Long upstreamId, Integer upstreamType);

    /**
     * 获得出运跟踪信息表（由外部API更新）分页
     *
     * @param pageReqVO 分页查询
     * @return 出运跟踪信息表（由外部API更新）分页
     */
    PageResult<TmsVesselTrackingDO> getVesselTrackingPage(TmsVesselTrackingPageReqVO pageReqVO);

    // ==================== 子表（出运轨迹日志表（记录多次事件节点）） ====================

    /**
     * 获得出运轨迹日志表（记录多次事件节点）分页
     *
     * @param pageReqVO  分页查询
     * @param trackingId 关联跟踪主表ID
     * @return 出运轨迹日志表（记录多次事件节点）分页
     */
    PageResult<TmsVesselTrackingLogDO> getVesselTrackingLogPage(PageParam pageReqVO, Long trackingId);

    /**
     * 创建出运轨迹日志表（记录多次事件节点）
     *
     * @param vesselTrackingLog 创建信息
     * @return 编号
     */
    Long createVesselTrackingLog(@Valid TmsVesselTrackingLogDO vesselTrackingLog);

    /**
     * 更新出运轨迹日志表（记录多次事件节点）
     *
     * @param vesselTrackingLog 更新信息
     */
    void updateVesselTrackingLog(@Valid TmsVesselTrackingLogDO vesselTrackingLog);

    /**
     * 删除出运轨迹日志表（记录多次事件节点）
     *
     * @param id 编号
     */
    void deleteVesselTrackingLog(Long id);

    /**
     * 获得出运轨迹日志表（记录多次事件节点）
     *
     * @param id 编号
     * @return 出运轨迹日志表（记录多次事件节点）
     */
    TmsVesselTrackingLogDO getVesselTrackingLog(Long id);

}