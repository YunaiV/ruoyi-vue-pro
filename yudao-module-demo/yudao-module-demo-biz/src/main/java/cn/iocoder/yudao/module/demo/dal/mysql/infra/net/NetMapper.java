package cn.iocoder.yudao.module.demo.dal.mysql.infra.net;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.net.NetDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo.*;

/**
 * 区块链网络 Mapper
 *
 * @author ruanzh.eth
 */
@Mapper
public interface NetMapper extends BaseMapperX<NetDO> {

    default PageResult<NetDO> selectPage(NetPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NetDO>()
                .likeIfPresent(NetDO::getName, reqVO.getName())
                .eqIfPresent(NetDO::getExplorer, reqVO.getExplorer())
                .eqIfPresent(NetDO::getSymbol, reqVO.getSymbol())
                .eqIfPresent(NetDO::getEndpoint, reqVO.getEndpoint())
                .eqIfPresent(NetDO::getType, reqVO.getType())
                .eqIfPresent(NetDO::getInfo, reqVO.getInfo())
                .betweenIfPresent(NetDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(NetDO::getId));
    }

    default List<NetDO> selectList(NetExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<NetDO>()
                .likeIfPresent(NetDO::getName, reqVO.getName())
                .eqIfPresent(NetDO::getExplorer, reqVO.getExplorer())
                .eqIfPresent(NetDO::getSymbol, reqVO.getSymbol())
                .eqIfPresent(NetDO::getEndpoint, reqVO.getEndpoint())
                .eqIfPresent(NetDO::getType, reqVO.getType())
                .eqIfPresent(NetDO::getInfo, reqVO.getInfo())
                .betweenIfPresent(NetDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(NetDO::getId));
    }

}
