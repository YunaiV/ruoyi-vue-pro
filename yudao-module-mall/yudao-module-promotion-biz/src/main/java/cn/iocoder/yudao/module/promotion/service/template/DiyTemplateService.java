package cn.iocoder.yudao.module.promotion.service.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.template.vo.DiyTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.template.vo.DiyTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.template.vo.DiyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.template.DiyTemplateDO;

import javax.validation.Valid;

/**
 * 装修模板 Service 接口
 *
 * @author owen
 */
public interface DiyTemplateService {

    /**
     * 创建装修模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDiyTemplate(@Valid DiyTemplateCreateReqVO createReqVO);

    /**
     * 更新装修模板
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyTemplate(@Valid DiyTemplateUpdateReqVO updateReqVO);

    /**
     * 删除装修模板
     *
     * @param id 编号
     */
    void deleteDiyTemplate(Long id);

    /**
     * 获得装修模板
     *
     * @param id 编号
     * @return 装修模板
     */
    DiyTemplateDO getDiyTemplate(Long id);

    /**
     * 获得装修模板分页
     *
     * @param pageReqVO 分页查询
     * @return 装修模板分页
     */
    PageResult<DiyTemplateDO> getDiyTemplatePage(DiyTemplatePageReqVO pageReqVO);

    /**
     * 使用装修模板
     *
     * @param id 编号
     */
    void useDiyTemplate(Long id);

}
