package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 用车申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaVehicleApplyService {

    /**
     * 创建用车申请草稿
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 申请编号
     */
    Long createVehicleApply(@Valid OaVehicleApplySaveReqVO createReqVO, Long userId);

    /**
     * 更新本人用车申请草稿
     *
     * @param updateReqVO 修改信息
     * @param userId 当前用户编号
     */
    void updateVehicleApply(@Valid OaVehicleApplySaveReqVO updateReqVO, Long userId);

    /**
     * 删除本人用车申请草稿
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     */
    void deleteVehicleApply(Long id, Long userId);

    /**
     * 提交用车申请，并占用预约时段
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 流程实例编号
     */
    String submitVehicleApply(Long id, Long userId);

    /**
     * 撤销本人审批中的用车申请
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     */
    void cancelVehicleApply(Long id, Long userId);

    /**
     * 更新已批准用车申请的还车状态
     *
     * @param id 申请编号
     * @param expectedReturnStatus 原还车状态
     * @param returnStatus 新还车状态
     */
    void updateVehicleApplyReturnStatus(Long id, Integer expectedReturnStatus, Integer returnStatus);

    /**
     * 回写用车申请审批结果
     *
     * @param id 申请编号
     * @param status BPM 审批结果
     */
    void updateVehicleApplyStatus(Long id, Integer status);

    /**
     * 获得用车申请详情
     *
     * @param id 申请编号
     * @return 用车申请
     */
    OaVehicleApplyDO getVehicleApply(Long id);

    /**
     * 获得本人用车申请分页
     *
     * @param userId 当前用户编号
     * @param pageReqVO 分页条件
     * @return 用车申请分页
     */
    PageResult<OaVehicleApplyDO> getVehicleApplyPage(Long userId, OaVehicleApplyPageReqVO pageReqVO);

    /**
     * 获得指定编号的用车申请列表
     *
     * @param ids 申请编号集合
     * @return 用车申请列表
     */
    List<OaVehicleApplyDO> getVehicleApplyList(Collection<Long> ids);

    /**
     * 获得用车申请 Map
     *
     * @param ids 申请编号集合
     * @return 用车申请 Map
     */
    default Map<Long, OaVehicleApplyDO> getVehicleApplyMap(Collection<Long> ids) {
        List<OaVehicleApplyDO> applies = getVehicleApplyList(ids);
        return convertMap(applies, OaVehicleApplyDO::getId);
    }

    /**
     * 查询并校验用车申请存在
     *
     * @param id 申请编号
     * @return 用车申请
     */
    OaVehicleApplyDO validateVehicleApplyExists(Long id);

    /**
     * 获得引用车辆的单据数量
     *
     * @param id 车辆编号
     * @return 单据数量
     */
    Long getVehicleApplyCountByVehicleId(Long id);

}
