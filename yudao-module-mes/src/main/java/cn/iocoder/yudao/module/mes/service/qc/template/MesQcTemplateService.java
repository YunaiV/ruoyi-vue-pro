package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.MesQcTemplatePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.MesQcTemplateSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 质检方案 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcTemplateService {

    /**
     * 创建质检方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTemplate(@Valid MesQcTemplateSaveReqVO createReqVO);

    /**
     * 更新质检方案
     *
     * @param updateReqVO 更新信息
     */
    void updateTemplate(@Valid MesQcTemplateSaveReqVO updateReqVO);

    /**
     * 删除质检方案（级联删除检测指标项和产品关联）
     *
     * @param id 编号
     */
    void deleteTemplate(Long id);

    /**
     * 获得质检方案
     *
     * @param id 编号
     * @return 质检方案
     */
    MesQcTemplateDO getTemplate(Long id);

    /**
     * 获得质检方案分页
     *
     * @param pageReqVO 分页查询
     * @return 质检方案分页
     */
    PageResult<MesQcTemplateDO> getTemplatePage(MesQcTemplatePageReqVO pageReqVO);

    /**
     * 获得质检方案列表（用于下拉选择）
     *
     * @return 质检方案列表
     */
    List<MesQcTemplateDO> getTemplateList();

    /**
     * 获得质检方案列表
     *
     * @param ids 编号数组
     * @return 质检方案列表
     */
    List<MesQcTemplateDO> getTemplateList(Collection<Long> ids);

    /**
     * 获得质检方案 Map
     *
     * @param ids 编号数组
     * @return 质检方案 Map
     */
    default Map<Long, MesQcTemplateDO> getTemplateMap(Collection<Long> ids) {
        return convertMap(getTemplateList(ids), MesQcTemplateDO::getId);
    }

    /**
     * 校验质检方案存在
     *
     * @param id 编号
     * @return 质检方案
     */
    MesQcTemplateDO validateTemplateExists(Long id);

}
