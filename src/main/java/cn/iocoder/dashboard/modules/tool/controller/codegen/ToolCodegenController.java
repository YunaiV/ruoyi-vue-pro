package cn.iocoder.dashboard.modules.tool.controller.codegen;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenTableRespVO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "代码生成器 API")
@RestController
@RequestMapping("/tool/codegen")
@Validated
public class ToolCodegenController {

    @Resource
    private ToolCodegenService codegenService;

    @ApiOperation("获得表定义分页")
    @GetMapping("/page")
    // TODO 权限 @PreAuthorize("@ss.hasPermi('tool:gen:list')")
    public CommonResult<PageResult<ToolCodegenTableRespVO>> getCodeGenTablePage(@Valid ToolCodegenTablePageReqVO pageReqVO) {
        PageResult<ToolCodegenTableDO> pageResult = codegenService.getCodeGenTablePage(pageReqVO);
        return success(ToolCodegenConvert.INSTANCE.convertPage(pageResult));
    }

    @ApiOperation("获得表和字段的明细")
    @GetMapping("/detail")
//   todo @PreAuthorize("@ss.hasPermi('tool:gen:query')")
    public CommonResult<ToolCodegenDetailRespVO> getCodeGenDetail(@RequestParam("tableId") Long tableId) {
        ToolCodegenTableDO table = codegenService.getCodeGenTablePage(tableId);
        List<ToolCodegenColumnDO> columns = codegenService.getCodegenColumnListByTableId(tableId);
        // 拼装返回
        return success(ToolCodegenConvert.INSTANCE.convert(table, columns));
    }

    @ApiOperation("基于数据库的表结构，创建代码生成器的表定义")
    @PostMapping("/create")
    // TODO 权限
    public CommonResult<Long> createCodeGenTable(@RequestParam("tableName") String tableName) {
        return success(codegenService.createCodegenTable(tableName));
    }

}
