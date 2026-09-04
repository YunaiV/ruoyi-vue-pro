package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplatePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryTemplateDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryTemplateMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS;

/**
 * PMS 知识库模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeLibraryTemplateServiceImpl implements PmsKnowledgeLibraryTemplateService {

    @Resource
    private PmsKnowledgeLibraryTemplateMapper libraryTemplateMapper;
    @Resource
    private PmsKnowledgeDocumentService documentService;

    @Override
    public Long createLibraryTemplate(PmsKnowledgeLibraryTemplateSaveReqVO createReqVO) {
        // 1. 校验模板名称和文档配置
        validateLibraryTemplateNameDuplicate(null, createReqVO.getName());
        validateLibraryTemplateDocuments(createReqVO.getDocuments());

        // 2. 创建知识库模板
        PmsKnowledgeLibraryTemplateDO template = BeanUtils.toBean(createReqVO, PmsKnowledgeLibraryTemplateDO.class)
                .setDocuments(BeanUtils.toBean(createReqVO.getDocuments(), PmsKnowledgeLibraryTemplateDO.Document.class));
        libraryTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateLibraryTemplate(PmsKnowledgeLibraryTemplateSaveReqVO updateReqVO) {
        // 1. 校验模板存在、名称和文档配置
        validateLibraryTemplateExists(updateReqVO.getId());
        validateLibraryTemplateNameDuplicate(updateReqVO.getId(), updateReqVO.getName());
        validateLibraryTemplateDocuments(updateReqVO.getDocuments());

        // 2. 更新知识库模板
        libraryTemplateMapper.updateById(BeanUtils.toBean(updateReqVO, PmsKnowledgeLibraryTemplateDO.class)
                .setDocuments(BeanUtils.toBean(updateReqVO.getDocuments(), PmsKnowledgeLibraryTemplateDO.Document.class)));
    }

    @Override
    public void deleteLibraryTemplate(Long id) {
        // 1. 校验模板存在
        validateLibraryTemplateExists(id);

        // 2. 删除知识库模板
        libraryTemplateMapper.deleteById(id);
    }

    @Override
    public PmsKnowledgeLibraryTemplateDO getLibraryTemplate(Long id) {
        return validateLibraryTemplateExists(id);
    }

    @Override
    public PageResult<PmsKnowledgeLibraryTemplateDO> getLibraryTemplatePage(
            PmsKnowledgeLibraryTemplatePageReqVO pageReqVO) {
        return libraryTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PmsKnowledgeLibraryTemplateDO> getLibraryTemplateList(Integer status) {
        return libraryTemplateMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTemplateDocumentList(Long templateId, Long libraryId, Long userId) {
        // 1. 校验模板存在且处于启用状态
        PmsKnowledgeLibraryTemplateDO template = validateLibraryTemplateExists(templateId);
        if (CommonStatusEnum.isDisable(template.getStatus())) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS);
        }
        if (CollUtil.isEmpty(template.getDocuments())) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_INVALID);
        }

        // 2. 在知识库根目录创建模板富文本文档；每篇文档独立创建内容权限
        for (PmsKnowledgeLibraryTemplateDO.Document document : template.getDocuments()) {
            documentService.createDocument(new PmsKnowledgeDocumentCreateReqVO().setLibraryId(libraryId)
                    .setFolderId(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT)
                    .setParentId(PmsKnowledgeDocumentDO.PARENT_ID_ROOT).setTitle(document.getTitle())
                    .setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType())
                    .setContent(document.getContent()), userId);
        }
    }

    private PmsKnowledgeLibraryTemplateDO validateLibraryTemplateExists(Long id) {
        PmsKnowledgeLibraryTemplateDO template = libraryTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 校验知识库模板名称唯一
     *
     * @param id 模板编号
     * @param name 模板名称
     */
    private void validateLibraryTemplateNameDuplicate(Long id, String name) {
        PmsKnowledgeLibraryTemplateDO template = libraryTemplateMapper.selectByName(name);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的模板
        if (id == null) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(template.getId(), id)) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE);
        }
    }

    private void validateLibraryTemplateDocuments(List<PmsKnowledgeLibraryTemplateSaveReqVO.Document> documents) {
        if (CollUtil.isEmpty(documents)) {
            throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_INVALID);
        }
        Set<String> titles = new HashSet<>();
        for (PmsKnowledgeLibraryTemplateSaveReqVO.Document document : documents) {
            if (document == null || StrUtil.hasBlank(document.getTitle(), document.getContent())
                    || !titles.add(document.getTitle())) {
                throw exception(KNOWLEDGE_LIBRARY_TEMPLATE_INVALID);
            }
        }
    }

}
