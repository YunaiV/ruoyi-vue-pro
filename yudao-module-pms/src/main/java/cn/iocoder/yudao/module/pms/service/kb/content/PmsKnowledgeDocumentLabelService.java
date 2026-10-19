package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentLabelDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识库文档标签 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeDocumentLabelService {

    /**
     * 创建文档标签
     *
     * @param saveReqVO 保存信息
     * @return 标签编号
     */
    Long createDocumentLabel(PmsKnowledgeDocumentLabelSaveReqVO saveReqVO);

    /**
     * 更新文档标签
     *
     * @param saveReqVO 保存信息
     */
    void updateDocumentLabel(PmsKnowledgeDocumentLabelSaveReqVO saveReqVO);

    /**
     * 删除文档标签
     *
     * @param id 标签编号
     */
    void deleteDocumentLabel(Long id);

    /**
     * 获得文档标签
     *
     * @param id 标签编号
     * @return 文档标签
     */
    PmsKnowledgeDocumentLabelDO getDocumentLabel(Long id);

    /**
     * 获得文档标签列表
     *
     * @return 标签列表
     */
    List<PmsKnowledgeDocumentLabelDO> getDocumentLabelList();

    /**
     * 校验文档标签均存在
     *
     * @param ids 标签编号集合
     */
    void validateDocumentLabelList(Collection<Long> ids);

    /**
     * 按标签获得当前用户可读的文档分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 文档分页
     */
    PageResult<PmsKnowledgeDocumentDO> getDocumentPageByLabel(
            PmsKnowledgeDocumentLabelPageReqVO pageReqVO, Long userId);

}
