package cn.iocoder.yudao.module.crm.service.permission;


import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * crm 数据权限 Service 接口
 *
 * @author HUIHUI
 */
public interface CrmPermissionService {

    /**
     * 创建数据权限
     *
     * @param reqVO  创建信息
     * @param userId 用户编号
     */
    void createPermission(CrmPermissionSaveReqVO reqVO, Long userId);

    /**
     * 创建数据权限
     *
     * @param createReqBO 创建信息
     * @return 编号
     */
    Long createPermission(@Valid CrmPermissionCreateReqBO createReqBO);

    /**
     * 创建数据权限
     *
     * @param createReqBOs 创建信息
     */
    void createPermissionBatch(@Valid List<CrmPermissionCreateReqBO> createReqBOs);

    /**
     * 更新数据权限
     *
     * @param updateReqVO 更新信息
     */
    void updatePermission(CrmPermissionUpdateReqVO updateReqVO);

    /**
     * 数据权限转移
     *
     * @param crmPermissionTransferReqBO 数据权限转移请求
     */
    void transferPermission(@Valid CrmPermissionTransferReqBO crmPermissionTransferReqBO);

    /**
     * 删除数据权限
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param level   数据权限级别，关联 {@link CrmPermissionLevelEnum}
     */
    void deletePermission(Integer bizType, Long bizId, Integer level);

    /**
     * 删除数据权限
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     */
    void deletePermission(Integer bizType, Long bizId);

    /**
     * 批量删除数据权限
     *
     * @param ids    权限编号
     * @param userId 用户编号
     */
    void deletePermissionBatch(Collection<Long> ids, Long userId);

    /**
     * 删除指定用户数据权限
     *
     * @param id     权限编号
     * @param userId 用户编号
     */
    void deleteSelfPermission(Long id, Long userId);

    /**
     * 获取数据权限列表，通过 数据类型 x 某个数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @return Crm 数据权限列表
     */
    List<CrmPermissionDO> getPermissionListByBiz(Integer bizType, Long bizId);

    /**
     * 获取数据权限列表，通过 数据类型 x 某个数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizIds  数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @return Crm 数据权限列表
     */
    List<CrmPermissionDO> getPermissionListByBiz(Integer bizType, Collection<Long> bizIds);

    /**
     * 获取用户参与的模块数据列表
     *
     * @param bizType 模块类型
     * @param userId  用户编号
     * @return 模块数据列表
     */
    List<CrmPermissionDO> getPermissionListByBizTypeAndUserId(Integer bizType, Long userId);

    /**
     * 校验是否有指定数据的操作权限
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId  用户编号
     * @param level   权限级别
     * @return 是否有权限
     */
    boolean hasPermission(Integer bizType, Long bizId, Long userId, CrmPermissionLevelEnum level);

}
