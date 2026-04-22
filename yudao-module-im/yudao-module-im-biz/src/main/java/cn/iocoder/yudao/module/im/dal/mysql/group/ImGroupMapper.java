package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IM 群 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMapper extends BaseMapperX<ImGroupDO> {

    /**
     * 根据群编号批量查询群，支持按状态和封禁过滤
     *
     * @param ids 群编号集合
     * @param status   群状态（可空，空则不过滤）
     * @param banned   是否封禁（可空，空则不过滤）
     * @return 群列表
     */
    default List<ImGroupDO> selectListByIds(Collection<Long> ids, Integer status, Boolean banned) {
        return selectList(new LambdaQueryWrapperX<ImGroupDO>()
                .in(ImGroupDO::getId, ids)
                .eqIfPresent(ImGroupDO::getStatus, status)
                .eqIfPresent(ImGroupDO::getBanned, banned));
    }

}