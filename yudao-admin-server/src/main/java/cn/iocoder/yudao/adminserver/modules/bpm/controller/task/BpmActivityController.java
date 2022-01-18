package cn.iocoder.yudao.adminserver.modules.bpm.controller.task;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmActivityService;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "流程活动实例")
@RestController
@RequestMapping("/bpm/activity")
@Validated
public class BpmActivityController {

    @Resource
    private BpmActivityService activityService;

    // TODO 芋艿：注解、权限、validtion

    @ApiOperation(value = "生成指定流程实例的高亮流程图",
            notes = "只高亮进行中的任务。不过要注意，该接口暂时没用，通过前端的 ProcessViewer.vue 界面的 highlightDiagram 方法生成")
    @GetMapping("/generate-highlight-diagram")
    @ApiImplicitParam(name = "id", value = "流程实例的编号", required = true, dataTypeClass = String.class)
    public void generateHighlightDiagram(@RequestParam("processInstanceId") String processInstanceId,
                                         HttpServletResponse response) throws IOException {
        byte[] bytes = activityService.generateHighlightDiagram(processInstanceId);
        ServletUtils.writeAttachment(response, StrUtil.format("流程图-{}.svg", processInstanceId), bytes);
    }

}
