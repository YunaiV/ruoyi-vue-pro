package cn.iocoder.yudao.module.demo.dal.mysql.infra.endpoint;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;

/**
 * 区块链节点 Mapper
 *
 * @author ruanzh.eth
 */
@Mapper
public interface EndpointMapper extends BaseMapperX<EndpointDO> {

    default PageResult<EndpointDO> selectPage(EndpointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EndpointDO>()
                .eqIfPresent(EndpointDO::getNetId, reqVO.getNetId())
                .likeIfPresent(EndpointDO::getName, reqVO.getName())
                .eqIfPresent(EndpointDO::getUrl, reqVO.getUrl())
                .eqIfPresent(EndpointDO::getBlocked, reqVO.getBlocked())
                .eqIfPresent(EndpointDO::getInfo, reqVO.getInfo())
                .betweenIfPresent(EndpointDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EndpointDO::getId));
    }

    default List<EndpointDO> selectList(EndpointExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<EndpointDO>()
                .eqIfPresent(EndpointDO::getNetId, reqVO.getNetId())
                .likeIfPresent(EndpointDO::getName, reqVO.getName())
                .eqIfPresent(EndpointDO::getUrl, reqVO.getUrl())
                .eqIfPresent(EndpointDO::getBlocked, reqVO.getBlocked())
                .eqIfPresent(EndpointDO::getInfo, reqVO.getInfo())
                .betweenIfPresent(EndpointDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EndpointDO::getId));
    }

}
