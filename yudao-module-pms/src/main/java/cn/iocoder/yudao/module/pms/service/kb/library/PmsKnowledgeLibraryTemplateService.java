package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplatePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryTemplateDO;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * PMS 知识库模板 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeLibraryTemplateService {

    /**
     * 创建知识库模板
     *
     * @param createReqVO 创建信息
     * @return 模板编号
     */
    Long createLibraryTemplate(PmsKnowledgeLibraryTemplateSaveReqVO createReqVO);

    /**
     * 更新知识库模板
     *
     * @param updateReqVO 更新信息
     */
    void updateLibraryTemplate(PmsKnowledgeLibraryTemplateSaveReqVO updateReqVO);

    /**
     * 删除知识库模板
     *
     * @param id 模板编号
     */
    void deleteLibraryTemplate(Long id);

    /**
     * 获得知识库模板
     *
     * @param id 模板编号
     * @return 知识库模板
     */
    PmsKnowledgeLibraryTemplateDO getLibraryTemplate(Long id);

    /**
     * 获得知识库模板分页
     *
     * @param pageReqVO 分页查询
     * @return 知识库模板分页
     */
    PageResult<PmsKnowledgeLibraryTemplateDO> getLibraryTemplatePage(
            PmsKnowledgeLibraryTemplatePageReqVO pageReqVO);

    /**
     * 获得知识库模板列表
     *
     * @param status 模板状态，为空时返回全部状态
     * @return 知识库模板列表
     */
    List<PmsKnowledgeLibraryTemplateDO> getLibraryTemplateList(@Nullable Integer status);

    /**
     * 根据知识库模板创建知识库文档
     *
     * @param templateId 模板编号
     * @param libraryId 知识库编号
     * @param userId 当前用户编号
     */
    void createTemplateDocumentList(Long templateId, Long libraryId, Long userId);

}
