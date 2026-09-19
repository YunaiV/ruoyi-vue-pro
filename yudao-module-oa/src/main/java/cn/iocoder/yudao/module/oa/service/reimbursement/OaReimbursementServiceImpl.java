package cn.iocoder.yudao.module.oa.service.reimbursement;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement.OaReimbursementDO;
import cn.iocoder.yudao.module.oa.dal.mysql.reimbursement.OaReimbursementMapper;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 费用报销 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaReimbursementServiceImpl implements OaReimbursementService {

    @Resource
    private OaReimbursementMapper reimbursementMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReimbursement(OaReimbursementSaveReqVO saveReqVO) {
        // 1. 校验证明人存在
        adminUserApi.validateUser(saveReqVO.getWitnessUserId());

        // 2. 转换申请内容、汇总金额和票据数，保存草稿
        OaReimbursementDO reimbursement = buildReimbursement(saveReqVO)
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus());
        reimbursementMapper.insert(reimbursement);
        return reimbursement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReimbursement(OaReimbursementSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验申请可修改
        validateReimbursementEditable(saveReqVO.getId(), userId);
        // 1.2 校验证明人存在
        adminUserApi.validateUser(saveReqVO.getWitnessUserId());

        // 2. 更新费用报销
        OaReimbursementDO reimbursement = buildReimbursement(saveReqVO);
        reimbursementMapper.updateById(reimbursement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReimbursement(OaReimbursementSubmitReqVO submitReqVO, Long userId) {
        // 1. 校验申请可提交
        OaReimbursementDO reimbursement = validateReimbursementEditable(submitReqVO.getId(), userId);

        // 2. 更新为审批中
        reimbursementMapper.updateById(new OaReimbursementDO().setId(reimbursement.getId())
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("totalPrice", reimbursement.getTotalPrice());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.REIMBURSEMENT)
                        .setBusinessKey(reimbursement.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees()));
        // 3.2 绑定流程编号
        reimbursementMapper.updateById(new OaReimbursementDO().setId(reimbursement.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    public OaReimbursementDO getReimbursement(Long id) {
        return reimbursementMapper.selectById(id);
    }

    @Override
    public PageResult<OaReimbursementDO> getReimbursementPage(Long userId, OaReimbursementPageReqVO pageReqVO) {
        return reimbursementMapper.selectPage(userId, pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReimbursementStatus(Long id, Integer status) {
        // 1. 校验申请存在
        validateReimbursementExists(id);

        // 2. 更新审批状态
        reimbursementMapper.updateById(new OaReimbursementDO().setId(id).setStatus(status));
    }

    /**
     * 构造申请及汇总字段
     *
     * @param saveReqVO 申请内容
     * @return 申请及计算后的汇总字段
     */
    private OaReimbursementDO buildReimbursement(OaReimbursementSaveReqVO saveReqVO) {
        // 1. 转换申请及报销明细
        OaReimbursementDO reimbursement = BeanUtils.toBean(saveReqVO, OaReimbursementDO.class);
        reimbursement.setItems(BeanUtils.toBean(saveReqVO.getItems(), OaReimbursementDO.Item.class));
        // 2. 汇总金额和票据数
        reimbursement.setTotalPrice(getSumValue(reimbursement.getItems(), OaReimbursementDO.Item::getPrice, BigDecimal::add, BigDecimal.ZERO))
                .setInvoiceCount(getSumValue(reimbursement.getItems(), OaReimbursementDO.Item::getInvoiceCount, Math::addExact, 0));
        return reimbursement;
    }

    /**
     * 校验申请可修改或提交
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 本人的未提交申请
     */
    private OaReimbursementDO validateReimbursementEditable(Long id, Long userId) {
        // 1. 查询申请
        OaReimbursementDO reimbursement = reimbursementMapper.selectById(id);
        if (reimbursement == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        // 2. 校验本人归属
        if (ObjUtil.notEqual(reimbursement.getCreator(), userId.toString())) {
            throw exception(APPLY_ACCESS_DENIED);
        }
        // 3. 只有未提交草稿允许修改或提交
        if (ObjUtil.notEqual(reimbursement.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(APPLY_STATUS_INVALID);
        }
        return reimbursement;
    }

    /**
     * 校验费用报销存在
     *
     * @param id 申请编号
     * @return 申请
     */
    private OaReimbursementDO validateReimbursementExists(Long id) {
        OaReimbursementDO reimbursement = reimbursementMapper.selectById(id);
        if (reimbursement == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        return reimbursement;
    }

}
