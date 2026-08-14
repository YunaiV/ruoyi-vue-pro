package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype.FmsAuxiliaryTypeSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * FMS 辅助核算类别 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsAuxiliaryTypeService {

    /**
     * 初始化系统预置辅助核算类别
     *
     * @param accountSetId 账套编号
     */
    void initializeDefaultTypes(Long accountSetId);

    /**
     * 创建辅助核算类别
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 编号
     */
    Long createAuxiliaryType(FmsAuxiliaryTypeSaveReqVO createReqVO, Long userId);

    /**
     * 更新辅助核算类别
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateAuxiliaryType(FmsAuxiliaryTypeSaveReqVO updateReqVO, Long userId);

    /**
     * 删除辅助核算类别
     *
     * @param accountSetId 账套编号
     * @param id 编号
     * @param userId 当前用户编号
     */
    void deleteAuxiliaryType(Long accountSetId, Long id, Long userId);

    /**
     * 获得辅助核算类别
     *
     * @param accountSetId 账套编号
     * @param id 辅助核算类别编号
     * @param userId 当前用户编号
     * @return 辅助核算类别
     */
    FmsAuxiliaryTypeDO getAuxiliaryType(Long accountSetId, Long id, Long userId);

    /**
     * 获得辅助核算类别列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 辅助核算类别列表
     */
    List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Long accountSetId, Long userId);

    /**
     * 获得账套的辅助核算类别列表
     *
     * @param accountSetId 账套编号
     * @return 辅助核算类别列表
     */
    List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Long accountSetId);

    /**
     * 获得辅助核算类别列表
     *
     * @param ids 辅助核算类别编号数组
     * @return 辅助核算类别列表
     */
    List<FmsAuxiliaryTypeDO> getAuxiliaryTypeList(Collection<Long> ids);

    /**
     * 获得辅助核算类别 Map
     *
     * @param ids 辅助核算类别编号数组
     * @return 辅助核算类别 Map
     */
    default Map<Long, FmsAuxiliaryTypeDO> getAuxiliaryTypeMap(Collection<Long> ids) {
        return convertMap(getAuxiliaryTypeList(ids), FmsAuxiliaryTypeDO::getId);
    }

    /**
     * 获得账套的辅助核算类别 Map
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 辅助核算类别 Map
     */
    default Map<Long, FmsAuxiliaryTypeDO> getAuxiliaryTypeMap(Long accountSetId, Long userId) {
        return convertMap(getAuxiliaryTypeList(accountSetId, userId), FmsAuxiliaryTypeDO::getId);
    }

    /**
     * 获得辅助核算类别名称与编号 Map
     *
     * @param accountSetId 账套编号
     * @return 辅助核算类别名称与编号 Map
     */
    default Map<String, Long> getAuxiliaryTypeIdMap(Long accountSetId) {
        return convertMap(getAuxiliaryTypeList(accountSetId),
                FmsAuxiliaryTypeDO::getName, FmsAuxiliaryTypeDO::getId);
    }

    /**
     * 校验并获得辅助核算类别列表
     *
     * @param ids 辅助核算类别编号数组
     * @return 辅助核算类别列表
     */
    List<FmsAuxiliaryTypeDO> validateAuxiliaryTypeList(Collection<Long> ids);

    /**
     * 校验并获得辅助核算类别列表
     *
     * @param accountSetId 账套编号
     * @param ids 辅助核算类别编号数组
     * @return 辅助核算类别列表
     */
    List<FmsAuxiliaryTypeDO> validateAuxiliaryTypeList(Long accountSetId, Collection<Long> ids);

}
