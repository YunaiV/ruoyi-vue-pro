package cn.iocoder.yudao.module.fms.service.voucher;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherAttachmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportTemplateVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherMoveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectAmountVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherTidyReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * FMS 凭证 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsVoucherService {

    /**
     * 创建凭证
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 凭证编号
     */
    Long createVoucher(FmsVoucherSaveReqVO createReqVO, Long userId);

    /**
     * 更新凭证
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateVoucher(FmsVoucherSaveReqVO updateReqVO, Long userId);

    /**
     * 更新凭证附件
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateVoucherAttachments(FmsVoucherAttachmentUpdateReqVO updateReqVO, Long userId);

    /**
     * 删除凭证
     *
     * @param accountSetId 账套编号
     * @param ids 凭证编号数组
     * @param userId 用户编号
     */
    void deleteVoucherList(Long accountSetId, List<Long> ids, Long userId);

    /**
     * 更新凭证审核状态
     *
     * @param accountSetId 账套编号
     * @param ids 凭证编号数组
     * @param status 审核状态
     * @param userId 用户编号
     */
    void updateVoucherReviewStatus(Long accountSetId, List<Long> ids, Integer status, Long userId);

    /**
     * 整理凭证号
     *
     * @param tidyReqVO 整理信息
     * @param userId 用户编号
     */
    void tidyVoucher(FmsVoucherTidyReqVO tidyReqVO, Long userId);

    /**
     * 移动凭证号
     *
     * @param moveReqVO 移动信息
     * @param userId 用户编号
     */
    void moveVoucher(FmsVoucherMoveReqVO moveReqVO, Long userId);

    /**
     * 获得凭证
     *
     * @param accountSetId 账套编号
     * @param id 凭证编号
     * @param userId 用户编号
     * @return 凭证
     */
    FmsVoucherDO getVoucher(Long accountSetId, Long id, Long userId);

    /**
     * 获得凭证分页
     *
     * @param pageReqVO 分页查询条件
     * @param userId 用户编号
     * @return 凭证分页
     */
    PageResult<FmsVoucherDO> getVoucherPage(FmsVoucherPageReqVO pageReqVO, Long userId);

    /**
     * 获得指定期间的凭证列表
     *
     * @param accountSetId 账套编号
     * @param beginTime 开始时间
     * @param endTime 结束时间，不包含
     * @return 凭证列表
     */
    List<FmsVoucherDO> getVoucherListByPeriod(Long accountSetId, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得凭证分录列表
     *
     * @param voucherIds 凭证编号数组
     * @return 凭证分录列表
     */
    List<FmsVoucherEntryDO> getVoucherEntryList(Collection<Long> voucherIds);

    /**
     * 获得指定凭证的科目发生额
     *
     * @param voucherIds 凭证编号数组
     * @return 科目发生额列表
     */
    List<FmsVoucherSubjectAmountVO> getVoucherSubjectAmountList(Collection<Long> voucherIds);

    /**
     * 获得凭证分录 Map
     *
     * @param voucherIds 凭证编号数组
     * @return 凭证分录 Map
     */
    default Map<Long, List<FmsVoucherEntryDO>> getVoucherEntryMap(Collection<Long> voucherIds) {
        return convertMultiMap(getVoucherEntryList(voucherIds), FmsVoucherEntryDO::getVoucherId);
    }

    /**
     * 获得使用指定科目的凭证分录数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 凭证分录数量
     */
    Long getVoucherEntryCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 获得指定科目包含数量数据的凭证分录数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 凭证分录数量
     */
    Long getVoucherEntryQuantityCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 迁移科目的凭证分录
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @param targetSubject 目标科目
     */
    void updateVoucherEntrySubject(Long accountSetId, Long subjectId, FmsSubjectDO targetSubject);

    /**
     * 迁移科目凭证分录的辅助核算
     *
     * @param accountSetId 账套编号
     * @param subjectId 科目编号
     * @param combination 辅助核算组合
     */
    void migrateVoucherEntryAuxiliaries(Long accountSetId, Long subjectId,
                                        FmsAuxiliaryCombinationDO combination);

    /**
     * 获得使用指定辅助核算项目的凭证分录数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryItemIds 辅助核算项目编号数组
     * @return 凭证分录数量
     */
    Long getVoucherEntryCountByAuxiliaryItemIds(Long accountSetId, Collection<Long> auxiliaryItemIds);

    /**
     * 获得使用指定辅助核算类别的凭证分录数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @return 凭证分录数量
     */
    Long getVoucherEntryCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId);

    /**
     * 获得使用指定凭证字的凭证数量
     *
     * @param accountSetId 账套编号
     * @param voucherWordId 凭证字编号
     * @return 凭证数量
     */
    Long getVoucherCountByVoucherWordId(Long accountSetId, Long voucherWordId);

    /**
     * 获得下一个凭证号
     *
     * @param accountSetId 账套编号
     * @param voucherWordId 凭证字编号
     * @param voucherTime 凭证日期
     * @param userId 用户编号
     * @return 下一个凭证号
     */
    Integer getNextVoucherNumber(Long accountSetId, Long voucherWordId, LocalDateTime voucherTime, Long userId);

    /**
     * 获得凭证导入模板数据
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 导入模板数据
     */
    FmsVoucherImportTemplateVO getVoucherImportTemplateData(Long accountSetId, Long userId);

    /**
     * 导入凭证
     *
     * @param accountSetId 账套编号
     * @param rows 导入分录数组
     * @param userId 用户编号
     * @return 导入结果
     */
    FmsVoucherImportRespVO importVoucher(Long accountSetId, List<FmsVoucherImportExcelVO> rows, Long userId);

    /**
     * 获得凭证汇总
     *
     * @param queryReqVO 查询条件
     * @param userId 用户编号
     * @return 凭证汇总
     */
    List<FmsVoucherStatisticsRespVO> getVoucherStatisticsList(
            FmsVoucherStatisticsReqVO queryReqVO, Long userId);

}
