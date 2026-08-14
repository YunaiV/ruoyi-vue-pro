package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplatecategory.FmsVoucherTemplateCategorySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplate.FmsVoucherTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateCategoryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * FMS 凭证模板 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsVoucherTemplateService {

    /**
     * 创建凭证模板分类
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 分类编号
     */
    Long createTemplateCategory(FmsVoucherTemplateCategorySaveReqVO createReqVO, Long userId);

    /**
     * 更新凭证模板分类
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateTemplateCategory(FmsVoucherTemplateCategorySaveReqVO updateReqVO, Long userId);

    /**
     * 删除凭证模板分类
     *
     * @param accountSetId 账套编号
     * @param id 分类编号
     * @param userId 用户编号
     */
    void deleteTemplateCategory(Long accountSetId, Long id, Long userId);

    /**
     * 获得凭证模板分类列表
     *
     * @param accountSetId 账套编号
     * @return 凭证模板分类列表
     */
    List<FmsVoucherTemplateCategoryDO> getTemplateCategoryList(Long accountSetId);

    /**
     * 获得凭证模板分类 Map
     *
     * @param accountSetId 账套编号
     * @return 凭证模板分类 Map
     */
    default Map<Long, FmsVoucherTemplateCategoryDO> getTemplateCategoryMap(Long accountSetId) {
        return convertMap(getTemplateCategoryList(accountSetId), FmsVoucherTemplateCategoryDO::getId);
    }

    /**
     * 创建凭证模板
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 模板编号
     */
    Long createVoucherTemplate(FmsVoucherTemplateSaveReqVO createReqVO, Long userId);

    /**
     * 更新凭证模板
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateVoucherTemplate(FmsVoucherTemplateSaveReqVO updateReqVO, Long userId);

    /**
     * 删除凭证模板
     *
     * @param accountSetId 账套编号
     * @param id 模板编号
     * @param userId 用户编号
     */
    void deleteVoucherTemplate(Long accountSetId, Long id, Long userId);

    /**
     * 获得凭证模板列表
     *
     * @param accountSetId 账套编号
     * @return 凭证模板列表
     */
    List<FmsVoucherTemplateDO> getVoucherTemplateList(Long accountSetId);

    /**
     * 获得使用指定科目的凭证模板数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 凭证模板数量
     */
    Long getVoucherTemplateCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 获得使用指定辅助核算项目的凭证模板数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryItemIds 辅助核算项目编号数组
     * @return 凭证模板数量
     */
    Long getVoucherTemplateCountByAuxiliaryItemIds(
            Long accountSetId, Collection<Long> auxiliaryItemIds);

    /**
     * 获得使用指定辅助核算类别的凭证模板数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @return 凭证模板数量
     */
    Long getVoucherTemplateCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId);

}
