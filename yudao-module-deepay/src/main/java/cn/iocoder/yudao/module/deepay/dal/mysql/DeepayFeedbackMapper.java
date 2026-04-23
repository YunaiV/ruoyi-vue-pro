package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFeedbackDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户反馈学习 Mapper（FeedbackAgent 写入）。
 */
@Mapper
public interface DeepayFeedbackMapper extends BaseMapperX<DeepayFeedbackDO> {

    /** 按图片 ID 查询所有反馈记录。 */
    default List<DeepayFeedbackDO> selectByImageId(Long imageId) {
        return selectList(new LambdaQueryWrapper<DeepayFeedbackDO>()
                .eq(DeepayFeedbackDO::getImageId, imageId));
    }

    /** 统计指定图片被选中的次数。 */
    @Select("SELECT COUNT(*) FROM deepay_feedback WHERE image_id = #{imageId} AND selected = 1")
    int countSelected(Long imageId);

}
