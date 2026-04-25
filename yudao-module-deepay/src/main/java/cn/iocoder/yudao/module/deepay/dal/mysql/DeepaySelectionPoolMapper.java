package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepaySelectionPoolDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 选款参考池 Mapper。
 */
@Mapper
public interface DeepaySelectionPoolMapper extends BaseMapperX<DeepaySelectionPoolDO> {

    /** 查询指定品类的 Top-N 款式（按热度分降序）。 */
    default List<DeepaySelectionPoolDO> selectTopByCategory(String category, int limit) {
        return selectList(new LambdaQueryWrapper<DeepaySelectionPoolDO>()
                .eq(category != null, DeepaySelectionPoolDO::getCategory, category)
                .orderByDesc(DeepaySelectionPoolDO::getScore)
                .last("LIMIT " + limit));
    }

}
