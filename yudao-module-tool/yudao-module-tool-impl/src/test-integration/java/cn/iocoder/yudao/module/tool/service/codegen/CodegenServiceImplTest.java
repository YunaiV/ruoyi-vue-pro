package cn.iocoder.yudao.module.tool.service.codegen;

import cn.iocoder.yudao.module.tool.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

class CodegenServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CodegenServiceImpl codegenService;

    @Test
    public void tetCreateCodegenTable() {
        codegenService.createCodegen(0L, "tool_test_demo");
//        toolCodegenService.createCodegenTable("tool_codegen_table");
//        toolCodegenService.createCodegen("tool_codegen_column");
    }

}
