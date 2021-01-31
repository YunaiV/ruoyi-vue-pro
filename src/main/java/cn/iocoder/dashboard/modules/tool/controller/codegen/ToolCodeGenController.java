package cn.iocoder.dashboard.modules.tool.controller.codegen;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodeGenTablePageItemRespVO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/tool/codegen")
public class ToolCodeGenController {

    @Resource
    private ToolCodegenService codegenService;

    @GetMapping("/table/page")
    public CommonResult<PageResult<ToolCodeGenTablePageItemRespVO>> getCodeGenTablePage() {
        return success(null);
    }

    @ApiOperation("基于数据库的表结构，创建代码生成器的表定义")
    @PostMapping("/table/create")
    // TODO 权限
    public CommonResult<Long> createCodeGenTable(@RequestParam("tableName") String tableName) {
        return success(codegenService.createCodegenTable(tableName));
    }

}
