package cn.iocoder.dashboard.modules.tool.controller.codegen;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodeGenTablePageItemRespVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/tool/code-gen")
public class ToolCodeGenController {

    @GetMapping("/table/page")
    public CommonResult<PageResult<ToolCodeGenTablePageItemRespVO>> getCodeGenTablePage() {
        return success(null);
    }

}
