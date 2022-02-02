package cn.iocoder.yudao.module.tool.convert.test;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.tool.controller.app.test.vo.*;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;

/**
 * 字典类型 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface TestDemoConvert {

    TestDemoConvert INSTANCE = Mappers.getMapper(TestDemoConvert.class);

    TestDemoDO convert(AppTestDemoCreateReqVO bean);

    TestDemoDO convert(AppTestDemoUpdateReqVO bean);

    AppTestDemoRespVO convert(TestDemoDO bean);

    List<AppTestDemoRespVO> convertList(List<TestDemoDO> list);

    PageResult<AppTestDemoRespVO> convertPage(PageResult<TestDemoDO> page);

    List<AppTestDemoExcelVO> convertList02(List<TestDemoDO> list);

}
