package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsWorkItemLabelMapper extends BaseMapperX<PmsWorkItemLabelDO> {

    default List<PmsWorkItemLabelDO> selectListByName(String name) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemLabelDO>()
                .likeIfPresent(PmsWorkItemLabelDO::getName, name)
                .orderByAsc(PmsWorkItemLabelDO::getCreateTime)
                .orderByAsc(PmsWorkItemLabelDO::getId));
    }

}
