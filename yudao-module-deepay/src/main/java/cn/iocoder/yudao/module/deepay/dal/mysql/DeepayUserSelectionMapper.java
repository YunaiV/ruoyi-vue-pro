package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserSelectionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户选择记录 Mapper。
 */
@Mapper
public interface DeepayUserSelectionMapper extends BaseMapperX<DeepayUserSelectionDO> {

    /**
     * 查询某用户最近 N 条选择记录（按时间倒序）。
     * 供 TrendAgent 优先排序时使用。
     */
    default List<DeepayUserSelectionDO> selectRecentByUserId(String userId, int limit) {
        return selectList(new LambdaQueryWrapper<DeepayUserSelectionDO>()
                .eq(DeepayUserSelectionDO::getUserId, userId)
                .orderByDesc(DeepayUserSelectionDO::getCreatedAt)
                .last("LIMIT " + limit));
    }

    /**
     * 查询某用户在指定品类下的选择次数（用于置信度参考）。
     */
    @Select("SELECT COUNT(*) FROM deepay_user_selection " +
            "WHERE user_id = #{userId} AND category = #{category}")
    int countByUserIdAndCategory(String userId, String category);

    /**
     * 查询某用户最常选的风格（最多 1 个）。
     */
    @Select("SELECT style FROM deepay_user_selection " +
            "WHERE user_id = #{userId} AND style IS NOT NULL " +
            "GROUP BY style ORDER BY COUNT(*) DESC LIMIT 1")
    String selectTopStyleByUserId(String userId);

}
