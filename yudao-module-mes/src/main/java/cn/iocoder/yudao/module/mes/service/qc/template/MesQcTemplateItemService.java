package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.item.MesQcTemplateItemPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.item.MesQcTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import jakarta.validation.Valid;

/**
 * MES 质检方案-产品关联 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcTemplateItemService {

    /**
     * 创建产品关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTemplateItem(@Valid MesQcTemplateItemSaveReqVO createReqVO);

    /**
     * 更新产品关联
     *
     * @param updateReqVO 更新信息
     */
    void updateTemplateItem(@Valid MesQcTemplateItemSaveReqVO updateReqVO);

    /**
     * 删除产品关联
     *
     * @param id 编号
     */
    void deleteTemplateItem(Long id);

    /**
     * 获得产品关联
     *
     * @param id 编号
     * @return 产品关联
     */
    MesQcTemplateItemDO getTemplateItem(Long id);

    /**
     * 获得产品关联分页
     *
     * @param pageReqVO 分页查询
     * @return 产品关联分页
     */
    PageResult<MesQcTemplateItemDO> getTemplateItemPage(MesQcTemplateItemPageReqVO pageReqVO);

    /**
     * 根据方案编号删除所有产品关联
     *
     * @param templateId 质检方案编号
     */
    void deleteTemplateItemByTemplateId(Long templateId);

    /**
     * 根据产品物料 ID 和 QC 类型查找检验模板的产品关联（必须存在，否则抛异常）
     *
     * @param itemId 产品物料 ID
     * @param qcType 检测种类（枚举 {@link MesQcTypeEnum}）
     * @return 匹配的产品关联
     */
    MesQcTemplateItemDO getRequiredTemplateByItemIdAndType(Long itemId, Integer qcType);

}
