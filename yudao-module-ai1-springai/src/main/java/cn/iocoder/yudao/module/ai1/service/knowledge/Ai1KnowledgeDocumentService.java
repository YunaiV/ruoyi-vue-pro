package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document.Ai1KnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document.Ai1KnowledgeDocumentSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document.Ai1KnowledgeDocumentVectorizeRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI1 知识文档 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1KnowledgeDocumentService {

    /**
     * 创建文档（粘贴文本），初始为未处理状态
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createKnowledgeDocument(@Valid Ai1KnowledgeDocumentSaveReqVO createReqVO);

    /**
     * 上传文档：读取 .txt / .md 文本文件内容后创建
     *
     * @param knowledgeBaseId 知识库编号
     * @param file            文件
     * @return 编号
     */
    Long uploadKnowledgeDocument(Long knowledgeBaseId, MultipartFile file);

    /**
     * 更新文档；内容变更时重置为未处理状态，并清理旧向量
     *
     * @param updateReqVO 更新信息
     */
    void updateKnowledgeDocument(@Valid Ai1KnowledgeDocumentSaveReqVO updateReqVO);

    /**
     * 删除文档，连带清理向量
     *
     * @param id 编号
     */
    void deleteKnowledgeDocument(Long id);

    /**
     * 批量删除文档，连带清理向量
     *
     * @param ids 编号列表
     */
    void deleteKnowledgeDocumentListByIds(List<Long> ids);

    /**
     * 删除知识库下的全部文档；向量由知识库删除时整体 drop，这里不逐条清理
     *
     * @param knowledgeBaseIds 知识库编号集合
     */
    void deleteKnowledgeDocumentListByKnowledgeBaseIds(Collection<Long> knowledgeBaseIds);

    /**
     * 获得文档
     *
     * @param id 编号
     * @return 文档
     */
    Ai1KnowledgeDocumentDO getKnowledgeDocument(Long id);

    /**
     * 获得文档分页
     *
     * @param pageReqVO 分页查询
     * @return 文档分页
     */
    PageResult<Ai1KnowledgeDocumentDO> getKnowledgeDocumentPage(Ai1KnowledgeDocumentPageReqVO pageReqVO);

    /**
     * 获得文档列表
     *
     * @param ids 编号集合
     * @return 文档列表
     */
    List<Ai1KnowledgeDocumentDO> getKnowledgeDocumentList(Collection<Long> ids);

    /**
     * 获得指定编号的文档 Map
     *
     * @param ids 编号集合
     * @return 文档 Map
     */
    default Map<Long, Ai1KnowledgeDocumentDO> getKnowledgeDocumentMap(Collection<Long> ids) {
        return convertMap(getKnowledgeDocumentList(ids), Ai1KnowledgeDocumentDO::getId);
    }

    /**
     * 文档向量化：分片 → 嵌入 → 写入 Milvus，并回写状态与分片数；失败时回写失败状态并抛出异常
     *
     * @param id 编号
     * @return 分片数量
     */
    Integer vectorizeKnowledgeDocument(Long id);

    /**
     * 知识库批量向量化：逐个向量化库下有内容的文档，单个失败不中断
     *
     * @param knowledgeBaseId 知识库编号
     * @return 成功、失败数量
     */
    Ai1KnowledgeDocumentVectorizeRespVO vectorizeKnowledgeDocumentListByKnowledgeBaseId(Long knowledgeBaseId);

}
