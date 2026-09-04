package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeFolderMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FOLDER_NOT_EXISTS;

/**
 * PMS 知识库文件夹读取 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeFolderReadServiceImpl implements PmsKnowledgeFolderReadService {

    @Resource
    private PmsKnowledgeFolderMapper folderMapper;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Override
    public PmsKnowledgeFolderDO getReadableFolder(Long id, Long userId) {
        // 1.1 校验文件夹存在
        PmsKnowledgeFolderDO folder = folderMapper.selectById(id);
        if (folder == null) {
            throw exception(KNOWLEDGE_FOLDER_NOT_EXISTS);
        }
        // 1.2 校验文件夹未被回收
        if (ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), folder.getStatus())) {
            throw exception(KNOWLEDGE_FOLDER_NOT_EXISTS);
        }

        // 2.1 校验知识库读取权限
        libraryMemberService.validateLibraryReadable(folder.getLibraryId(), userId);
        // 2.2 校验内容读取权限
        contentPermissionService.validateContentPermissionReadable(folder.getPermissionId(), folder.getLibraryId(), userId);
        return folder;
    }

    @Override
    public List<PmsKnowledgeFolderDO> getFolderList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? Collections.emptyList() : folderMapper.selectByIds(ids);
    }

}
