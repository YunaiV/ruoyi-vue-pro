package cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT OPC UA Server 配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotOpcUaServerConfigMapper extends BaseMapperX<IotOpcUaServerConfigDO> {

    default PageResult<IotOpcUaServerConfigDO> selectPage(IotOpcUaServerConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotOpcUaServerConfigDO>()
                .likeIfPresent(IotOpcUaServerConfigDO::getName, reqVO.getName())
                .eqIfPresent(IotOpcUaServerConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotOpcUaServerConfigDO::getId));
    }

    default IotOpcUaServerConfigDO selectByCredentialAlias(String credentialAlias) {
        return selectFirstOne(new LambdaQueryWrapperX<IotOpcUaServerConfigDO>()
                .eq(IotOpcUaServerConfigDO::getCredentialAlias, credentialAlias)
                .orderByAsc(IotOpcUaServerConfigDO::getId));
    }

    default List<IotOpcUaServerConfigDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotOpcUaServerConfigDO>()
                .eq(IotOpcUaServerConfigDO::getStatus, status)
                .orderByAsc(IotOpcUaServerConfigDO::getId));
    }

}
