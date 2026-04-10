package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator.MesQcTemplateIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator.MesQcTemplateIndicatorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import jakarta.validation.Valid;

/**
 * MES 质检方案-检测指标项 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcTemplateIndicatorService {

    /**
     * 创建检测指标项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTemplateIndicator(@Valid MesQcTemplateIndicatorSaveReqVO createReqVO);

    /**
     * 更新检测指标项
     *
     * @param updateReqVO 更新信息
     */
    void updateTemplateIndicator(@Valid MesQcTemplateIndicatorSaveReqVO updateReqVO);

    /**
     * 删除检测指标项
     *
     * @param id 编号
     */
    void deleteTemplateIndicator(Long id);

    /**
     * 获得检测指标项
     *
     * @param id 编号
     * @return 检测指标项
     */
    MesQcTemplateIndicatorDO getTemplateIndicator(Long id);

    /**
     * 获得检测指标项分页
     *
     * @param pageReqVO 分页查询
     * @return 检测指标项分页
     */
    PageResult<MesQcTemplateIndicatorDO> getTemplateIndicatorPage(MesQcTemplateIndicatorPageReqVO pageReqVO);

    /**
     * 根据方案编号删除所有检测指标项
     *
     * @param templateId 质检方案编号
     */
    void deleteTemplateIndicatorByTemplateId(Long templateId);

}
