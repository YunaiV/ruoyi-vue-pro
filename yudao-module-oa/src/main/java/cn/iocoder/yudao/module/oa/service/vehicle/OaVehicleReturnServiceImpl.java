package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.OaVehicleReturnPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.OaVehicleReturnSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleReturnDO;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleReturnMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleReturnStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.BpmModelConstants.VEHICLE_RETURN;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 还车申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaVehicleReturnServiceImpl implements OaVehicleReturnService {

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private OaVehicleReturnMapper vehicleReturnMapper;

    @Resource
    private OaVehicleApplyService vehicleApplyService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    public Long createVehicleReturn(OaVehicleReturnSaveReqVO createReqVO, Long userId) {
        // 1.1 查询关联用车申请
        OaVehicleApplyDO apply = vehicleApplyService.validateVehicleApplyExists(createReqVO.getApplyId());
        // 1.2 校验申请归属及待还车状态
        validateVehicleApplyReturnable(apply, userId);
        // 1.3 校验实际回车时间，同一用车申请允许保存多个草稿
        validateVehicleReturn(createReqVO);
        // 1.4 生成业务单号，并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX);
        validateVehicleReturnNoUnique(no);

        // 2. 创建草稿
        OaVehicleReturnDO vehicleReturn = BeanUtils.toBean(createReqVO, OaVehicleReturnDO.class).setNo(no)
                .setVehicleId(apply.getVehicleId()).setUserId(userId).setDeptId(apply.getDeptId())
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus());
        vehicleReturnMapper.insert(vehicleReturn);
        return vehicleReturn.getId();
    }

    @Override
    public void updateVehicleReturn(OaVehicleReturnSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验还车申请存在
        OaVehicleReturnDO vehicleReturn = validateVehicleReturnExists(updateReqVO.getId());
        // 1.2 校验本人归属
        validateVehicleReturnOwner(vehicleReturn, userId);
        // 1.3 只能编辑草稿
        validateVehicleReturnDraft(vehicleReturn);
        // 1.4 校验本次选择的用车申请存在
        OaVehicleApplyDO apply = vehicleApplyService.validateVehicleApplyExists(updateReqVO.getApplyId());
        // 1.5 校验申请归属及待还车状态
        validateVehicleApplyReturnable(apply, userId);
        // 1.6 校验实际回车时间
        validateVehicleReturn(updateReqVO);

        // 2. 保存草稿
        vehicleReturnMapper.updateById(BeanUtils.toBean(updateReqVO, OaVehicleReturnDO.class)
                .setVehicleId(apply.getVehicleId()).setUserId(userId).setDeptId(apply.getDeptId()));
    }

    @Override
    public void deleteVehicleReturn(Long id, Long userId) {
        // 1.1 校验还车申请存在
        OaVehicleReturnDO vehicleReturn = validateVehicleReturnExists(id);
        // 1.2 校验本人归属
        validateVehicleReturnOwner(vehicleReturn, userId);
        // 1.3 只允许删除草稿，已提交记录保留审批历史
        validateVehicleReturnDraft(vehicleReturn);

        // 2. 删除草稿，原用车单仍然待还车
        vehicleReturnMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitVehicleReturn(Long id, Long userId) {
        // 1.1 校验还车申请存在
        OaVehicleReturnDO vehicleReturn = validateVehicleReturnExists(id);
        // 1.2 校验本人归属
        validateVehicleReturnOwner(vehicleReturn, userId);
        // 1.3 仅未提交申请允许发起
        validateVehicleReturnDraft(vehicleReturn);
        // 1.4 校验原用车申请存在
        OaVehicleApplyDO apply = vehicleApplyService.validateVehicleApplyExists(vehicleReturn.getApplyId());
        // 1.5 校验原申请归属及待还车状态
        validateVehicleApplyReturnable(apply, userId);
        // 1.6 重新校验实际回车时间
        OaVehicleReturnSaveReqVO reqVO = BeanUtils.toBean(vehicleReturn, OaVehicleReturnSaveReqVO.class);
        validateVehicleReturn(reqVO);

        // 2.1 原用车申请切换为还车中，仍然占用车辆
        vehicleApplyService.updateVehicleApplyReturnStatus(apply.getId(), OaVehicleReturnStatusEnum.PENDING_RETURN.getStatus(), OaVehicleReturnStatusEnum.RETURNING.getStatus());
        // 2.2 更新还车申请为审批中
        vehicleReturnMapper.updateById(new OaVehicleReturnDO().setId(id).setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(VEHICLE_RETURN)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        vehicleReturnMapper.updateById(new OaVehicleReturnDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelVehicleReturn(Long id, Long userId) {
        // 1.1 校验还车申请存在
        OaVehicleReturnDO vehicleReturn = validateVehicleReturnExists(id);
        // 1.2 校验本人归属
        validateVehicleReturnOwner(vehicleReturn, userId);
        // 1.3 仅允许取消审批中的还车申请
        if (ObjUtil.notEqual(vehicleReturn.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(VEHICLE_RETURN_STATUS_INVALID);
        }

        // 2. 取消流程，结果事件将原用车单恢复为待还车
        processInstanceApi.cancelProcessInstanceByStartUser(userId, vehicleReturn.getProcessInstanceId(),
                "申请人取消还车申请");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVehicleReturnStatus(Long id, Integer status) {
        // 1. 校验申请存在，已处理的审批结果不再重复更新业务状态
        OaVehicleReturnDO vehicleReturn = validateVehicleReturnExists(id);
        if (ObjUtil.notEqual(vehicleReturn.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2.1 还车通过后结束占用；拒绝或取消则恢复为待还车，允许再次发起
        vehicleApplyService.updateVehicleApplyReturnStatus(vehicleReturn.getApplyId(), OaVehicleReturnStatusEnum.RETURNING.getStatus(),
                ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus()) ? OaVehicleReturnStatusEnum.RETURNED.getStatus() : OaVehicleReturnStatusEnum.PENDING_RETURN.getStatus());
        // 2.2 保存审批结果，与原用车单更新共同提交或回滚
        vehicleReturnMapper.updateById(new OaVehicleReturnDO().setId(id).setStatus(status));
    }

    @Override
    public OaVehicleReturnDO getVehicleReturn(Long id) {
        return vehicleReturnMapper.selectById(id);
    }

    @Override
    public PageResult<OaVehicleReturnDO> getVehicleReturnPage(Long userId, OaVehicleReturnPageReqVO pageReqVO) {
        return vehicleReturnMapper.selectPage(userId, pageReqVO);
    }

    /**
     * 校验还车申请单号唯一
     *
     * @param no 还车申请单号
     */
    private void validateVehicleReturnNoUnique(String no) {
        OaVehicleReturnDO vehicleReturn = vehicleReturnMapper.selectByNo(no);
        if (vehicleReturn != null) {
            throw exception(VEHICLE_RETURN_NO_DUPLICATE);
        }
    }

    /**
     * 校验还车申请存在
     *
     * @param id 还车申请编号
     * @return 还车申请
     */
    private OaVehicleReturnDO validateVehicleReturnExists(Long id) {
        OaVehicleReturnDO vehicleReturn = vehicleReturnMapper.selectById(id);
        if (vehicleReturn == null) {
            throw exception(VEHICLE_RETURN_NOT_EXISTS);
        }
        return vehicleReturn;
    }

    /**
     * 校验还车申请属于当前用户
     *
     * @param vehicleReturn 还车申请
     * @param userId 当前用户编号
     */
    private void validateVehicleReturnOwner(OaVehicleReturnDO vehicleReturn, Long userId) {
        if (ObjUtil.notEqual(vehicleReturn.getUserId(), userId)) {
            throw exception(VEHICLE_RETURN_NOT_OWNER);
        }
    }

    /**
     * 校验还车申请为草稿
     *
     * @param vehicleReturn 还车申请
     */
    private void validateVehicleReturnDraft(OaVehicleReturnDO vehicleReturn) {
        if (ObjUtil.notEqual(vehicleReturn.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(VEHICLE_RETURN_STATUS_INVALID);
        }
    }

    /**
     * 校验原用车申请允许本人还车
     *
     * @param apply 用车申请
     * @param userId 当前用户编号
     */
    private void validateVehicleApplyReturnable(OaVehicleApplyDO apply, Long userId) {
        if (ObjUtil.notEqual(apply.getUserId(), userId)) {
            throw exception(VEHICLE_RETURN_NOT_OWNER);
        }
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                || ObjUtil.notEqual(apply.getReturnStatus(), OaVehicleReturnStatusEnum.PENDING_RETURN.getStatus())) {
            throw exception(VEHICLE_RETURN_STATUS_INVALID);
        }
    }

    /**
     * 校验实际回车时间
     *
     * @param reqVO 还车信息
     */
    private void validateVehicleReturn(OaVehicleReturnSaveReqVO reqVO) {
        if (reqVO.getActualStartTime() == null
                || reqVO.getActualReturnTime().isBefore(reqVO.getActualStartTime())
                || reqVO.getActualReturnTime().isAfter(LocalDateTime.now())) {
            throw exception(VEHICLE_RETURN_TIME_INVALID);
        }
    }

}
