package cn.iocoder.yudao.module.promotion.dal.mysql.decorate;

import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.DecorateComponentDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DecorateComponentMapper extends BaseMapperX<DecorateComponentDO> {

    default List<DecorateComponentDO> selectByPage(Integer pageId){
        return selectList(DecorateComponentDO::getPageId, pageId);
    }

}




