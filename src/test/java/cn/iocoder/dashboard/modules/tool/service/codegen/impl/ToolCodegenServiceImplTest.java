package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.iocoder.dashboard.BaseDbUnitTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

class ToolCodegenServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ToolCodegenServiceImpl toolCodegenService;

    @Test
    public void tetCreateCodegenTable() {
        toolCodegenService.createCodegen("tool_test_demo");
//        toolCodegenService.createCodegenTable("tool_codegen_table");
//        toolCodegenService.createCodegen("tool_codegen_column");
    }

}
