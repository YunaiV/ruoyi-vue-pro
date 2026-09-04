package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentShareDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentShareMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_SHARE_ALREADY_OPEN;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_SHARE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.MessageTemplateConstants.KNOWLEDGE_DOCUMENT_SHARED;

/**
 * PMS 知识库文档分享 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PmsKnowledgeDocumentShareServiceImpl implements PmsKnowledgeDocumentShareService {

    @Resource
    private PmsKnowledgeDocumentShareMapper documentShareMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;

    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmsKnowledgeDocumentShareDO openShare(Long documentId, List<Long> shareUserIds, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(documentId);
        // 1.2 校验文档内容编辑权限
        contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId);
        // 1.3 校验文档尚未开启分享
        PmsKnowledgeDocumentShareDO share = documentShareMapper.selectByDocumentId(documentId);
        if (share != null && CommonStatusEnum.isEnable(share.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_SHARE_ALREADY_OPEN);
        }

        // 1.4 校验并规范分享成员
        List<Long> normalizedShareUserIds = normalizeShareUserIds(shareUserIds);

        // 2. 新建或重新开启分享；重新开启时生成新的外部令牌
        if (share == null) {
            share = new PmsKnowledgeDocumentShareDO().setDocumentId(documentId);
        }
        share.setShareUserIds(normalizedShareUserIds).setToken(generateShareToken())
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setCloseUserId(null).setCloseTime(null);
        if (share.getId() == null) {
            documentShareMapper.insert(share);
        } else {
            documentShareMapper.updateToReopen(share);
        }

        // 3. 通知内部分享成员
        sendKnowledgeDocumentSharedNotify(normalizedShareUserIds, userId, share.getToken(), document.getTitle());
        return share;
    }

    @Override
    public void updateShareMemberList(Long documentId, List<Long> shareUserIds, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(documentId);
        // 1.2 校验文档内容编辑权限
        contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId);
        // 1.3 校验文档已开启分享
        PmsKnowledgeDocumentShareDO share = validateActiveShare(documentId);

        // 1.4 校验并规范分享成员
        List<Long> normalizedShareUserIds = normalizeShareUserIds(shareUserIds);

        // 2. 更新内部分享成员
        documentShareMapper.updateById(new PmsKnowledgeDocumentShareDO().setId(share.getId())
                .setShareUserIds(normalizedShareUserIds));

        // 3. 只通知本次新增的分享成员
        Set<Long> addedUserIds = new LinkedHashSet<>(normalizedShareUserIds);
        if (CollUtil.isNotEmpty(share.getShareUserIds())) {
            share.getShareUserIds().forEach(addedUserIds::remove);
        }
        sendKnowledgeDocumentSharedNotify(addedUserIds, userId, share.getToken(), document.getTitle());
    }

    @Override
    public void closeShare(Long documentId, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(documentId);
        // 1.2 校验文档内容编辑权限
        contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId);
        // 1.3 校验文档已开启分享
        PmsKnowledgeDocumentShareDO share = validateActiveShare(documentId);

        // 2. 关闭分享
        documentShareMapper.updateById(new PmsKnowledgeDocumentShareDO().setId(share.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setCloseUserId(userId).setCloseTime(LocalDateTime.now()));
    }

    @Override
    public PmsKnowledgeDocumentShareDO getActiveDocumentShare(Long documentId) {
        PmsKnowledgeDocumentShareDO share = documentShareMapper.selectByDocumentId(documentId);
        return share != null && CommonStatusEnum.isEnable(share.getStatus())
                ? share : null;
    }

    @Override
    public PmsKnowledgeDocumentShareDO getActiveDocumentShare(Long documentId, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(documentId);
        // 1.2 校验文档内容读取权限
        contentPermissionService.validateContentPermissionReadable(
                document.getPermissionId(), document.getLibraryId(), userId);
        // 2. 返回当前仍处于开启状态的分享配置
        return getActiveDocumentShare(documentId);
    }

    @Override
    public PmsKnowledgeDocumentDO getDocumentByShareToken(String token) {
        // 1. 校验分享令牌处于开启状态
        PmsKnowledgeDocumentShareDO share = documentShareMapper.selectByToken(token);
        if (share == null || CommonStatusEnum.isDisable(share.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_SHARE_INVALID);
        }

        // 2. 查询正常状态的分享文档
        PmsKnowledgeDocumentDO document = documentMapper.selectById(share.getDocumentId());
        if (document == null || ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(),
                document.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_SHARE_INVALID);
        }
        return document;
    }

    @Override
    public void deleteSharesByDocumentIds(Collection<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return;
        }
        documentShareMapper.deleteByDocumentIds(documentIds);
    }

    private PmsKnowledgeDocumentDO validateDocumentExists(Long documentId) {
        PmsKnowledgeDocumentDO document = documentMapper.selectById(documentId);
        if (document == null || ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(),
                document.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_NOT_EXISTS);
        }
        return document;
    }

    private PmsKnowledgeDocumentShareDO validateActiveShare(Long documentId) {
        PmsKnowledgeDocumentShareDO share = getActiveDocumentShare(documentId);
        if (share == null) {
            throw exception(KNOWLEDGE_DOCUMENT_SHARE_INVALID);
        }
        return share;
    }

    /**
     * 规范并校验分享成员列表，避免重复通知并拒绝不存在或已禁用的用户
     */
    private List<Long> normalizeShareUserIds(List<Long> shareUserIds) {
        if (CollUtil.isEmpty(shareUserIds)) {
            adminUserApi.validateUserList(Collections.emptyList());
            return Collections.emptyList();
        }
        List<Long> normalizedShareUserIds = new ArrayList<>(new LinkedHashSet<>(shareUserIds));
        adminUserApi.validateUserList(normalizedShareUserIds);
        return normalizedShareUserIds;
    }

    /**
     * 生成外部分享令牌
     *
     * @return 新的分享令牌
     */
    private String generateShareToken() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 发送知识文档分享站内信。通知配置异常不能阻断文档分享主业务
     */
    private void sendKnowledgeDocumentSharedNotify(Collection<Long> receiverUserIds, Long operatorUserId,
                                                   String shareToken, String documentTitle) {
        if (CollUtil.isEmpty(receiverUserIds)) {
            return;
        }
        Set<Long> uniqueReceiverUserIds = new LinkedHashSet<>(receiverUserIds);
        uniqueReceiverUserIds.remove(null);
        uniqueReceiverUserIds.remove(operatorUserId);
        Map<String, Object> templateParams = Maps.newHashMapWithExpectedSize(2);
        templateParams.put("documentTitle", documentTitle);
        templateParams.put("route", "/pms/kb/document/share/" + shareToken);
        uniqueReceiverUserIds.forEach(receiverUserId -> {
            try {
                notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                        .setUserId(receiverUserId).setTemplateCode(KNOWLEDGE_DOCUMENT_SHARED)
                        .setTemplateParams(templateParams));
            } catch (RuntimeException ex) {
                log.warn("[sendKnowledgeDocumentSharedNotify][向用户({})发送知识文档分享站内信失败，参数为({})]",
                        receiverUserId, templateParams, ex);
            }
        });
    }

}
