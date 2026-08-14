package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;

import java.util.List;
import java.util.Collection;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * FMS 辅助核算项目 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsAuxiliaryItemService {

    /**
     * 创建辅助核算项目
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 编号
     */
    Long createAuxiliaryItem(FmsAuxiliaryItemSaveReqVO createReqVO, Long userId);

    /**
     * 更新辅助核算项目
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateAuxiliaryItem(FmsAuxiliaryItemSaveReqVO updateReqVO, Long userId);

    /**
     * 删除辅助核算项目
     *
     * @param accountSetId 账套编号
     * @param ids 编号数组
     * @param userId 当前用户编号
     */
    void deleteAuxiliaryItemList(Long accountSetId, List<Long> ids, Long userId);

    /**
     * 更新辅助核算项目状态
     *
     * @param accountSetId 账套编号
     * @param id 编号
     * @param status 状态
     * @param userId 当前用户编号
     */
    void updateAuxiliaryItemStatus(Long accountSetId, Long id, Integer status, Long userId);

    /**
     * 导入辅助核算项目
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @param importItems 导入项目数组
     * @param userId 当前用户编号
     * @return 导入结果
     */
    FmsAuxiliaryItemImportRespVO importAuxiliaryItemList(Long accountSetId, Long auxiliaryTypeId,
            List<FmsAuxiliaryItemImportExcelVO> importItems, Long userId);

    /**
     * 获得辅助核算项目分页
     *
     * @param pageReqVO 分页条件
     * @param userId 当前用户编号
     * @return 辅助核算项目分页
     */
    PageResult<FmsAuxiliaryItemDO> getAuxiliaryItemPage(
            FmsAuxiliaryItemPageReqVO pageReqVO, Long userId);

    /**
     * 获得辅助核算项目
     *
     * @param accountSetId 账套编号
     * @param id 辅助核算项目编号
     * @param userId 当前用户编号
     * @return 辅助核算项目
     */
    FmsAuxiliaryItemDO getAuxiliaryItem(Long accountSetId, Long id, Long userId);

    /**
     * 获得辅助核算项目列表
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @param userId 当前用户编号
     * @return 辅助核算项目列表
     */
    List<FmsAuxiliaryItemDO> getAuxiliaryItemList(
            Long accountSetId, Long auxiliaryTypeId, Long userId);

    /**
     * 获得指定状态的辅助核算项目列表
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @param status 状态
     * @param userId 当前用户编号
     * @return 辅助核算项目列表
     */
    List<FmsAuxiliaryItemDO> getAuxiliaryItemListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
            Long accountSetId, Long auxiliaryTypeId, Integer status, Long userId);

    /**
     * 获得账套的全部辅助核算项目
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 辅助核算项目列表
     */
    List<FmsAuxiliaryItemDO> getAuxiliaryItemListByAccountSetId(Long accountSetId, Long userId);

    /**
     * 获得账套的辅助核算项目 Map
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 辅助核算项目 Map，键为“类别编号|项目编码”
     */
    default Map<String, FmsAuxiliaryItemDO> getAuxiliaryItemMapByTypeIdAndCode(
            Long accountSetId, Long userId) {
        return convertMap(getAuxiliaryItemListByAccountSetId(accountSetId, userId),
                item -> item.getAuxiliaryTypeId() + "|" + item.getCode());
    }

    /**
     * 获得指定辅助核算类别的项目数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @return 辅助核算项目数量
     */
    Long getAuxiliaryItemCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId);

    /**
     * 校验并获得辅助核算项目列表
     *
     * @param accountSetId 账套编号
     * @param ids 辅助核算项目编号数组
     * @return 辅助核算项目列表
     */
    List<FmsAuxiliaryItemDO> validateAuxiliaryItemList(Long accountSetId, Collection<Long> ids);

}
