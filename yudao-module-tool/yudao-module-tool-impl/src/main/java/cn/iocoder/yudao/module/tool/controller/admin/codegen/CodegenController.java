package cn.iocoder.yudao.module.tool.controller.admin.codegen;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenDetailRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenPreviewRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.CodegenTableRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.SchemaTableRespVO;
import cn.iocoder.yudao.module.tool.convert.codegen.CodegenConvert;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaTableDO;
import cn.iocoder.yudao.module.tool.service.codegen.CodegenService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "管理后台 - 代码生成器")
@RestController
@RequestMapping("/tool/codegen")
@Validated
public class CodegenController {

    @Resource
    private CodegenService codegenService;

    @GetMapping("/db/table/list")
    @ApiOperation(value = "获得数据库自带的表定义列表", notes = "会过滤掉已经导入 Codegen 的表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableName", value = "表名，模糊匹配", required = true, example = "yudao", dataTypeClass = String.class),
            @ApiImplicitParam(name = "tableComment", value = "描述，模糊匹配", required = true, example = "芋道", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<List<SchemaTableRespVO>> getSchemaTableList(
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "tableComment", required = false) String tableComment) {
        // 获得数据库自带的表定义列表
        List<SchemaTableDO> schemaTables = codegenService.getSchemaTableList(tableName, tableComment);
        // 移除在 Codegen 中，已经存在的
        Set<String> existsTables = CollectionUtils.convertSet(codegenService.getCodeGenTableList(), CodegenTableDO::getTableName);
        schemaTables.removeIf(table -> existsTables.contains(table.getTableName()));
        return success(CodegenConvert.INSTANCE.convertList04(schemaTables));
    }

    @GetMapping("/table/page")
    @ApiOperation("获得表定义分页")
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<PageResult<CodegenTableRespVO>> getCodeGenTablePage(@Valid CodegenTablePageReqVO pageReqVO) {
        PageResult<CodegenTableDO> pageResult = codegenService.getCodegenTablePage(pageReqVO);
        return success(CodegenConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/detail")
    @ApiOperation("获得表和字段的明细")
    @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<CodegenDetailRespVO> getCodegenDetail(@RequestParam("tableId") Long tableId) {
        CodegenTableDO table = codegenService.getCodegenTablePage(tableId);
        List<CodegenColumnDO> columns = codegenService.getCodegenColumnListByTableId(tableId);
        // 拼装返回
        return success(CodegenConvert.INSTANCE.convert(table, columns));
    }

    @ApiOperation("基于数据库的表结构，创建代码生成器的表和字段定义")
    @ApiImplicitParam(name = "tableNames", value = "表名数组", required = true, example = "sys_user", dataTypeClass = List.class)
    @PostMapping("/create-list-from-db")
    @PreAuthorize("@ss.hasPermission('tool:codegen:create')")
    public CommonResult<List<Long>> createCodegenListFromDB(@RequestParam("tableNames") List<String> tableNames) {
        return success(codegenService.createCodegenListFromDB(getLoginUserId(), tableNames));
    }

    @ApiOperation("基于 SQL 建表语句，创建代码生成器的表和字段定义")
    @ApiImplicitParam(name = "sql", value = "SQL 建表语句", required = true, example = "sql", dataTypeClass = String.class)
    @PostMapping("/create-list-from-sql")
    @PreAuthorize("@ss.hasPermission('tool:codegen:create')")
    public CommonResult<Long> createCodegenListFromSQL(@RequestParam("sql") String sql) {
        return success(codegenService.createCodegenListFromSQL(getLoginUserId(), sql));
    }

    @ApiOperation("更新数据库的表和字段定义")
    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> updateCodegen(@Valid @RequestBody CodegenUpdateReqVO updateReqVO) {
        codegenService.updateCodegen(updateReqVO);
        return success(true);
    }

    @ApiOperation("基于数据库的表结构，同步数据库的表和字段定义")
    @PutMapping("/sync-from-db")
    @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> syncCodegenFromDB(@RequestParam("tableId") Long tableId) {
        codegenService.syncCodegenFromDB(tableId);
        return success(true);
    }

    @ApiOperation("基于 SQL 建表语句，同步数据库的表和字段定义")
    @PutMapping("/sync-from-sql")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "sql", value = "SQL 建表语句", required = true, example = "sql", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> syncCodegenFromSQL(@RequestParam("tableId") Long tableId,
                                                    @RequestParam("sql") String sql) {
        codegenService.syncCodegenFromSQL(tableId, sql);
        return success(true);
    }

    @ApiOperation("删除数据库的表和字段定义")
    @DeleteMapping("/delete")
    @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:delete')")
    public CommonResult<Boolean> deleteCodegen(@RequestParam("tableId") Long tableId) {
        codegenService.deleteCodegen(tableId);
        return success(true);
    }

    @ApiOperation("预览生成代码")
    @GetMapping("/preview")
    @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:preview')")
    public CommonResult<List<CodegenPreviewRespVO>> previewCodegen(@RequestParam("tableId") Long tableId) {
        Map<String, String> codes = codegenService.generationCodes(tableId);
        return success(CodegenConvert.INSTANCE.convert(codes));
    }

    @ApiOperation("下载生成代码")
    @GetMapping("/download")
    @ApiImplicitParam(name = "tableId", value = "表编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:download')")
    public void downloadCodegen(@RequestParam("tableId") Long tableId,
                                HttpServletResponse response) throws IOException {
        // 生成代码
        Map<String, String> codes = codegenService.generationCodes(tableId);
        // 构建 zip 包
        String[] paths = codes.keySet().toArray(new String[0]);
        ByteArrayInputStream[] ins = codes.values().stream().map(IoUtil::toUtf8Stream).toArray(ByteArrayInputStream[]::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtil.zip(outputStream, paths, ins);
        // 输出
        ServletUtils.writeAttachment(response, "codegen.zip", outputStream.toByteArray());
    }

}
