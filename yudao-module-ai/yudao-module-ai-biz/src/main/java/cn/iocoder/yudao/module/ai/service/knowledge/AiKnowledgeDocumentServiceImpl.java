package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeDocumentCreateListReqVO;
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
    @Transactional(rollbackFor = Exception.class)
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

        // 4. 文档切片入库（同步）
        knowledgeSegmentService.createKnowledgeSegmentBySplitContent(documentDO.getId(), content);
        return documentDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            documentDOs.add(BeanUtils.toBean(documentVO, AiKnowledgeDocumentDO.class).setKnowledgeId(createListReqVO.getKnowledgeId())
                    .setContent(content).setContentLength(content.length()).setTokens(tokenCountEstimator.estimate(content))
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
        knowledgeDocumentMapper.insertBatch(documentDOs);

        // 4. 批量创建文档切片（异步）
        documentDOs.forEach(documentDO ->
                knowledgeSegmentService.createKnowledgeSegmentBySplitContentAsync(documentDO.getId(), documentDO.getContent()));
        return convertList(documentDOs, AiKnowledgeDocumentDO::getId);
    }

    @Override
    public PageResult<AiKnowledgeDocumentDO> getKnowledgeDocumentPage(AiKnowledgeDocumentPageReqVO pageReqVO) {
        return knowledgeDocumentMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateKnowledgeDocument(AiKnowledgeDocumentUpdateReqVO reqVO) {
        // 1. 校验文档是否存在
        validateKnowledgeDocumentExists(reqVO.getId());
        // 2. 更新文档
        AiKnowledgeDocumentDO document = BeanUtils.toBean(reqVO, AiKnowledgeDocumentDO.class);
        knowledgeDocumentMapper.updateById(document);
        // TODO @芋艿：这里要处理状态的变更
    }

    @Override
    public AiKnowledgeDocumentDO validateKnowledgeDocumentExists(Long id) {
        AiKnowledgeDocumentDO knowledgeDocument = knowledgeDocumentMapper.selectById(id);
        if (knowledgeDocument == null) {
            throw exception(KNOWLEDGE_DOCUMENT_NOT_EXISTS);
        }
        return knowledgeDocument;
    }

    private static String readUrl(String url) {
        // 下载文件
        ByteArrayResource resource = null;
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

}
