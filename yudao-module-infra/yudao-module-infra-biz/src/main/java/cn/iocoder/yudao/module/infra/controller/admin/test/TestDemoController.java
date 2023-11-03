package cn.iocoder.yudao.module.infra.controller.admin.test;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.infra.controller.admin.test.vo.*;
import cn.iocoder.yudao.module.infra.convert.test.TestDemoConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.module.infra.service.test.TestDemoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 字典类型")
@RestController
@RequestMapping("/infra/test-demo")
@Validated
public class TestDemoController {

    @Resource
    private TestDemoService testDemoService;

    @PostMapping("/create")
    @Operation(summary = "创建字典类型")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:create')")
    public CommonResult<Long> createTestDemo(@Valid @RequestBody TestDemoCreateReqVO createReqVO) {
        return success(testDemoService.createTestDemo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新字典类型")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:update')")
    public CommonResult<Boolean> updateTestDemo(@Valid @RequestBody TestDemoUpdateReqVO updateReqVO) {
        testDemoService.updateTestDemo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:test-demo:delete')")
    public CommonResult<Boolean> deleteTestDemo(@RequestParam("id") Long id) {
        testDemoService.deleteTestDemo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得字典类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:query')")
    public CommonResult<TestDemoRespVO> getTestDemo(@RequestParam("id") Long id) {
        TestDemoDO testDemo = testDemoService.getTestDemo(id);
        return success(TestDemoConvert.INSTANCE.convert(testDemo));
    }

    @GetMapping("/list")
    @Operation(summary = "获得字典类型列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:query')")
    public CommonResult<List<TestDemoRespVO>> getTestDemoList(@RequestParam("ids") Collection<Long> ids) {
        List<TestDemoDO> list = testDemoService.getTestDemoList(ids);
        return success(TestDemoConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得字典类型分页")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:query')")    public CommonResult<PageResult<TestDemoRespVO>> getTestDemoPage(@Valid TestDemoPageReqVO pageVO) {
        PageResult<TestDemoDO> pageResult = testDemoService.getTestDemoPage(pageVO);
        return success(TestDemoConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出字典类型 Excel")
    @PreAuthorize("@ss.hasPermission('infra:test-demo:export')")    @OperateLog(type = EXPORT)
    public void exportTestDemoExcel(@Valid TestDemoExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<TestDemoDO> list = testDemoService.getTestDemoList(exportReqVO);
        // 导出 Excel
        List<TestDemoExcelVO> datas = TestDemoConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "字典类型.xls", "数据", TestDemoExcelVO.class, datas);
    }

}
