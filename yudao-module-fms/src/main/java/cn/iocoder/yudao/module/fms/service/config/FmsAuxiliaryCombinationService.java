package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;

import java.util.Collection;
import java.util.List;

/**
 * FMS 辅助核算组合 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsAuxiliaryCombinationService {

    /**
     * 为凭证分录获得或创建辅助核算组合
     *
     * @param accountSetId 账套编号
     * @param entries 凭证分录数组
     */
    void setAuxiliaryCombinationIds(Long accountSetId, List<FmsVoucherEntryDO> entries);

    /**
     * 保存辅助核算组合
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @param items 辅助核算项目数组
     * @return 辅助核算组合
     */
    FmsAuxiliaryCombinationDO saveAuxiliaryCombination(Long accountSetId, Long subjectId,
                                                       List<FmsAuxiliaryCombinationDO.AuxiliaryItem> items);

    /**
     * 获得指定科目的辅助核算组合数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 辅助核算组合数量
     */
    Long getAuxiliaryCombinationCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 迁移科目的辅助核算组合
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @param targetSubjectId 目标科目编号
     */
    void updateAuxiliaryCombinationSubject(Long accountSetId, Long subjectId, Long targetSubjectId);

    /**
     * 删除包含指定辅助核算项目的组合
     *
     * @param accountSetId 账套编号
     * @param auxiliaryItemIds 辅助核算项目编号数组
     */
    void deleteAuxiliaryCombinationByAuxiliaryItemIds(Long accountSetId, Collection<Long> auxiliaryItemIds);

}
