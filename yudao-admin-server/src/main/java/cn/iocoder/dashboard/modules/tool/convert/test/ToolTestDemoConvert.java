package cn.iocoder.dashboard.modules.tool.convert.test;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoCreateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoExcelVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoRespVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 字典类型 Convert
*
* @author 芋艿
*/
@Mapper
public interface ToolTestDemoConvert {

    ToolTestDemoConvert INSTANCE = Mappers.getMapper(ToolTestDemoConvert.class);

    ToolTestDemoDO convert(ToolTestDemoCreateReqVO bean);

    ToolTestDemoDO convert(ToolTestDemoUpdateReqVO bean);

    ToolTestDemoRespVO convert(ToolTestDemoDO bean);

    List<ToolTestDemoRespVO> convertList(List<ToolTestDemoDO> list);

    PageResult<ToolTestDemoRespVO> convertPage(PageResult<ToolTestDemoDO> page);

    List<ToolTestDemoExcelVO> convertList02(List<ToolTestDemoDO> list);

}
