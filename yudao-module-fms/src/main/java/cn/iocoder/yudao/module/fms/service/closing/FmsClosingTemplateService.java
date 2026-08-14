package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateSaveReqVO;

import java.util.Collection;
import java.util.List;

/**
 * FMS 结账模板 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsClosingTemplateService {

    /**
     * 初始化账套结账模板
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     */
    void initializeClosingTemplates(Long accountSetId, Long userId);

    /**
     * 获得结账模板列表
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 结账模板列表
     */
    List<FmsClosingTemplateRespVO> getClosingTemplateList(Long accountSetId, Long userId);

    /**
     * 获得引用指定科目的结账模板数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号集合
     * @return 结账模板数量
     */
    Long getClosingTemplateCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 创建结账模板
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 模板编号
     */
    Long createClosingTemplate(FmsClosingTemplateSaveReqVO createReqVO, Long userId);

    /**
     * 更新结账模板
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateClosingTemplate(FmsClosingTemplateSaveReqVO updateReqVO, Long userId);

    /**
     * 删除结账模板
     *
     * @param accountSetId 账套编号
     * @param id 模板编号
     * @param userId 用户编号
     */
    void deleteClosingTemplate(Long accountSetId, Long id, Long userId);

}
