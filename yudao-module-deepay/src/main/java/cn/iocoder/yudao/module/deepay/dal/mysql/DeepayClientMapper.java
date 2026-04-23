package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayClientDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayClientMapper extends BaseMapperX<DeepayClientDO> {

    default DeepayClientDO selectById(Long id) {
        return selectOne(new LambdaQueryWrapper<DeepayClientDO>()
                .eq(DeepayClientDO::getId, id));
    }

    default List<DeepayClientDO> selectByLevel(String level) {
        return selectList(new LambdaQueryWrapper<DeepayClientDO>()
                .eq(DeepayClientDO::getLevel, level)
                .orderByDesc(DeepayClientDO::getTotalOrderAmount));
    }

}
