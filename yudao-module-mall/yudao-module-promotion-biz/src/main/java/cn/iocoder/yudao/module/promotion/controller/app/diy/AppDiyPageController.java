package cn.iocoder.yudao.module.promotion.controller.app.diy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.promotion.controller.app.diy.vo.AppDiyPagePropertyRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.service.diy.DiyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 装修页面")
@RestController
@RequestMapping("/promotion/diy-page")
@Validated
public class AppDiyPageController {

    @Resource
    private DiyPageService diyPageService;

    @GetMapping("/get")
    @Operation(summary = "获得装修页面")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppDiyPagePropertyRespVO> getDiyPage(@RequestParam("id") Long id) {
        DiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(BeanUtils.toBean(diyPage, AppDiyPagePropertyRespVO.class));
    }

}
