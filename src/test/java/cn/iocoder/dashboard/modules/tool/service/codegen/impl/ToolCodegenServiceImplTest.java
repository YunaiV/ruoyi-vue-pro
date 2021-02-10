package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.iocoder.dashboard.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToolCodegenServiceImplTest {

    @Resource
    private ToolCodegenServiceImpl toolCodegenService;

    @Test
    public void tetCreateCodegenTable() {
//        toolCodegenService.createCodegenTable("sys_test_demo");
//        toolCodegenService.createCodegenTable("tool_codegen_table");
        toolCodegenService.createCodegen("tool_codegen_column");
    }

}
