package cn.iocoder.yudao.module.iot.dal.mysql.plugininfo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.*;

/**
 * IoT 插件信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PluginInfoMapper extends BaseMapperX<PluginInfoDO> {

    default PageResult<PluginInfoDO> selectPage(PluginInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PluginInfoDO>()
                .eqIfPresent(PluginInfoDO::getPluginId, reqVO.getPluginId())
                .likeIfPresent(PluginInfoDO::getName, reqVO.getName())
                .eqIfPresent(PluginInfoDO::getDescription, reqVO.getDescription())
                .eqIfPresent(PluginInfoDO::getDeployType, reqVO.getDeployType())
                .eqIfPresent(PluginInfoDO::getFile, reqVO.getFile())
                .eqIfPresent(PluginInfoDO::getVersion, reqVO.getVersion())
                .eqIfPresent(PluginInfoDO::getType, reqVO.getType())
                .eqIfPresent(PluginInfoDO::getProtocol, reqVO.getProtocol())
                .eqIfPresent(PluginInfoDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PluginInfoDO::getConfigSchema, reqVO.getConfigSchema())
                .eqIfPresent(PluginInfoDO::getConfig, reqVO.getConfig())
                .eqIfPresent(PluginInfoDO::getScript, reqVO.getScript())
                .betweenIfPresent(PluginInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PluginInfoDO::getId));
    }

}