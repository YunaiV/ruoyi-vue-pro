package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepaySelectionLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选款记录 Mapper — 操作 deepay_selection_log。
 *
 * <p>记录客户每次点选（选中 / 跳过）行为，是"越用越准"的数据基础。</p>
 */
@Mapper
public interface DeepaySelectionLogMapper extends BaseMapperX<DeepaySelectionLogDO> {

    /**
     * 查询指定客户的所有选款记录（时间倒序）。
     */
    default List<DeepaySelectionLogDO> selectByCustomerId(Long customerId) {
        return selectList(new LambdaQueryWrapper<DeepaySelectionLogDO>()
                .eq(DeepaySelectionLogDO::getCustomerId, customerId)
                .orderByDesc(DeepaySelectionLogDO::getCreatedAt));
    }

    /**
     * 查询指定客户 + 品类下的选款记录。
     */
    default List<DeepaySelectionLogDO> selectByCustomerAndCategory(Long customerId, String category) {
        return selectList(new LambdaQueryWrapper<DeepaySelectionLogDO>()
                .eq(DeepaySelectionLogDO::getCustomerId, customerId)
                .eq(category != null, DeepaySelectionLogDO::getCategory, category)
                .orderByDesc(DeepaySelectionLogDO::getCreatedAt));
    }

    /**
     * 统计指定客户选中（is_selected=1）某风格的次数。
     * 用于 FeedbackAgent 动态提升风格权重。
     */
    @Select("SELECT COUNT(*) FROM deepay_selection_log " +
            "WHERE customer_id = #{customerId} AND style = #{style} AND is_selected = 1")
    int countSelectedByStyle(Long customerId, String style);

    /**
     * 查询指定客户最常选中的风格（取最高频 1 条）。
     * FeedbackAgent / CustomerProfileAgent 据此推断偏好风格。
     */
    @Select("SELECT style FROM deepay_selection_log " +
            "WHERE customer_id = #{customerId} AND is_selected = 1 AND style IS NOT NULL " +
            "GROUP BY style ORDER BY COUNT(*) DESC LIMIT 1")
    String selectTopStyleByCustomer(Long customerId);

}
