package cn.iocoder.yudao.module.promotion.dal.mysql.decorate;

import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.PageDecorateDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PageDecorateMapper extends BaseMapperX<PageDecorateDO> {
    default List<PageDecorateDO> selectByPageType(Integer type){
        return selectList(PageDecorateDO::getType, type);
    }
}




