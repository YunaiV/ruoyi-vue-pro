package cn.iocoder.dashboard.modules.system.convert.dict;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeRespVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeSimpleRespVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDictTypeConvert {

    SysDictTypeConvert INSTANCE = Mappers.getMapper(SysDictTypeConvert.class);

    PageResult<SysDictTypeRespVO> convertPage(PageResult<SysDictTypeDO> bean);

    SysDictTypeRespVO convert(SysDictTypeDO bean);

    @Mapping(source = "records", target = "list")
    PageResult<SysDictTypeDO> convertPage02(IPage<SysDictTypeDO> page);

    SysDictTypeDO convert(SysDictTypeCreateReqVO bean);

    SysDictTypeDO convert(SysDictTypeUpdateReqVO bean);

    List<SysDictTypeSimpleRespVO> convertList(List<SysDictTypeDO> list);

}
