package cn.iocoder.yudao.module.demo.convert.infra.endpoint;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;

/**
 * 区块链节点 Convert
 *
 * @author ruanzh.eth
 */
@Mapper
public interface EndpointConvert {

    EndpointConvert INSTANCE = Mappers.getMapper(EndpointConvert.class);

    EndpointDO convert(EndpointCreateReqVO bean);

    EndpointDO convert(EndpointUpdateReqVO bean);

    EndpointRespVO convert(EndpointDO bean);

    List<EndpointRespVO> convertList(List<EndpointDO> list);

    PageResult<EndpointRespVO> convertPage(PageResult<EndpointDO> page);

    List<EndpointExcelVO> convertList02(List<EndpointDO> list);

}
