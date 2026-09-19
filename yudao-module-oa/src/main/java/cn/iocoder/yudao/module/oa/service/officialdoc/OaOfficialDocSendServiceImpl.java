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
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.OaOfficialDocSendMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 公文发文 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaOfficialDocSendServiceImpl implements OaOfficialDocSendService {

    @Resource
    private OaOfficialDocSendMapper officialDocSendMapper;

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private OaOfficialDocTemplateService officialDocTemplateService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaOfficialDocReceiveService officialDocReceiveService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private DeptApi deptApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOfficialDocSend(OaOfficialDocSendSaveReqVO reqVO, Long userId) {
        // 1. 校验发文信息及关联数据
        validateOfficialDocSend(reqVO, null);
        // 1.2 生成单号并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_SEND_NO_PREFIX);
        if (officialDocSendMapper.selectByNo(no) != null) {
            throw exception(OFFICIAL_DOC_SEND_NO_DUPLICATE);
        }

        // 2. 保存发文草稿，单号及审批状态由服务端生成
        OaOfficialDocSendDO send = BeanUtils.toBean(reqVO, OaOfficialDocSendDO.class).setId(null)
                .setNo(no)
                .setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus()).setDocumentNo(buildOfficialDocSendDocumentNo(reqVO));
        officialDocSendMapper.insert(send);
        return send.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOfficialDocSend(OaOfficialDocSendSaveReqVO reqVO, Long userId) {
        // 1.1 校验发文存在
        OaOfficialDocSendDO send = validateOfficialDocSendExists(reqVO.getId());
        // 1.2 校验发文归属
        validateOfficialDocSendOwner(send, userId);
        // 1.3 仅未提交草稿允许修改
        validateOfficialDocSendDraft(send);
        // 1.4 校验发文信息
        validateOfficialDocSend(reqVO, send.getId());

        // 2. 更新表单字段，不覆盖流程状态和归属
        officialDocSendMapper.updateById(BeanUtils.toBean(reqVO, OaOfficialDocSendDO.class)
                .setDocumentNo(buildOfficialDocSendDocumentNo(reqVO)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOfficialDocSend(Long id, Long userId) {
        // 1.1 校验发文存在
        OaOfficialDocSendDO send = validateOfficialDocSendExists(id);
        // 1.2 校验发文归属
        validateOfficialDocSendOwner(send, userId);
        // 1.3 未提交、审批不通过和已取消的单据允许删除
        // TODO @AI：notEqualsAny
        if (!ObjectUtils.equalsAny(send.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus(),
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 删除公文
        officialDocSendMapper.deleteById(id);
    }

    @Override
    public OaOfficialDocSendDO getOfficialDocSend(Long id) {
        return officialDocSendMapper.selectById(id);
    }

    @Override
    public List<OaOfficialDocSendDO> getOfficialDocSendList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return officialDocSendMapper.selectByIds(ids);
    }

    @Override
    public PageResult<OaOfficialDocSendDO> getOfficialDocSendPage(OaOfficialDocSendPageReqVO reqVO, Long userId) {
        return officialDocSendMapper.selectPage(userId, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOfficialDocSend(Long id, Long userId) {
        // 1.1 校验发文存在
        OaOfficialDocSendDO send = validateOfficialDocSendExists(id);
        // 1.2 校验发文归属
        validateOfficialDocSendOwner(send, userId);
        // 1.3 校验草稿状态
        validateOfficialDocSendDraft(send);
        // 1.4 校验发文关联数据
        validateOfficialDocSend(BeanUtils.toBean(send, OaOfficialDocSendSaveReqVO.class), send.getId());

        // 2. 更新审批状态
        officialDocSendMapper.updateById(new OaOfficialDocSendDO().setId(id).setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.OFFICIAL_DOC_SEND)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        officialDocSendMapper.updateById(new OaOfficialDocSendDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOfficialDocSend(Long id, Long userId) {
        // 1.1 校验发文存在
        OaOfficialDocSendDO send = validateOfficialDocSendExists(id);
        // 1.2 校验发文归属
        validateOfficialDocSendOwner(send, userId);
        // 1.3 仅审批中的单据允许撤销
        if (ObjUtil.notEqual(send.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }

        // 2. 撤销审批流程，审批事件同步单据状态
        processInstanceApi.cancelProcessInstanceByStartUser(userId, send.getProcessInstanceId(), "申请人撤销公文发文");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOfficialDocSendStatus(Long id, String processInstanceId, Integer status) {
        // 1.1 校验发文存在
        OaOfficialDocSendDO send = validateOfficialDocSendExists(id);
        // 1.2 校验单据与回调流程一致
        if (StrUtil.isEmpty(processInstanceId) || (send.getProcessInstanceId() != null
                && ObjUtil.notEqual(send.getProcessInstanceId(), processInstanceId))) {
            throw exception(OFFICIAL_DOC_PROCESS_MISMATCH);
        }
        // 1.3 已处理的审批结果不再生成收文记录
        if (ObjUtil.notEqual(send.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2.1 更新审批结果
        officialDocSendMapper.updateById(new OaOfficialDocSendDO().setId(id).setStatus(status).setProcessInstanceId(processInstanceId));
        // 2.2 审批通过后发送至主送和抄送部门
        if (ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        }
    }

    /**
     * 校验公文发文存在
     *
     * @param id 公文发文编号
     * @return 公文发文
     */
    private OaOfficialDocSendDO validateOfficialDocSendExists(Long id) {
        OaOfficialDocSendDO send = officialDocSendMapper.selectById(id);
        if (send == null) {
            throw exception(OFFICIAL_DOC_NOT_EXISTS);
        }
        return send;
    }

    /**
     * 校验公文发文操作人
     *
     * @param send 公文发文
     * @param userId 操作用户编号
     */
    private void validateOfficialDocSendOwner(OaOfficialDocSendDO send, Long userId) {
        if (ObjUtil.notEqual(send.getCreator(), userId.toString())) {
            throw exception(OFFICIAL_DOC_ACCESS_DENIED);
        }
    }

    /**
     * 校验公文发文为未提交草稿
     *
     * @param send 公文发文
     */
    private void validateOfficialDocSendDraft(OaOfficialDocSendDO send) {
        if (ObjUtil.notEqual(send.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(OFFICIAL_DOC_STATUS_INVALID);
        }
    }

    /**
     * 校验发文关联的模板、部门及发文字号
     *
     * @param reqVO 发文信息
     * @param id 当前发文编号，新增时为空
     */
    private void validateOfficialDocSend(OaOfficialDocSendSaveReqVO reqVO, Long id) {
        // 1. 校验套红模板可用
        officialDocTemplateService.validateOfficialDocTemplate(reqVO.getTemplateId());
        // 2. 校验发文及接收部门
        deptApi.validateDeptList(Collections.singleton(reqVO.getSendDeptId()));
        deptApi.validateDeptList(reqVO.getMainDeptIds());
        if (CollUtil.isNotEmpty(reqVO.getCopyDeptIds())) {
            deptApi.validateDeptList(reqVO.getCopyDeptIds());
        }
        // 3. 完整发文字号不能重复，尚未编号的草稿不参与查重
        if (StrUtil.isNotBlank(reqVO.getNoPrefix()) && reqVO.getYear() != null && reqVO.getSequence() != null
                && officialDocSendMapper.selectCountByDocumentNoAndIdNot(buildOfficialDocSendDocumentNo(reqVO), id) > 0) {
            throw exception(OFFICIAL_DOC_SEND_DOCUMENT_NO_DUPLICATE);
        }
    }

    /**
     * 根据字号、年份及序号组合发文字号
     *
     * @param reqVO 发文信息
     * @return 发文字号
     */
    private String buildOfficialDocSendDocumentNo(OaOfficialDocSendSaveReqVO reqVO) {
        return StrUtil.nullToEmpty(reqVO.getNoPrefix())
                + (reqVO.getYear() == null ? "" : "〔" + reqVO.getYear() + "〕")
                + (reqVO.getSequence() == null ? "" : reqVO.getSequence() + "号");
    }

    @Override
    public Long getOfficialDocSendCountByTemplateId(Long id) {
        return officialDocSendMapper.selectCountByTemplateId(id);
    }

}
