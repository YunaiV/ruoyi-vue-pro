package cn.iocoder.dashboard.modules.tool.controller.test;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.*;
import cn.iocoder.dashboard.modules.tool.convert.test.ToolTestDemoConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import cn.iocoder.dashboard.modules.tool.service.test.ToolTestDemoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "字典类型")
@RestController
@RequestMapping("/tool/test-demo")
@Validated
public class ToolTestDemoController {

    @Resource
    private ToolTestDemoService testDemoService;

    @ApiOperation("创建字典类型")
    @PostMapping("/create")
    public CommonResult<Long> createTestDemo(@Valid ToolTestDemoCreateReqVO createReqVO) {
        return success(testDemoService.createTestDemo(createReqVO));
    }

    @ApiOperation("更新字典类型")
    @PutMapping("/update")
    public CommonResult<Boolean> updateTestDemo(@Valid ToolTestDemoUpdateReqVO updateReqVO) {
        testDemoService.updateTestDemo(updateReqVO);
        return success(true);
    }

    @ApiOperation("删除字典类型")
    @DeleteMapping("/delete")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    public CommonResult<Boolean> deleteTestDemo(@RequestParam("id") Long id) {
        testDemoService.deleteTestDemo(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    public CommonResult<ToolTestDemoRespVO> getTestDemo(@RequestParam("id") Long id) {
        ToolTestDemoDO testDemo = testDemoService.getTestDemo(id);
        return success(ToolTestDemoConvert.INSTANCE.convert(testDemo));
    }

    @GetMapping("/list")
    @ApiOperation("获得字典类型列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, dataTypeClass = List.class)
    public CommonResult<List<ToolTestDemoRespVO>> getTestDemoList(@RequestParam("ids") Collection<Long> ids) {
        List<ToolTestDemoDO> list = testDemoService.getTestDemoList(ids);
        return success(ToolTestDemoConvert.INSTANCE.convertList(list));
    }

    @ApiOperation("获得字典类型分页")
    @GetMapping("/page")
    public CommonResult<PageResult<ToolTestDemoRespVO>> getTestDemoPage(@Valid ToolTestDemoPageReqVO pageVO) {
        PageResult<ToolTestDemoDO> pageResult = testDemoService.getTestDemoPage(pageVO);
        return success(ToolTestDemoConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出字典类型 Excel")
    public void exportTestDemoExcel(@Valid ToolTestDemoExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<ToolTestDemoDO> list = testDemoService.getTestDemoList(exportReqVO);
        // 导出 Excel
        List<ToolTestDemoExcelVO> datas = ToolTestDemoConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "字典类型.xls", "数据", ToolTestDemoExcelVO.class, datas);
    }

}
