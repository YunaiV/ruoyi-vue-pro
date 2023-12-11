package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * CRM 查询工具类
 *
 * @author HUIHUI
 */
public class CrmQueryWrapperUtils {

    /**
     * 构造 CRM 数据类型数据分页查询条件
     *
     * @param query     连表查询对象
     * @param bizType   数据类型 {@link CrmBizTypeEnum}
     * @param bizId     数据编号
     * @param userId    用户编号
     * @param sceneType 场景类型
     * @param pool      公海
     */
    // TODO @puhui999：bizId 直接传递会不会简单点 回复：还是需要 SFunction 因为分页连表时不知道 bizId 是多少
    public static <T extends MPJLambdaWrapper<?>, S> void appendPermissionCondition(T query, Integer bizType, SFunction<S, ?> bizId,
                                                                                    Long userId, Integer sceneType, Boolean pool) {
        // 1. 构建数据权限连表条件
        if (ObjUtil.notEqual(validateAdminUser(userId), Boolean.TRUE)) { // 管理员不需要数据权限
            query.innerJoin(CrmPermissionDO.class, on ->
                    on.eq(CrmPermissionDO::getBizType, bizType).eq(CrmPermissionDO::getBizId, bizId)
                            .eq(CrmPermissionDO::getUserId, userId));
        }
        // 2.1 场景一：我负责的数据
        if (CrmSceneTypeEnum.isOwner(sceneType)) {
            query.eq("owner_user_id", userId);
        }
        // 2.2 场景二：我参与的数据
        if (CrmSceneTypeEnum.isInvolved(sceneType)) {
            query
                    .ne("owner_user_id", userId)
                    .and(q -> q.eq(CrmPermissionDO::getLevel, CrmPermissionLevelEnum.READ.getLevel())
                            .or()
                            .eq(CrmPermissionDO::getLevel, CrmPermissionLevelEnum.WRITE.getLevel()));

        }
        // 2.3 场景三：下属负责的数据
        if (CrmSceneTypeEnum.isSubordinate(sceneType)) {
            List<AdminUserRespDTO> subordinateUsers = getAdminUserApi().getUserListBySubordinate(userId);
            // TODO @puhui999：如果为空，不拼接，就是查询了所有数据呀？
            if (CollUtil.isNotEmpty(subordinateUsers)) {
                query.in("owner_user_id", convertSet(subordinateUsers, AdminUserRespDTO::getId));
            }
        }

        // 3. 拼接公海的查询条件
        if (ObjUtil.equal(pool, Boolean.TRUE)) { // 情况一：公海
            query.isNull("owner_user_id");
        } else { // 情况二：不是公海
            query.isNotNull("owner_user_id");
        }
    }

    /**
     * 构造 CRM 数据类型批量数据查询条件
     *
     * @param query   连表查询对象
     * @param bizType 数据类型 {@link CrmBizTypeEnum}
     * @param bizIds  数据编号
     * @param userId  用户编号
     */
    public static <T extends MPJLambdaWrapper<?>> void appendPermissionCondition(T query, Integer bizType, Collection<Long> bizIds, Long userId) {
        if (ObjUtil.equal(validateAdminUser(userId), Boolean.TRUE)) {// 管理员不需要数据权限
            return;
        }

        query.innerJoin(CrmPermissionDO.class, on ->
                on.eq(CrmPermissionDO::getBizType, bizType).in(CrmPermissionDO::getBizId, bizIds)
                        .in(CollUtil.isNotEmpty(bizIds), CrmPermissionDO::getUserId, userId));
    }

    private static AdminUserApi getAdminUserApi() {
        return AdminUserApiHolder.ADMIN_USER_API;
    }

    // TODO @puhui999：需要实现；

    /**
     * 校验用户是否是管理员
     *
     * @param userId 用户编号
     * @return 是/否
     */
    private static boolean validateAdminUser(Long userId) {
        return false;
    }

    /**
     * 静态内部类实现 AdminUserApi 单例获取
     *
     * @author HUIHUI
     */
    private static class AdminUserApiHolder {

        private static final AdminUserApi ADMIN_USER_API = SpringUtil.getBean(AdminUserApi.class);

    }

}
