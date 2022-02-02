package cn.iocoder.yudao.module.tool.controller.app.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.tool.controller.app.test.vo.*;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.module.tool.convert.test.TestDemoConvert;
import cn.iocoder.yudao.module.tool.service.test.TestDemoService;

@Api(tags = "用户 APP - 字典类型")
@RestController
@RequestMapping("/tool/test-demo")
@Validated
public class AppTestDemoController {

    @Resource
    private TestDemoService testDemoService;

    @PostMapping("/create")
    @ApiOperation("创建字典类型")
    public CommonResult<Long> createTestDemo(@Valid @RequestBody AppTestDemoCreateReqVO createReqVO) {
        return success(testDemoService.createTestDemo(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新字典类型")
    public CommonResult<Boolean> updateTestDemo(@Valid @RequestBody AppTestDemoUpdateReqVO updateReqVO) {
        testDemoService.updateTestDemo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    public CommonResult<Boolean> deleteTestDemo(@RequestParam("id") Long id) {
        testDemoService.deleteTestDemo(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:test-demo:query')")
    public CommonResult<AppTestDemoRespVO> getTestDemo(@RequestParam("id") Long id) {
        TestDemoDO testDemo = testDemoService.getTestDemo(id);
        return success(TestDemoConvert.INSTANCE.convert(testDemo));
    }

    @GetMapping("/list")
    @ApiOperation("获得字典类型列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    public CommonResult<List<AppTestDemoRespVO>> getTestDemoList(@RequestParam("ids") Collection<Long> ids) {
        List<TestDemoDO> list = testDemoService.getTestDemoList(ids);
        return success(TestDemoConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得字典类型分页")
    public CommonResult<PageResult<AppTestDemoRespVO>> getTestDemoPage(@Valid AppTestDemoPageReqVO pageVO) {
        PageResult<TestDemoDO> pageResult = testDemoService.getTestDemoPage(pageVO);
        return success(TestDemoConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出字典类型 Excel")
    @OperateLog(type = EXPORT)
    public void exportTestDemoExcel(@Valid AppTestDemoExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<TestDemoDO> list = testDemoService.getTestDemoList(exportReqVO);
        // 导出 Excel
        List<AppTestDemoExcelVO> datas = TestDemoConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "字典类型.xls", "数据", AppTestDemoExcelVO.class, datas);
    }

}
