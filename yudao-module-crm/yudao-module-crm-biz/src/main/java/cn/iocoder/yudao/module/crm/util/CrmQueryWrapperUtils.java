package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * CRM 分页查询工具类
 *
 * @author HUIHUI
 */
public class CrmQueryWrapperUtils {

    /**
     * 构造 CRM 数据类型数据分页查询条件
     *
     * @param queryMapper 连表查询对象
     * @param bizType     数据类型 {@link CrmBizTypeEnum}
     * @param bizId       数据编号
     * @param userId      用户编号
     * @param sceneType   场景类型
     * @param pool        公海
     */
    public static <T extends MPJLambdaWrapper<?>, S> void builderPageQuery(
            T queryMapper, Integer bizType, SFunction<S, ?> bizId, Long userId, Integer sceneType, Boolean pool) {
        // 1. 构建数据权限连表条件
        if (ObjUtil.notEqual(validateAdminUser(userId), Boolean.TRUE)) { // 管理员不需要数据权限
            queryMapper.innerJoin(CrmPermissionDO.class, on ->
                    on.eq(CrmPermissionDO::getBizType, bizType).eq(CrmPermissionDO::getBizId, bizId)
                            .eq(CrmPermissionDO::getUserId, userId));
        }
        // 1.2 场景一：我负责的数据
        if (CrmSceneTypeEnum.isOwner(sceneType)) {
            queryMapper.eq("owner_user_id", userId);
        }
        // 1.3 场景一：我参与的数据
        if (CrmSceneTypeEnum.isInvolved(sceneType)) {
            queryMapper.ne("owner_user_id", userId);
        }
        // 1.4 场景二：下属负责的数据
        if (CrmSceneTypeEnum.isSubordinate(sceneType)) {
            List<AdminUserRespDTO> subordinateUsers = getAdminUserApi().getUserListBySubordinate(userId);
            if (CollUtil.isNotEmpty(subordinateUsers)) {
                queryMapper.in("owner_user_id", convertSet(subordinateUsers, AdminUserRespDTO::getId));
            }
        }

        // 2. 拼接公海的查询条件
        if (ObjUtil.equal(pool, Boolean.TRUE)) { // 情况一：公海
            queryMapper.isNull("owner_user_id");
        } else { // 情况二：不是公海
            queryMapper.isNotNull("owner_user_id");
        }
    }

    /**
     * 构造 CRM 数据类型批量数据查询条件
     *
     * @param queryMapper 连表查询对象
     * @param bizType     数据类型 {@link CrmBizTypeEnum}
     * @param bizIds      数据编号
     * @param userId      用户编号
     */
    public static <T extends MPJLambdaWrapper<?>, S> void builderListQueryBatch(
            T queryMapper, Integer bizType, Collection<Long> bizIds, Long userId) {
        // 1. 构建数据权限连表条件
        if (ObjUtil.notEqual(validateAdminUser(userId), Boolean.TRUE)) { // 管理员不需要数据权限
            queryMapper.innerJoin(CrmPermissionDO.class, on ->
                    on.eq(CrmPermissionDO::getBizType, bizType).in(CrmPermissionDO::getBizId, bizIds)
                            .eq(CrmPermissionDO::getUserId, userId));
        }
    }

    private static AdminUserApi getAdminUserApi() {
        return SpringUtil.getBean(AdminUserApi.class);
    }

    /**
     * 校验用户是否是管理员
     *
     * @param userId 用户编号
     * @return 是/否
     */
    private static boolean validateAdminUser(Long userId) {
        return false;
    }

}
