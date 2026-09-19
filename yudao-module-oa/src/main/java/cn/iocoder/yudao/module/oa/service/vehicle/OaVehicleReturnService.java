package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.OaVehicleReturnPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.OaVehicleReturnSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleReturnDO;
import jakarta.validation.Valid;

/**
 * 还车申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaVehicleReturnService {

    /**
     * 创建本人还车申请草稿
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 申请编号
     */
    Long createVehicleReturn(@Valid OaVehicleReturnSaveReqVO createReqVO, Long userId);

    /**
     * 更新本人还车申请草稿
     *
     * @param updateReqVO 修改信息
     * @param userId 当前用户编号
     */
    void updateVehicleReturn(@Valid OaVehicleReturnSaveReqVO updateReqVO, Long userId);

    /**
     * 删除本人还车申请草稿
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     */
    void deleteVehicleReturn(Long id, Long userId);

    /**
     * 提交本人还车申请
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 流程实例编号
     */
    String submitVehicleReturn(Long id, Long userId);

    /**
     * 取消本人审批中的还车申请
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     */
    void cancelVehicleReturn(Long id, Long userId);

    /**
     * 回写还车审批结果并更新车辆及用车申请
     *
     * @param id 还车申请编号
     * @param status 审批结果
     */
    void updateVehicleReturnStatus(Long id, Integer status);

    /**
     * 获得还车申请详情
     *
     * @param id 还车申请编号
     * @return 还车申请
     */
    OaVehicleReturnDO getVehicleReturn(Long id);

    /**
     * 获得本人还车申请分页
     *
     * @param userId 当前用户编号
     * @param pageReqVO 分页条件
     * @return 还车申请分页
     */
    PageResult<OaVehicleReturnDO> getVehicleReturnPage(Long userId, OaVehicleReturnPageReqVO pageReqVO);

}
