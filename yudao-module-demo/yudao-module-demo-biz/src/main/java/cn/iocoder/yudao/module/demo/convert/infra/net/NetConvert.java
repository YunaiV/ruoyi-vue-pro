package cn.iocoder.yudao.module.demo.convert.infra.net;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.net.NetDO;

/**
 * 区块链网络 Convert
 *
 * @author ruanzh.eth
 */
@Mapper
public interface NetConvert {

    NetConvert INSTANCE = Mappers.getMapper(NetConvert.class);

    NetDO convert(NetCreateReqVO bean);

    NetDO convert(NetUpdateReqVO bean);

    NetRespVO convert(NetDO bean);

    List<NetRespVO> convertList(List<NetDO> list);

    PageResult<NetRespVO> convertPage(PageResult<NetDO> page);

    List<NetExcelVO> convertList02(List<NetDO> list);

}
