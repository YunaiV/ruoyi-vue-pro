package cn.iocoder.dashboard.modules.system.convert.dict;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataRespVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataSimpleVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDictDataConvert {

    SysDictDataConvert INSTANCE = Mappers.getMapper(SysDictDataConvert.class);

    List<SysDictDataSimpleVO> convertList(List<SysDictDataDO> list);

    SysDictDataRespVO convert(SysDictDataDO bean);

    PageResult<SysDictDataRespVO> convertPage(PageResult<SysDictDataDO> page);

    SysDictDataDO convert(SysDictDataUpdateReqVO bean);

    SysDictDataDO convert(SysDictDataCreateReqVO bean);

    @Mapping(source = "records", target = "list")
    PageResult<SysDictDataDO> convertPage02(IPage<SysDictDataDO> page);

}
