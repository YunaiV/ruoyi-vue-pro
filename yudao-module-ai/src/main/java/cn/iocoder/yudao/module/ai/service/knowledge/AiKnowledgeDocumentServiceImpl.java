package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentCreateListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeDocumentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 知识库文档 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeDocumentServiceImpl implements AiKnowledgeDocumentService {

    @Resource
    private AiKnowledgeDocumentMapper knowledgeDocumentMapper;

    @Resource
    private TokenCountEstimator tokenCountEstimator;

    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiKnowledgeService knowledgeService;

    @Override
    public Long createKnowledgeDocument(AiKnowledgeDocumentCreateReqVO createReqVO) {
        // 1. 校验参数
        knowledgeService.validateKnowledgeExists(createReqVO.getKnowledgeId());

        // 2. 下载文档
        String content = readUrl(createReqVO.getUrl());

        // 3. 文档记录入库
        AiKnowledgeDocumentDO documentDO = BeanUtils.toBean(createReqVO, AiKnowledgeDocumentDO.class)
                .setContent(content).setContentLength(content.length()).setTokens(tokenCountEstimator.estimate(content))
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        knowledgeDocumentMapper.insert(documentDO);

        // 4. 文档切片入库（异步）
        knowledgeSegmentService.createKnowledgeSegmentBySplitContentAsync(documentDO.getId(), content);
        return documentDO.getId();
    }

    @Override
    public List<Long> createKnowledgeDocumentList(AiKnowledgeDocumentCreateListReqVO createListReqVO) {
        // 1. 校验参数
        knowledgeService.validateKnowledgeExists(createListReqVO.getKnowledgeId());

        // 2. 下载文档
        List<String> contents = convertList(createListReqVO.getList(), document -> readUrl(document.getUrl()));

        // 3. 文档记录入库
        List<AiKnowledgeDocumentDO> documentDOs = new ArrayList<>(createListReqVO.getList().size());
        for (int i = 0; i < createListReqVO.getList().size(); i++) {
            AiKnowledgeDocumentCreateListReqVO.Document documentVO = createListReqVO.getList().get(i);
            String content = contents.get(i);
            documentDOs.add(BeanUtils.toBean(documentVO, AiKnowledgeDocumentDO.class)
                    .setKnowledgeId(createListReqVO.getKnowledgeId())
                    .setContent(content).setContentLength(content.length())
                    .setTokens(tokenCountEstimator.estimate(content))
                    .setSegmentMaxTokens(createListReqVO.getSegmentMaxTokens())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
        knowledgeDocumentMapper.insertBatch(documentDOs);

        // 4. 批量创建文档切片（异步）
        documentDOs.forEach(documentDO -> knowledgeSegmentService
                .createKnowledgeSegmentBySplitContentAsync(documentDO.getId(), documentDO.getContent()));
        return convertList(documentDOs, AiKnowledgeDocumentDO::getId);
    }

    @Override
    public PageResult<AiKnowledgeDocumentDO> getKnowledgeDocumentPage(AiKnowledgeDocumentPageReqVO pageReqVO) {
        return knowledgeDocumentMapper.selectPage(pageReqVO);
    }

    @Override
    public AiKnowledgeDocumentDO getKnowledgeDocument(Long id) {
        return knowledgeDocumentMapper.selectById(id);
    }

    @Override
    public void updateKnowledgeDocument(AiKnowledgeDocumentUpdateReqVO reqVO) {
        // 1. 校验文档是否存在
        AiKnowledgeDocumentDO oldDocument = validateKnowledgeDocumentExists(reqVO.getId());

        // 2. 更新文档
        AiKnowledgeDocumentDO document = BeanUtils.toBean(reqVO, AiKnowledgeDocumentDO.class);
        knowledgeDocumentMapper.updateById(document);

        // 3. 如果处于开启状态，并且最大 tokens 发生变化，则 segment 需要重新索引
        if (CommonStatusEnum.isEnable(oldDocument.getStatus())
                && reqVO.getSegmentMaxTokens() != null
                && ObjUtil.notEqual(reqVO.getSegmentMaxTokens(), oldDocument.getSegmentMaxTokens())) {
            // 删除旧的文档切片
            knowledgeSegmentService.deleteKnowledgeSegmentByDocumentId(reqVO.getId());
            // 重新创建文档切片
            knowledgeSegmentService.createKnowledgeSegmentBySplitContentAsync(reqVO.getId(), oldDocument.getContent());
        }
    }

    @Override
    public void updateKnowledgeDocumentStatus(AiKnowledgeDocumentUpdateStatusReqVO reqVO) {
        // 1. 校验存在
        AiKnowledgeDocumentDO document = validateKnowledgeDocumentExists(reqVO.getId());

        // 2. 更新状态
        knowledgeDocumentMapper.updateById(new AiKnowledgeDocumentDO()
                .setId(reqVO.getId()).setStatus(reqVO.getStatus()));

        // 3. 处理文档切片
        if (CommonStatusEnum.isEnable(reqVO.getStatus())) {
            knowledgeSegmentService.createKnowledgeSegmentBySplitContentAsync(reqVO.getId(), document.getContent());
        } else {
            knowledgeSegmentService.deleteKnowledgeSegmentByDocumentId(reqVO.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeDocument(Long id) {
        // 1. 校验存在
        validateKnowledgeDocumentExists(id);

        // 2. 删除
        knowledgeDocumentMapper.deleteById(id);

        // 3. 删除对应的段落
        knowledgeSegmentService.deleteKnowledgeSegmentByDocumentId(id);
    }

    @Override
    public AiKnowledgeDocumentDO validateKnowledgeDocumentExists(Long id) {
        AiKnowledgeDocumentDO knowledgeDocument = knowledgeDocumentMapper.selectById(id);
        if (knowledgeDocument == null) {
            throw exception(KNOWLEDGE_DOCUMENT_NOT_EXISTS);
        }
        return knowledgeDocument;
    }

    @Override
    public String readUrl(String url) {
        // 下载文件
        ByteArrayResource resource;
        try {
            byte[] bytes = HttpUtil.downloadBytes(url);
            if (bytes.length == 0) {
                throw exception(KNOWLEDGE_DOCUMENT_FILE_EMPTY);
            }
            resource = new ByteArrayResource(bytes);
        } catch (Exception e) {
            log.error("[readUrl][url({}) 读取失败]", url, e);
            throw exception(KNOWLEDGE_DOCUMENT_FILE_DOWNLOAD_FAIL);
        }

        // 读取文件
        TikaDocumentReader loader = new TikaDocumentReader(resource);
        List<Document> documents = loader.get();
        Document document = CollUtil.getFirst(documents);
        if (document == null || StrUtil.isEmpty(document.getText())) {
            throw exception(KNOWLEDGE_DOCUMENT_FILE_READ_FAIL);
        }
        return document.getText();
    }

    @Override
    public List<AiKnowledgeDocumentDO> getKnowledgeDocumentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return knowledgeDocumentMapper.selectByIds(ids);
    }

    @Override
    public List<AiKnowledgeDocumentDO> getKnowledgeDocumentListByKnowledgeId(Long knowledgeId) {
        return knowledgeDocumentMapper.selectListByKnowledgeId(knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeDocumentByKnowledgeId(Long knowledgeId) {
        // 1. 获取该知识库下的所有文档
        List<AiKnowledgeDocumentDO> documents = knowledgeDocumentMapper.selectListByKnowledgeId(knowledgeId);
        if (CollUtil.isEmpty(documents)) {
            return;
        }

        // 2. 逐个删除文档及其对应的段落
        for (AiKnowledgeDocumentDO document : documents) {
            deleteKnowledgeDocument(document.getId());
        }
    }

}
