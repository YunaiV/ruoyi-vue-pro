package cn.iocoder.yudao.module.iot.dal.mysql.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.*;

/**
 * IoT 插件信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PluginInfoMapper extends BaseMapperX<PluginInfoDO> {

    default PageResult<PluginInfoDO> selectPage(PluginInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PluginInfoDO>()
                .likeIfPresent(PluginInfoDO::getName, reqVO.getName())
                .eqIfPresent(PluginInfoDO::getStatus, reqVO.getStatus())
                .orderByDesc(PluginInfoDO::getId));
    }

}