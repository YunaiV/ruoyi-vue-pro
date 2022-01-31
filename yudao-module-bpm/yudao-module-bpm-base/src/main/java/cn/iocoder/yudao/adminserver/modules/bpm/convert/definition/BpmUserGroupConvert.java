package cn.iocoder.yudao.adminserver.modules.bpm.convert.definition;

import java.util.*;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.coreservice.modules.bpm.api.group.dto.BpmUserGroupDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * 用户组 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmUserGroupConvert {

    BpmUserGroupConvert INSTANCE = Mappers.getMapper(BpmUserGroupConvert.class);

    BpmUserGroupDO convert(BpmUserGroupCreateReqVO bean);

    BpmUserGroupDO convert(BpmUserGroupUpdateReqVO bean);

    BpmUserGroupRespVO convert(BpmUserGroupDO bean);

    List<BpmUserGroupRespVO> convertList(List<BpmUserGroupDO> list);

    PageResult<BpmUserGroupRespVO> convertPage(PageResult<BpmUserGroupDO> page);

    @Named("convertList2")
    List<BpmUserGroupRespVO> convertList2(List<BpmUserGroupDO> list);

    List<BpmUserGroupDTO> convertList3(List<BpmUserGroupDO> list);
}
