package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleReturnStatusEnum;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleStatusEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.BpmModelConstants.VEHICLE_APPLY;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 用车申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaVehicleApplyServiceImpl implements OaVehicleApplyService {

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private OaVehicleApplyMapper vehicleApplyMapper;

    @Resource
    private OaVehicleService vehicleService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    public Long createVehicleApply(OaVehicleApplySaveReqVO createReqVO, Long userId) {
        // 1.1 校验用车信息，草稿不占用预约时段
        OaVehicleDO vehicle = validateVehicleApply(createReqVO);
        // 1.2 校验申请人有效
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.3 生成业务单号，并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.VEHICLE_APPLY_NO_PREFIX);
        validateVehicleApplyNoUnique(no);

        // 2. 创建草稿
        OaVehicleApplyDO apply = BeanUtils.toBean(createReqVO, OaVehicleApplyDO.class).setNo(no).setId(null)
                .setVehicleNo(vehicle.getNo()).setUserId(userId).setDeptId(user.getDeptId())
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus()).setReturnStatus(OaVehicleReturnStatusEnum.NOT_EFFECTIVE.getStatus());
        vehicleApplyMapper.insert(apply);
        return apply.getId();
    }

    @Override
    public void updateVehicleApply(OaVehicleApplySaveReqVO updateReqVO, Long userId) {
        // 1.1 查询草稿并校验归属，草稿允许调整车辆
        OaVehicleApplyDO apply = validateVehicleApplyExists(updateReqVO.getId());
        validateVehicleApplyOwner(apply, userId);
        // 1.2 只有未提交申请允许编辑
        validateVehicleApplyDraft(apply);
        // 1.3 校验车辆和用车时间
        OaVehicleDO vehicle = validateVehicleApply(updateReqVO);

        // 2. 更新草稿
        vehicleApplyMapper.updateById(BeanUtils.toBean(updateReqVO, OaVehicleApplyDO.class)
                .setVehicleNo(vehicle.getNo()));
    }

    @Override
    public void deleteVehicleApply(Long id, Long userId) {
        // 1.1 校验申请归属
        OaVehicleApplyDO apply = validateVehicleApplyExists(id);
        validateVehicleApplyOwner(apply, userId);
        // 1.2 已提交记录保留审批历史，不允许删除
        validateVehicleApplyDraft(apply);

        // 2. 删除草稿
        vehicleApplyMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitVehicleApply(Long id, Long userId) {
        // 1.1 查询申请并校验归属
        OaVehicleApplyDO apply = validateVehicleApplyExists(id);
        validateVehicleApplyOwner(apply, userId);
        // 1.2 仅未提交申请可以发起流程
        validateVehicleApplyDraft(apply);
        OaVehicleDO vehicle = vehicleService.validateVehicleExists(apply.getVehicleId());
        // 1.3 提交时校验车辆空闲状态
        if (ObjUtil.notEqual(vehicle.getStatus(), OaVehicleStatusEnum.IDLE.getStatus())) {
            throw exception(VEHICLE_NOT_ENABLED);
        }
        // 1.4 检查同一车辆时段冲突，审批中也占用预约
        Long count = vehicleApplyMapper.selectCountByVehicleIdAndTimeOverlap(vehicle.getId(), id,
                apply.getStartTime(), apply.getEndTime());
        if (count > 0) {
            throw exception(VEHICLE_APPLY_TIME_CONFLICT);
        }

        // 2. 保存提交时的车辆快照并占用预约时段
        vehicleApplyMapper.updateById(new OaVehicleApplyDO().setId(id).setVehicleNo(vehicle.getNo())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(VEHICLE_APPLY)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        vehicleApplyMapper.updateById(new OaVehicleApplyDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelVehicleApply(Long id, Long userId) {
        // 1.1 校验本人申请
        OaVehicleApplyDO apply = validateVehicleApplyExists(id);
        validateVehicleApplyOwner(apply, userId);
        // 1.2 仅审批中允许取消
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(VEHICLE_APPLY_STATUS_INVALID);
        }

        // 2. 取消流程，事件回写后释放预约
        processInstanceApi.cancelProcessInstanceByStartUser(userId, apply.getProcessInstanceId(), "申请人取消用车申请");
    }

    @Override
    public void updateVehicleApplyReturnStatus(Long id, Integer expectedReturnStatus, Integer returnStatus) {
        // 1. 校验原申请已通过且还车状态未发生变化
        OaVehicleApplyDO apply = validateVehicleApplyExists(id);
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                || ObjUtil.notEqual(apply.getReturnStatus(), expectedReturnStatus)) {
            throw exception(VEHICLE_RETURN_STATUS_INVALID);
        }

        // 2. 按原状态回写，防止多个还车草稿同时提交成功
        int updateCount = vehicleApplyMapper.updateReturnStatusByIdAndReturnStatus(id, expectedReturnStatus, returnStatus);
        if (updateCount == 0) {
            throw exception(VEHICLE_RETURN_STATUS_INVALID);
        }
    }

    @Override
    public void updateVehicleApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在，已处理的审批结果不再重复更新业务状态
        OaVehicleApplyDO apply = validateVehicleApplyExists(id);
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2. 通过后等待还车；拒绝、取消后不再占用预约时段
        vehicleApplyMapper.updateById(new OaVehicleApplyDO().setId(id).setStatus(status)
                .setReturnStatus(ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                        ? OaVehicleReturnStatusEnum.PENDING_RETURN.getStatus() : OaVehicleReturnStatusEnum.NOT_EFFECTIVE.getStatus()));
    }

    @Override
    public OaVehicleApplyDO getVehicleApply(Long id) {
        return vehicleApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaVehicleApplyDO> getVehicleApplyPage(Long userId, OaVehicleApplyPageReqVO pageReqVO) {
        return vehicleApplyMapper.selectPage(userId, pageReqVO);
    }

    @Override
    public List<OaVehicleApplyDO> getVehicleApplyList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return vehicleApplyMapper.selectByIds(ids);
    }

    @Override
    public OaVehicleApplyDO validateVehicleApplyExists(Long id) {
        OaVehicleApplyDO apply = vehicleApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(VEHICLE_APPLY_NOT_EXISTS);
        }
        return apply;
    }

    /**
     * 校验车辆和预约时间
     *
     * @param reqVO 申请信息
     * @return 车辆
     */
    private OaVehicleDO validateVehicleApply(OaVehicleApplySaveReqVO reqVO) {
        // 1. 校验车辆存在，空闲状态仅在提交时校验
        OaVehicleDO vehicle = vehicleService.validateVehicleExists(reqVO.getVehicleId());
        // 2. 用车时间采用左闭右开区间，结束必须晚于开始
        if (!reqVO.getStartTime().isBefore(reqVO.getEndTime())) {
            throw exception(VEHICLE_APPLY_TIME_INVALID);
        }
        return vehicle;
    }

    /**
     * 校验申请存在且属于当前用户
     *
     * @param apply 用车申请
     * @param userId 当前用户编号
     */
    private void validateVehicleApplyOwner(OaVehicleApplyDO apply, Long userId) {
        if (ObjUtil.notEqual(apply.getUserId(), userId)) {
            throw exception(VEHICLE_APPLY_NOT_OWNER);
        }
    }

    /**
     * 校验申请处于草稿状态
     *
     * @param apply 用车申请
     */
    private void validateVehicleApplyDraft(OaVehicleApplyDO apply) {
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(VEHICLE_APPLY_STATUS_INVALID);
        }
    }

    /**
     * 校验申请单号唯一
     *
     * @param no 申请单号
     */
    private void validateVehicleApplyNoUnique(String no) {
        OaVehicleApplyDO apply = vehicleApplyMapper.selectByNo(no);
        if (apply != null) {
            throw exception(VEHICLE_APPLY_NO_DUPLICATE);
        }
    }

    @Override
    public Long getVehicleApplyCountByVehicleId(Long id) {
        return vehicleApplyMapper.selectCountByVehicleId(id);
    }

}
