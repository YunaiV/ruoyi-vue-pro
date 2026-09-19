package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceivePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceiveSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocReceiveDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.OaOfficialDocReceiveMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.enums.officialdoc.OaOfficialDocHandleStatusEnum;
import cn.iocoder.yudao.module.oa.enums.officialdoc.OaOfficialDocReceiveTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 公文收文 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaOfficialDocReceiveServiceImpl implements OaOfficialDocReceiveService {

    @Resource
    private OaOfficialDocReceiveMapper officialDocReceiveMapper;

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    public Long createOfficialDocReceive(OaOfficialDocReceiveSaveReqVO reqVO) {
        // 1. 生成收文单号并校验唯一
        String no = noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_RECEIVE_NO_PREFIX);
        validateOfficialDocReceiveNoUnique(no);

        // 2. 保存手工收文
        OaOfficialDocReceiveDO receive = BeanUtils.toBean(reqVO, OaOfficialDocReceiveDO.class).setId(null)
                .setReceiveType(OaOfficialDocReceiveTypeEnum.MAIN.getType()).setNo(no)
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus())
                .setHandleStatus(OaOfficialDocHandleStatusEnum.WAIT_CLAIM.getStatus());
        officialDocReceiveMapper.insert(receive);
        return receive.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOfficialDocReceiveListByOfficialDocSend(OaOfficialDocSendDO send) {
        // 1.1 查询已经生成的收文
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(send.getId());
        // 1.2 按收文类型整理接收部门
        Map<Integer, List<Long>> deptIdsByReceiveType = new LinkedHashMap<>();
        deptIdsByReceiveType.put(OaOfficialDocReceiveTypeEnum.MAIN.getType(), send.getMainDeptIds());
        deptIdsByReceiveType.put(OaOfficialDocReceiveTypeEnum.COPY.getType(), send.getCopyDeptIds());

        // 2. 分别创建主送、抄送的待签收记录
        deptIdsByReceiveType.forEach((receiveType, deptIds) -> {
            if (CollUtil.isEmpty(deptIds)) {
                return;
            }
            Set<Long> receivedDeptIds = convertSet(receives, OaOfficialDocReceiveDO::getReceiveDeptId,
                    receive -> ObjUtil.equal(receive.getReceiveType(), receiveType));
            deptIds.forEach(deptId -> {
                // 同一类型已投递的部门不重复生成，主送与抄送互不影响
                if (!receivedDeptIds.add(deptId)) {
                    return;
                }
                // 2.1 生成接收部门的收文单号，并校验唯一性
                String no = noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_RECEIVE_NO_PREFIX);
                validateOfficialDocReceiveNoUnique(no);
                // 2.2 保存发文快照，初始化为未开始审批、待签收的部门收文
                OaOfficialDocReceiveDO receive = new OaOfficialDocReceiveDO().setSendId(send.getId())
                        .setNo(no)
                        .setReceiveDeptId(deptId).setReceiveType(receiveType).setTitle(send.getTitle())
                        .setDocumentNo(send.getDocumentNo()).setSecrecyLevel(send.getSecrecyLevel())
                        .setUrgencyLevel(send.getUrgencyLevel()).setFormalFileUrl(send.getFormalFileUrl())
                        .setFileUrls(send.getFileUrls()).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus())
                        .setHandleStatus(OaOfficialDocHandleStatusEnum.WAIT_CLAIM.getStatus());
                receive.setCreator(""); // 自动投递尚无归属人，避免被当前审批人的通用审计填充占用；签收后再确定归属
                officialDocReceiveMapper.insert(receive);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOfficialDocReceive(OaOfficialDocReceiveSaveReqVO reqVO, Long userId) {
        // 1.1 校验收文存在
        OaOfficialDocReceiveDO receive = validateOfficialDocReceiveExists(reqVO.getId());
        // 1.2 校验收文归属
        validateOfficialDocReceiveOwner(receive, userId);
        // 1.3 仅未提交草稿允许修改
        validateOfficialDocReceiveDraft(receive);

        // 2. 更新表单字段，不覆盖流程状态和归属
        OaOfficialDocReceiveDO updateObj = BeanUtils.toBean(reqVO, OaOfficialDocReceiveDO.class)
                .setReceiveType(receive.getReceiveType());
        // 自动收文保留投递时的附件和正式公文，不允许通过编辑替换
        if (receive.getSendId() != null) {
            updateObj.setFileUrls(receive.getFileUrls()).setFormalFileUrl(receive.getFormalFileUrl());
        }
        officialDocReceiveMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimOfficialDocReceive(Long id, Long userId) {
        // 1.1 校验收文存在
        OaOfficialDocReceiveDO receive = validateOfficialDocReceiveExists(id);
        // 1.2 校验接收部门
        if (ObjUtil.notEqual(receive.getReceiveDeptId(), adminUserApi.getUser(userId).getDeptId())) {
            throw exception(OFFICIAL_DOC_ACCESS_DENIED);
        }
        // 1.3 仅未签收的自动投递单据允许认领
        if (receive.getSendId() == null || StrUtil.isNotEmpty(receive.getCreator())
                || ObjUtil.notEqual(receive.getHandleStatus(), OaOfficialDocHandleStatusEnum.WAIT_CLAIM.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 签收仅确定单据归属和办理状态，保留主办人及收文时间
        OaOfficialDocReceiveDO updateObj = new OaOfficialDocReceiveDO().setId(id)
                .setHandleStatus(OaOfficialDocHandleStatusEnum.CLAIMED.getStatus());
        updateObj.setCreator(userId.toString());
        officialDocReceiveMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOfficialDocReceive(Long id, Long userId) {
        // 1.1 校验收文存在
        OaOfficialDocReceiveDO receive = validateOfficialDocReceiveExists(id);
        // 1.2 校验收文归属
        validateOfficialDocReceiveOwner(receive, userId);
        // 1.3 校验草稿状态
        validateOfficialDocReceiveDraft(receive);
        // 1.4 抄送仅知会，不发起办理审批
        if (ObjUtil.notEqual(receive.getReceiveType(), OaOfficialDocReceiveTypeEnum.MAIN.getType())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 更新审批状态和办理状态
        officialDocReceiveMapper.updateById(new OaOfficialDocReceiveDO().setId(id).setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus())
                .setHandleStatus(OaOfficialDocHandleStatusEnum.PROCESSING.getStatus()));

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.OFFICIAL_DOC_RECEIVE)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        officialDocReceiveMapper.updateById(new OaOfficialDocReceiveDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOfficialDocReceive(Long id, Long userId) {
        // 1.1 校验收文存在
        OaOfficialDocReceiveDO receive = validateOfficialDocReceiveExists(id);
        // 1.2 校验收文归属
        validateOfficialDocReceiveOwner(receive, userId);
        // 1.3 仅审批中的单据允许撤销
        if (ObjUtil.notEqual(receive.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 撤销审批流程，审批事件同步单据状态
        processInstanceApi.cancelProcessInstanceByStartUser(userId, receive.getProcessInstanceId(), "申请人撤销公文收文");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOfficialDocReceiveStatus(Long id, Integer status) {
        // 1. 校验收文存在
        validateOfficialDocReceiveExists(id);

        // 2. 更新审批结果及办理状态
        officialDocReceiveMapper.updateById(new OaOfficialDocReceiveDO().setId(id).setStatus(status)
                .setHandleStatus(ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                        ? OaOfficialDocHandleStatusEnum.COMPLETED.getStatus() : OaOfficialDocHandleStatusEnum.CLAIMED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOfficialDocReceive(Long id, Long userId) {
        // 1.1 校验收文存在
        OaOfficialDocReceiveDO receive = validateOfficialDocReceiveExists(id);
        // 1.2 校验收文归属
        validateOfficialDocReceiveOwner(receive, userId);
        // 1.3 未提交、审批不通过和已取消的单据允许删除
        // TODO @AI：notEqualsAny
        if (!ObjectUtils.equalsAny(receive.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus(),
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 删除公文
        officialDocReceiveMapper.deleteById(id);
    }

    @Override
    public PageResult<OaOfficialDocReceiveDO> getOfficialDocReceivePage(OaOfficialDocReceivePageReqVO reqVO, Long userId) {
        Long deptId = adminUserApi.getUser(userId).getDeptId();
        if (deptId == null) {
            return PageResult.empty();
        }
        return officialDocReceiveMapper.selectPage(deptId, reqVO);
    }

    @Override
    public OaOfficialDocReceiveDO getOfficialDocReceive(Long id) {
        return officialDocReceiveMapper.selectById(id);
    }

    /**
     * 校验收文单据编号唯一
     *
     * @param no 单据编号
     */
    private void validateOfficialDocReceiveNoUnique(String no) {
        if (officialDocReceiveMapper.selectByNo(no) != null) {
            throw exception(OFFICIAL_DOC_RECEIVE_NO_DUPLICATE);
        }
    }

    /**
     * 校验公文收文存在
     *
     * @param id 公文收文编号
     * @return 公文收文
     */
    private OaOfficialDocReceiveDO validateOfficialDocReceiveExists(Long id) {
        OaOfficialDocReceiveDO receive = officialDocReceiveMapper.selectById(id);
        if (receive == null) {
            throw exception(OFFICIAL_DOC_NOT_EXISTS);
        }
        return receive;
    }

    /**
     * 校验公文收文操作人
     *
     * @param receive 公文收文
     * @param userId 操作用户编号
     */
    private void validateOfficialDocReceiveOwner(OaOfficialDocReceiveDO receive, Long userId) {
        if (ObjUtil.notEqual(receive.getCreator(), userId.toString())) {
            throw exception(OFFICIAL_DOC_ACCESS_DENIED);
        }
    }

    /**
     * 校验公文收文为未提交草稿
     *
     * @param receive 公文收文
     */
    private void validateOfficialDocReceiveDraft(OaOfficialDocReceiveDO receive) {
        if (ObjUtil.notEqual(receive.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }
    }

}
