package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentLabelDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentLabelMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS;

/**
 * PMS 知识库文档标签 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeDocumentLabelServiceImpl implements PmsKnowledgeDocumentLabelService {

    @Resource
    private PmsKnowledgeDocumentLabelMapper documentLabelMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;

    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Override
    public Long createDocumentLabel(PmsKnowledgeDocumentLabelSaveReqVO saveReqVO) {
        PmsKnowledgeDocumentLabelDO documentLabel = BeanUtils.toBean(saveReqVO, PmsKnowledgeDocumentLabelDO.class)
                .setName(StrUtil.trim(saveReqVO.getName())).setColor(StrUtil.trim(saveReqVO.getColor()));
        documentLabelMapper.insert(documentLabel);
        return documentLabel.getId();
    }

    @Override
    public void updateDocumentLabel(PmsKnowledgeDocumentLabelSaveReqVO saveReqVO) {
        // 1. 校验文档标签存在
        validateDocumentLabelExists(saveReqVO.getId());

        // 2. 更新文档标签
        documentLabelMapper.updateById(BeanUtils.toBean(saveReqVO, PmsKnowledgeDocumentLabelDO.class)
                .setName(StrUtil.trim(saveReqVO.getName())).setColor(StrUtil.trim(saveReqVO.getColor())));
    }

    @Override
    public void deleteDocumentLabel(Long id) {
        // 1. 校验文档标签存在
        validateDocumentLabelExists(id);

        // 2. 删除文档标签
        documentLabelMapper.deleteById(id);
    }

    @Override
    public PmsKnowledgeDocumentLabelDO getDocumentLabel(Long id) {
        return documentLabelMapper.selectById(id);
    }

    @Override
    public List<PmsKnowledgeDocumentLabelDO> getDocumentLabelList() {
        return documentLabelMapper.selectList();
    }

    @Override
    public void validateDocumentLabelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<PmsKnowledgeDocumentLabelDO> list = documentLabelMapper.selectByIds(ids);
        if (list.size() != ids.size()) {
            throw exception(KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<PmsKnowledgeDocumentDO> getDocumentPageByLabel(
            PmsKnowledgeDocumentLabelPageReqVO pageReqVO, Long userId) {
        // 1.1 校验文档标签存在
        validateDocumentLabelExists(pageReqVO.getLabelId());
        // 1.2 获得当前用户可读取的知识库
        List<Long> readableLibraryIds = libraryMemberService.getReadableLibraryIdList(userId);
        if (CollUtil.isEmpty(readableLibraryIds)) {
            return PageResult.empty();
        }
        Set<Long> readablePermissionIds = contentPermissionService
                .getReadableContentPermissionIdSet(readableLibraryIds, userId);
        if (CollUtil.isEmpty(readablePermissionIds)) {
            return PageResult.empty();
        }

        // 2. 按标签分页查询非回收站文档
        return documentMapper.selectPageByLabelIdAndStatuses(
                pageReqVO, readableLibraryIds, readablePermissionIds,
                PmsKnowledgeDocumentStatusEnum.ACTIVE_STATUSES);
    }

    private PmsKnowledgeDocumentLabelDO validateDocumentLabelExists(Long id) {
        PmsKnowledgeDocumentLabelDO documentLabel = documentLabelMapper.selectById(id);
        if (documentLabel == null) {
            throw exception(KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS);
        }
        return documentLabel;
    }

}
