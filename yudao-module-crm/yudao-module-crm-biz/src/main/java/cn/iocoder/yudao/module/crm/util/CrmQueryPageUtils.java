package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneEnum;
import cn.iocoder.yudao.module.crm.framework.vo.CrmBasePageReqVO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * CRM 分页查询工具类
 *
 * @author HUIHUI
 */
public class CrmQueryPageUtils {

    // TODO @puhui999：是不是弱化 CrmBasePageReqVO，把 sceneType 作为参数传入。默认其实 pool 不一定要传递的
    /**
     * 构造 CRM 数据类型数据分页查询条件
     *
     * @param queryMapper    连表查询对象
     * @param reqVO          查询条件
     * @param userId         用户编号
     * @param bizType        数据类型 {@link CrmBizTypeEnum}
     * @param bizId          数据编号
     * @param subordinateIds 下属用户编号，可为空
     */
    public static <T extends MPJLambdaWrapper<?>, V extends CrmBasePageReqVO, S> void builderQuery(
            T queryMapper, V reqVO, Long userId,
            Integer bizType, SFunction<S, ?> bizId,
            @Nullable Collection<Long> subordinateIds, // TODO @puhui999：subordinateIds 可以优化成 subordinateUserIds
            Boolean isAdmin) {
        // TODO @puhui999：是不是特殊处理，让这个 util 有状态，这样 isAdmin 直接从这里读取；subordinateIds 也是；这样，可以简化参数；
        // 1. 构建数据权限连表条件
        if (ObjUtil.notEqual(isAdmin, Boolean.TRUE)) { // 管理员不需要数据权限
            queryMapper.innerJoin(CrmPermissionDO.class, on ->
                    on.eq(CrmPermissionDO::getBizType, bizType).eq(CrmPermissionDO::getBizId, bizId)
                            .eq(CrmPermissionDO::getUserId, userId));
        }
        // 2. 拼接公海的查询条件
        if (ObjUtil.equal(reqVO.getPool(), Boolean.TRUE)) { // 情况一：公海
            queryMapper.isNull("owner_user_id");
        } else { // 情况二：不是公海
            queryMapper.isNotNull("owner_user_id");
        }
        // 3. 拼接场景的查询条件
        // TODO @puhui999：:1 处的数据权限，应该和 3 处的场景是结合的？
        // null 时：一种处理
        // 1：一种条件；
        // 2：一种条件；
        // 3：一种条件；
        if (CrmSceneEnum.isOwner(reqVO.getSceneType())) { // 场景一：我负责的数据
            queryMapper.eq("owner_user_id", userId);
        }
        // TODO puhui999: 这里有一个疑问：如果下属负责的数据权限中没有自己的话还能看吗？回复：不能
        if (CrmSceneEnum.isSubordinate(reqVO.getSceneType()) && CollUtil.isNotEmpty(subordinateIds)) { // 场景三：下属负责的数据
            queryMapper.in("owner_user_id", subordinateIds);
        }
    }

}
