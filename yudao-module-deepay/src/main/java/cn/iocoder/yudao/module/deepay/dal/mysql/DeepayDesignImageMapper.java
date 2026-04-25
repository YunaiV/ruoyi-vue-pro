package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 设计图评分 Mapper（ImageScoringAgent 写入 / FeedbackAgent 更新）。
 */
@Mapper
public interface DeepayDesignImageMapper extends BaseMapperX<DeepayDesignImageDO> {

    /** 查询指定品类的 Top-N 图片（按综合分降序）。 */
    default List<DeepayDesignImageDO> selectTopByCategory(String category, int limit) {
        return selectList(new LambdaQueryWrapper<DeepayDesignImageDO>()
                .eq(category != null, DeepayDesignImageDO::getCategory, category)
                .orderByDesc(DeepayDesignImageDO::getScore)
                .last("LIMIT " + limit));
    }

    /**
     * 推荐查询（STEP 29）：按品类 + 风格过滤，评分降序，支持个性化推荐。
     * category 或 style 为 null 时忽略对应条件。
     */
    default List<DeepayDesignImageDO> selectRecommend(String category, String style, int limit) {
        return selectList(new LambdaQueryWrapper<DeepayDesignImageDO>()
                .eq(category != null, DeepayDesignImageDO::getCategory, category)
                .eq(style != null, DeepayDesignImageDO::getStyle, style)
                .orderByDesc(DeepayDesignImageDO::getScore)
                .last("LIMIT " + limit));
    }

    /** 原子递增 view_count（图片被展示时调用）。 */
    default void incrementViewCount(String imageUrl) {
        update(null, new LambdaUpdateWrapper<DeepayDesignImageDO>()
                .eq(DeepayDesignImageDO::getUrl, imageUrl)
                .setSql("view_count = COALESCE(view_count, 0) + 1"));
    }

    /** 原子递增 click_count（图片被用户选中时调用）。 */
    default void incrementClickCount(String imageUrl) {
        update(null, new LambdaUpdateWrapper<DeepayDesignImageDO>()
                .eq(DeepayDesignImageDO::getUrl, imageUrl)
                .setSql("click_count = COALESCE(click_count, 0) + 1"));
    }

    /** 原子递增 order_count（该图片关联订单支付成功时调用）。 */
    default void incrementOrderCount(String imageUrl) {
        update(null, new LambdaUpdateWrapper<DeepayDesignImageDO>()
                .eq(DeepayDesignImageDO::getUrl, imageUrl)
                .setSql("order_count = COALESCE(order_count, 0) + 1"));
    }

}
