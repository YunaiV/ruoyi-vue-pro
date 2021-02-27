package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ToolCodegenServiceImplTest {

    @Resource
    private ToolCodegenServiceImpl toolCodegenService;

    @Test
    public void tetCreateCodegenTable() {
        toolCodegenService.createCodegen("tool_test_demo");
//        toolCodegenService.createCodegenTable("tool_codegen_table");
//        toolCodegenService.createCodegen("tool_codegen_column");
    }

}
