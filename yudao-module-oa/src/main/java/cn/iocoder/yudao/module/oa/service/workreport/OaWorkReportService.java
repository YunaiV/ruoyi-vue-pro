package cn.iocoder.yudao.module.oa.service.workreport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO;

import javax.validation.Valid;

/**
 * OA 工作汇报 Service 接口
 *
 * @author 芋道源码
 */
public interface OaWorkReportService {

    /**
     * 创建工作汇报草稿
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 汇报编号
     */
    Long createWorkReport(@Valid OaWorkReportSaveReqVO createReqVO, Long userId);

    /**
     * 更新工作汇报草稿
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateWorkReport(@Valid OaWorkReportSaveReqVO updateReqVO, Long userId);

    /**
     * 删除工作汇报草稿
     *
     * @param id 汇报编号
     * @param userId 用户编号
     */
    void deleteWorkReport(Long id, Long userId);

    /**
     * 提交工作汇报
     *
     * @param id 汇报编号
     * @param userId 用户编号
     */
    void submitWorkReport(Long id, Long userId);

    /**
     * 取消提交工作汇报
     *
     * @param id 汇报编号
     * @param userId 用户编号
     */
    void cancelWorkReport(Long id, Long userId);

    /**
     * 获得工作汇报
     *
     * @param id 汇报编号
     * @param userId 当前用户编号
     * @return 工作汇报
     */
    OaWorkReportDO getWorkReport(Long id, Long userId);

    /**
     * 获得我的工作汇报分页
     *
     * @param pageReqVO 分页条件
     * @param userId 用户编号
     * @return 工作汇报分页
     */
    PageResult<OaWorkReportDO> getWorkReportPage(OaWorkReportPageReqVO pageReqVO, Long userId);

    /**
     * 获得工作汇报统计
     *
     * @param reqVO 统计条件
     * @param userId 当前用户编号
     * @return 工作汇报统计
     */
    OaWorkReportStatisticsRespVO getWorkReportStatistics(@Valid OaWorkReportStatisticsReqVO reqVO, Long userId);

}
