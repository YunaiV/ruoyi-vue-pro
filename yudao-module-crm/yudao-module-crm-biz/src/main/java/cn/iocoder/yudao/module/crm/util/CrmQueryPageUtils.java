package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.concerned.CrmConcernedDO;
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

    /**
     * 构造 crm 数据类型数据分页查询条件
     *
     * @param queryMapper    连表查询对象
     * @param reqVO          查询条件
     * @param userId         用户编号
     * @param bizType        数据类型 {@link CrmBizTypeEnum}
     * @param bizId          数据编号
     * @param subordinateIds 下属用户编号，可为空
     */
    public static <T extends MPJLambdaWrapper<?>, V extends CrmBasePageReqVO, S> void builderQuery(T queryMapper, V reqVO, Long userId,
                                                                                                   Integer bizType, SFunction<S, ?> bizId,
                                                                                                   @Nullable Collection<Long> subordinateIds,
                                                                                                   Boolean isAdmin) {
        // 构建数据权限连表条件
        if (ObjUtil.notEqual(isAdmin, Boolean.TRUE)) { // 管理员不需要数据权限
            queryMapper.innerJoin(CrmPermissionDO.class, on ->
                    on.eq(CrmPermissionDO::getBizType, bizType).eq(CrmPermissionDO::getBizId, bizId)
                            .eq(CrmPermissionDO::getUserId, userId));
        }
        if (ObjUtil.equal(reqVO.getPool(), Boolean.TRUE)) { // 情况一：公海
            queryMapper.isNull("owner_user_id");
        } else { // 情况二：不是公海
            queryMapper.isNotNull("owner_user_id");
        }
        // 场景数据过滤
        if (CrmSceneEnum.isOwner(reqVO.getSceneType())) { // 场景一：我负责的数据
            queryMapper.eq("owner_user_id", userId);
        }
        if (CrmSceneEnum.isFollow(reqVO.getSceneType())) { // 场景二：我关注的数据
            queryMapper.innerJoin(CrmConcernedDO.class, on ->
                    on.eq(CrmConcernedDO::getBizType, bizType).eq(CrmConcernedDO::getBizId, bizId)
                            .eq(CrmConcernedDO::getUserId, userId));
        }
        // TODO puhui999: 这里有一个疑问：如果下属负责的数据权限中没有自己的话还能看吗？
        if (CrmSceneEnum.isSubordinate(reqVO.getSceneType()) && CollUtil.isNotEmpty(subordinateIds)) { // 场景三：下属负责的数据
            queryMapper.in("owner_user_id", subordinateIds);
        }
    }

}
