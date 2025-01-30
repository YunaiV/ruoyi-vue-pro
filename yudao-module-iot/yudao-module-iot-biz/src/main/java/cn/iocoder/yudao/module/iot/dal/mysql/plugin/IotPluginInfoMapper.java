package cn.iocoder.yudao.module.iot.dal.mysql.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.info.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IotPluginInfoMapper extends BaseMapperX<IotPluginInfoDO> {

    default PageResult<IotPluginInfoDO> selectPage(PluginInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotPluginInfoDO>()
                .likeIfPresent(IotPluginInfoDO::getName, reqVO.getName())
                .eqIfPresent(IotPluginInfoDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotPluginInfoDO::getId));
    }

    default List<IotPluginInfoDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotPluginInfoDO>()
                .eq(IotPluginInfoDO::getStatus, status)
                .orderByAsc(IotPluginInfoDO::getId));
    }

    default IotPluginInfoDO selectByPluginKey(String pluginKey) {
        return selectOne(IotPluginInfoDO::getPluginKey, pluginKey);
    }

}