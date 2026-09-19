package cn.iocoder.yudao.module.oa.service.seal;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.*;
import cn.iocoder.yudao.module.oa.dal.mysql.seal.OaSealApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.enums.seal.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 用印申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaSealApplyServiceImpl implements OaSealApplyService {

    @Resource
    private OaSealApplyMapper sealApplyMapper;
    @Resource
    @Lazy
    private OaSealService sealService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSealApply(OaSealApplySaveReqVO reqVO, Long userId) {
        // 1.1 校验印章和申请内容，草稿不占用预约时段
        OaSealDO seal = sealService.validateSealExists(reqVO.getSealId());
        // 1.2 申请人与所属部门取自平台账号，不接受客户端指定
        adminUserApi.validateUser(userId);
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        // 1.3 生成申请单号，并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX);
        if (sealApplyMapper.selectByNo(no) != null) {
            throw exception(SEAL_APPLY_NO_DUPLICATE);
        }

        // 2. 保存草稿及印章快照，审批状态与业务状态分别维护
        OaSealApplyDO apply = BeanUtils.toBean(reqVO, OaSealApplyDO.class).setId(null)
                .setNo(no).setUserId(userId).setDeptId(user.getDeptId())
                .setSealNo(seal.getNo()).setSealName(seal.getName()).setKeeperUserId(seal.getKeeperUserId())
                .setSealType(seal.getType()).setKeeperDeptId(seal.getKeeperDeptId())
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus()).setUseStatus(OaSealUseStatusEnum.PENDING.getStatus());
        sealApplyMapper.insert(apply);
        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSealApply(OaSealApplySaveReqVO reqVO, Long userId) {
        // 1.1 校验申请存在及归属
        OaSealApplyDO apply = validateSealApplyExists(reqVO.getId());
        validateOwner(apply, userId);
        // 1.2 仅未提交的草稿允许修改
        validateDraft(apply);
        // 1.3 重新校验申请内容及印章
        OaSealDO seal = sealService.validateSealExists(reqVO.getSealId());

        // 2. 更新白名单字段，申请人、流程及用印状态不允许通用更新
        OaSealApplyDO update = BeanUtils.toBean(reqVO, OaSealApplyDO.class)
                .setSealNo(seal.getNo()).setSealName(seal.getName()).setKeeperUserId(seal.getKeeperUserId())
                .setSealType(seal.getType()).setKeeperDeptId(seal.getKeeperDeptId());
        if (ObjUtil.notEqual(update.getType(), OaSealApplyTypeEnum.CONTRACT.getType())) {
            update.setContractPrice(null).setContractParty(null);
        }
        if (ObjUtil.notEqual(update.getMode(), OaSealUseModeEnum.BORROW.getMode())) {
            update.setExpectedReturnTime(null);
        }
        sealApplyMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSealApply(Long id, Long userId) {
        // 1.1 校验申请归属
        OaSealApplyDO apply = validateSealApplyExists(id);
        validateOwner(apply, userId);
        // 1.2 已提交申请保留审批记录
        validateDraft(apply);

        // 2. 删除草稿
        sealApplyMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitSealApply(Long id, Long userId) {
        // 1.1 查询申请并校验归属
        OaSealApplyDO apply = validateSealApplyExists(id);
        validateOwner(apply, userId);
        // 1.2 校验草稿状态
        validateDraft(apply);
        // 1.3 校验关联印章存在
        OaSealDO seal = sealService.validateSealExists(apply.getSealId());
        // 1.4 校验借用时段冲突
        validateTimeConflict(apply);

        // 2. 更新审批状态，借用申请在审批中参与时段冲突检查
        sealApplyMapper.updateById(new OaSealApplyDO().setId(id).setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus())
                .setSealNo(seal.getNo()).setSealName(seal.getName()).setKeeperUserId(seal.getKeeperUserId())
                .setSealType(seal.getType()).setKeeperDeptId(seal.getKeeperDeptId()));

        // 3.1 发起审批
        String processId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.SEAL_APPLY)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        sealApplyMapper.updateById(new OaSealApplyDO().setId(id).setProcessInstanceId(processId));
        return processId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSealApply(Long id, Long userId) {
        // 1.1 查询申请，台账删除不影响历史申请撤销
        OaSealApplyDO apply = validateSealApplyExists(id);
        // 1.2 校验本人申请及审批中状态
        validateOwner(apply, userId);
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(SEAL_APPLY_STATUS_INVALID);
        }

        // 2. 撤销流程，由审批事件释放预约
        processInstanceApi.cancelProcessInstanceByStartUser(userId, apply.getProcessInstanceId(), "申请人撤销用印申请");
    }

    /**
     * 接收 BPM 内部审批结果，仅校验申请存在并更新状态
     *
     * @param id 申请编号
     * @param status 审批结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSealApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在
        validateSealApplyExists(id);

        // 2. 更新审批状态，通过仅表示允许用印
        sealApplyMapper.updateById(new OaSealApplyDO().setId(id).setStatus(status));
    }

    @Override
    public OaSealApplyDO getSealApply(Long id) {
        return sealApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaSealApplyDO> getSealApplyPage(Long userId, OaSealApplyPageReqVO reqVO) {
        return sealApplyMapper.selectPage(userId, reqVO);
    }

    /**
     * 校验借用时段冲突，时间边界使用闭区间
     *
     * @param apply 待提交的申请
     */
    private void validateTimeConflict(OaSealApplyDO apply) {
        // 1. 现场用印及未填写完整借用时间的申请不检查时段冲突
        if (ObjUtil.notEqual(apply.getMode(), OaSealUseModeEnum.BORROW.getMode())
                || apply.getExpectedUseTime() == null || apply.getExpectedReturnTime() == null) {
            return;
        }
        if (apply.getExpectedUseTime().isAfter(apply.getExpectedReturnTime())) {
            throw exception(SEAL_APPLY_TIME_INVALID);
        }
        // 2. 审批中或审批通过的外借申请占用预计时段，不依赖用印登记状态
        if (CollUtil.isNotEmpty(sealApplyMapper.selectListBySealIdAndBorrowTimeConflict(
                apply.getSealId(), apply.getId(), apply.getExpectedUseTime(), apply.getExpectedReturnTime()))) {
            throw exception(SEAL_APPLY_TIME_CONFLICT);
        }
    }

    /**
     * 校验申请存在
     *
     * @param id 申请编号
     * @return 用印申请
     */
    private OaSealApplyDO validateSealApplyExists(Long id) {
        OaSealApplyDO apply = sealApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(SEAL_APPLY_NOT_EXISTS);
        }
        return apply;
    }

    /**
     * 校验申请归属
     *
     * @param apply 用印申请
     * @param userId 申请人
     */
    private void validateOwner(OaSealApplyDO apply, Long userId) {
        if (ObjUtil.notEqual(apply.getUserId(), userId)) {
            throw exception(SEAL_APPLY_NOT_OWNER);
        }
    }

    /**
     * 校验申请未提交
     *
     * @param apply 用印申请
     */
    private void validateDraft(OaSealApplyDO apply) {
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(SEAL_APPLY_STATUS_INVALID);
        }
    }

    @Override
    public Long getSealApplyCountBySealId(Long id) {
        return sealApplyMapper.selectCountBySealId(id);
    }

}
