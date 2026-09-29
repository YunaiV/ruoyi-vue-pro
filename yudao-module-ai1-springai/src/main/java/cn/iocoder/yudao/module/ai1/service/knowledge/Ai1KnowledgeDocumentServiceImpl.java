package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentVectorizeRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.knowledge.Ai1KnowledgeDocumentMapper;
import cn.iocoder.yudao.module.ai1.enums.knowledge.Ai1KnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.ai1.tool.rag.Ai1RagTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

// TODO @AI：“文档向量化在请求线程内同步执行，登录上下文中的租户天然存在”，这里是不是去掉噢；
/**
 * AI1 知识文档 Service 实现类
 *
 * 文档向量化在请求线程内同步执行，登录上下文中的租户天然存在
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class Ai1KnowledgeDocumentServiceImpl implements Ai1KnowledgeDocumentService {

    /**
     * 允许上传的文本文件扩展名
     */
    private static final List<String> UPLOAD_FILE_SUFFIXES = Arrays.asList(".txt", ".md");

    @Resource
    private Ai1KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Resource
    private Ai1KnowledgeBaseService knowledgeBaseService;

    @Resource
    private Ai1RagTool ragTool;

    @Override
    public Long createKnowledgeDocument(Ai1KnowledgeDocumentSaveReqVO createReqVO) {
        // 1. 校验知识库存在
        knowledgeBaseService.validateKnowledgeBaseExists(createReqVO.getKnowledgeBaseId());

        // 2. 插入：初始为未处理，需手动向量化
        Ai1KnowledgeDocumentDO document = BeanUtils.toBean(createReqVO, Ai1KnowledgeDocumentDO.class)
                .setChunkCount(0).setStatus(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus());
        knowledgeDocumentMapper.insert(document);
        return document.getId();
    }

    @Override
    public Long uploadKnowledgeDocument(Long knowledgeBaseId, MultipartFile file) {
        // 1. 校验文件类型
        String filename = file.getOriginalFilename();
        // TODO @AI：要不要支持更多的类型噢？
        if (StrUtil.isBlank(filename) || !StrUtil.endWithAnyIgnoreCase(filename, UPLOAD_FILE_SUFFIXES.toArray(new String[0]))) {
            throw exception(KNOWLEDGE_DOCUMENT_FILE_TYPE_INVALID);
        }

        // TODO @AI：1.1 1.2 ；他们再做类的事情哈；
        // 2. 读取文本内容（UTF-8）
        String content;
        try (InputStream inputStream = file.getInputStream()) {
            // TODO @AI：IoUtil 支持 readUtf8 的呀；
            content = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[uploadKnowledgeDocument][知识库({}) 文件({}) 读取失败]", knowledgeBaseId, filename, e);
            throw exception(KNOWLEDGE_DOCUMENT_FILE_READ_FAIL, e.getMessage());
        }
        if (StrUtil.isBlank(content)) {
            throw exception(KNOWLEDGE_DOCUMENT_FILE_EMPTY);
        }

        // 3. 创建文档
        // TODO @AI：name 的长度放开吧，简化点；先 new 再创建；
        return createKnowledgeDocument(new Ai1KnowledgeDocumentSaveReqVO().setKnowledgeBaseId(knowledgeBaseId)
                .setName(StrUtil.maxLength(filename, 197)).setContent(content));
    }

    @Override
    public void updateKnowledgeDocument(Ai1KnowledgeDocumentSaveReqVO updateReqVO) {
        // 1. 校验存在
        Ai1KnowledgeDocumentDO document = validateKnowledgeDocumentExists(updateReqVO.getId());

        // 2. 更新：归属知识库不可变更；内容变更时重置为未处理，需重新向量化
        Ai1KnowledgeDocumentDO updateObj = new Ai1KnowledgeDocumentDO().setId(document.getId())
                .setName(updateReqVO.getName()).setContent(updateReqVO.getContent());
        boolean contentChanged = ObjUtil.notEqual(document.getContent(), updateReqVO.getContent());
        if (contentChanged) {
            updateObj.setChunkCount(0).setStatus(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus());
        }
        knowledgeDocumentMapper.updateById(updateObj);

        // 3. 内容变更时清理旧向量
        if (contentChanged) {
            Ai1KnowledgeBaseDO knowledgeBase = knowledgeBaseService.getKnowledgeBase(document.getKnowledgeBaseId());
            if (knowledgeBase != null) {
                ragTool.deleteByDocumentIds(knowledgeBase, Collections.singletonList(document.getId()));
            }
        }
    }

    @Override
    public void deleteKnowledgeDocument(Long id) {
        deleteKnowledgeDocumentListByIds(Collections.singletonList(id));
    }

    @Override
    public void deleteKnowledgeDocumentListByIds(List<Long> ids) {
        // 1. 校验存在
        List<Ai1KnowledgeDocumentDO> documents = convertList(ids, this::validateKnowledgeDocumentExists);

        // 2. 删除
        knowledgeDocumentMapper.deleteByIds(ids);

        // 3. 按知识库分组清理向量
        Map<Long, List<Long>> documentIdsMap = convertMultiMap(documents,
                Ai1KnowledgeDocumentDO::getKnowledgeBaseId, Ai1KnowledgeDocumentDO::getId);
        for (Ai1KnowledgeBaseDO knowledgeBase : knowledgeBaseService.getKnowledgeBaseList(documentIdsMap.keySet())) {
            ragTool.deleteByDocumentIds(knowledgeBase, documentIdsMap.get(knowledgeBase.getId()));
        }
    }

    @Override
    public void deleteKnowledgeDocumentListByKnowledgeBaseIds(Collection<Long> knowledgeBaseIds) {
        if (CollUtil.isEmpty(knowledgeBaseIds)) {
            return;
        }
        knowledgeDocumentMapper.deleteByKnowledgeBaseIds(knowledgeBaseIds);
    }

    @Override
    public Ai1KnowledgeDocumentDO getKnowledgeDocument(Long id) {
        return knowledgeDocumentMapper.selectById(id);
    }

    @Override
    public PageResult<Ai1KnowledgeDocumentDO> getKnowledgeDocumentPage(Ai1KnowledgeDocumentPageReqVO pageReqVO) {
        return knowledgeDocumentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1KnowledgeDocumentDO> getKnowledgeDocumentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return knowledgeDocumentMapper.selectByIds(ids);
    }

    // TODO @AI：方法内注释；现在有点粘合在一起
    @Override
    public Integer vectorizeKnowledgeDocument(Long id) {
        Ai1KnowledgeDocumentDO document = validateKnowledgeDocumentExists(id);
        Ai1KnowledgeBaseDO knowledgeBase = knowledgeBaseService.validateKnowledgeBaseExists(document.getKnowledgeBaseId());
        return vectorizeKnowledgeDocument(knowledgeBase, document);
    }

    @Override
    public Ai1KnowledgeDocumentVectorizeRespVO vectorizeKnowledgeDocumentListByKnowledgeBaseId(Long knowledgeBaseId) {
        // 1. 校验知识库存在、且有文档
        Ai1KnowledgeBaseDO knowledgeBase = knowledgeBaseService.validateKnowledgeBaseExists(knowledgeBaseId);
        List<Ai1KnowledgeDocumentDO> documents = knowledgeDocumentMapper.selectListByKnowledgeBaseId(knowledgeBaseId);
        if (CollUtil.isEmpty(documents)) {
            throw exception(KNOWLEDGE_BASE_DOCUMENT_EMPTY);
        }

        // 2. 逐个向量化：跳过空内容文档，单个失败不中断
        int successCount = 0;
        int failureCount = 0;
        for (Ai1KnowledgeDocumentDO document : documents) {
            if (StrUtil.isBlank(document.getContent())) {
                continue;
            }
            try {
                vectorizeKnowledgeDocument(knowledgeBase, document);
                successCount++;
            } catch (ServiceException e) {
                failureCount++;
                log.warn("[vectorizeKnowledgeDocumentListByKnowledgeBaseId][知识库({}) 文档({}) 向量化失败：{}]",
                        knowledgeBaseId, document.getId(), e.getMessage());
            }
        }
        return new Ai1KnowledgeDocumentVectorizeRespVO(successCount, failureCount);
    }

    /**
     * 单文档向量化：调用 RAG 工具写入向量后回写已向量化；任何失败都回写失败状态，并抛出业务异常
     */
    private Integer vectorizeKnowledgeDocument(Ai1KnowledgeBaseDO knowledgeBase, Ai1KnowledgeDocumentDO document) {
        if (StrUtil.isBlank(document.getContent())) {
            throw exception(KNOWLEDGE_DOCUMENT_CONTENT_EMPTY);
        }
        try {
            int chunkCount = ragTool.vectorize(knowledgeBase, document.getId(), document.getContent());
            knowledgeDocumentMapper.updateById(new Ai1KnowledgeDocumentDO().setId(document.getId())
                    .setChunkCount(chunkCount).setStatus(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus()));
            return chunkCount;
        } catch (Exception e) {
            log.warn("[vectorizeKnowledgeDocument][文档({}) 向量化失败]", document.getId(), e);
            knowledgeDocumentMapper.updateById(new Ai1KnowledgeDocumentDO().setId(document.getId())
                    .setStatus(Ai1KnowledgeDocumentStatusEnum.FAILED.getStatus()));
            // TODO @AI：jdk8 兼容性；
            if (e instanceof ServiceException serviceException) {
                throw serviceException;
            }
            throw exception(KNOWLEDGE_DOCUMENT_VECTORIZE_FAIL, e.getMessage());
        }
    }

    private Ai1KnowledgeDocumentDO validateKnowledgeDocumentExists(Long id) {
        Ai1KnowledgeDocumentDO document = knowledgeDocumentMapper.selectById(id);
        if (document == null) {
            throw exception(KNOWLEDGE_DOCUMENT_NOT_EXISTS);
        }
        return document;
    }

}
