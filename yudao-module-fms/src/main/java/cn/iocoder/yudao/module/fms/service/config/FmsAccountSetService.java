package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * FMS 账套 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsAccountSetService {

    /**
     * 创建账套
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 账套编号
     */
    Long createAccountSet(FmsAccountSetSaveReqVO createReqVO, Long userId);

    /**
     * 更新账套
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateAccountSet(FmsAccountSetSaveReqVO updateReqVO, Long userId);

    /**
     * 更新账套会计制度
     *
     * @param accountSetId 账套编号
     * @param standard 会计制度
     * @param userId 用户编号
     */
    void updateAccountSetStandard(Long accountSetId, Integer standard, Long userId);

    /**
     * 初始化账套
     *
     * @param initializeReqVO 初始化信息
     * @param userId 当前用户编号
     */
    void initializeAccountSet(FmsAccountSetInitializeReqVO initializeReqVO, Long userId);

    /**
     * 获得账套
     *
     * @param id 账套编号
     * @return 账套
     */
    FmsAccountSetDO getAccountSet(Long id);

    /**
     * 校验账套存在并返回账套
     *
     * @param id 账套编号
     * @return 账套
     */
    FmsAccountSetDO validateAccountSetExists(Long id);

    /**
     * 校验账套读取权限
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 账套
     */
    FmsAccountSetDO validateAccountSetReadPermission(Long accountSetId, Long userId);

    /**
     * 校验账套写入权限
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 账套
     */
    FmsAccountSetDO validateAccountSetWritePermission(Long accountSetId, Long userId);

    /**
     * 校验账套主管权限
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 账套
     */
    FmsAccountSetDO validateAccountSetOwnerPermission(Long accountSetId, Long userId);

    /**
     * 锁定账套，使同一账套的关键写操作串行执行
     *
     * @param accountSetId 账套编号
     */
    void lockAccountSet(Long accountSetId);

    /**
     * 获得指定账套列表
     *
     * @param ids 账套编号数组
     * @return 账套列表
     */
    List<FmsAccountSetDO> getAccountSetList(Collection<Long> ids);

    /**
     * 获得指定账套 Map
     *
     * @param ids 账套编号数组
     * @return 账套 Map
     */
    default Map<Long, FmsAccountSetDO> getAccountSetMap(Collection<Long> ids) {
        return convertMap(getAccountSetList(ids), FmsAccountSetDO::getId);
    }

}
