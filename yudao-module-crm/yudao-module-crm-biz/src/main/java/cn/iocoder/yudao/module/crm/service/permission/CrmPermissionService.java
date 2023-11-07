package cn.iocoder.yudao.module.crm.service.permission;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionPageReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionUpdateReqBO;

import javax.validation.Valid;
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
     * @param createBO 创建信息
     * @return 编号
     */
    Long createPermission(@Valid CrmPermissionCreateReqBO createBO);

    /**
     * 更新数据权限
     *
     * @param updateBO 更新信息
     */
    void updatePermission(@Valid CrmPermissionUpdateReqBO updateBO);

    /**
     * 删除数据权限
     *
     * @param id 编号
     */
    void deletePermission(Long id);

    /**
     * 获取用户数据权限通过 数据类型 x 某个数据 x 用户编号
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId  用户编号，AdminUser#id
     * @return Crm 数据权限
     */
    CrmPermissionDO getPermissionByBizTypeAndBizIdAndUserId(Integer bizType, Long bizId, Long userId);

    /**
     * 获取数据权限列表，通过 数据类型 x 某个数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @return Crm 数据权限列表
     */
    List<CrmPermissionDO> getPermissionByBizTypeAndBizId(Integer bizType, Long bizId);

    /**
     * 数据权限转移
     *
     * @param crmPermissionTransferReqBO 数据权限转移请求
     */
    void transferPermission(@Valid CrmPermissionTransferReqBO crmPermissionTransferReqBO);

    /**
     * 获取数据权限分页数据
     *
     * @param pageReqBO 分页请求
     * @return 数据权限分页数据
     */
    PageResult<CrmPermissionDO> getPermissionPage(CrmPermissionPageReqBO pageReqBO);

}
