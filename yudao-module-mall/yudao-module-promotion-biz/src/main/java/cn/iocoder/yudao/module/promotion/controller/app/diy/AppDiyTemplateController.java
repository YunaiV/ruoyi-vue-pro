package cn.iocoder.yudao.module.promotion.controller.app.diy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.diy.vo.AppDiyTemplatePropertyRespVO;
import cn.iocoder.yudao.module.promotion.convert.diy.DiyTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.diy.DiyPageEnum;
import cn.iocoder.yudao.module.promotion.service.diy.DiyPageService;
import cn.iocoder.yudao.module.promotion.service.diy.DiyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.findFirst;

@Tag(name = "用户 APP - 装修模板")
@RestController
@RequestMapping("/promotion/diy-template")
@Validated
public class AppDiyTemplateController {

    @Resource
    private DiyTemplateService diyTemplateService;
    @Resource
    private DiyPageService diyPageService;

    // TODO @疯狂：要不要把 used 和 get 接口合并哈；不传递 id，直接拿默认；
    @GetMapping("/used")
    @Operation(summary = "使用中的装修模板")
    public CommonResult<AppDiyTemplatePropertyRespVO> getUsedDiyTemplate() {
        DiyTemplateDO diyTemplate = diyTemplateService.getUsedDiyTemplate();
        return success(buildVo(diyTemplate));
    }

    @GetMapping("/get")
    @Operation(summary = "获得装修模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppDiyTemplatePropertyRespVO> getDiyTemplate(@RequestParam("id") Long id) {
        DiyTemplateDO diyTemplate = diyTemplateService.getDiyTemplate(id);
        return success(buildVo(diyTemplate));
    }

    private AppDiyTemplatePropertyRespVO buildVo(DiyTemplateDO diyTemplate) {
        if (diyTemplate == null) {
            return null;
        }
        // 查询模板下的页面
        List<DiyPageDO> pages = diyPageService.getDiyPageByTemplateId(diyTemplate.getId());
        String home = findFirst(pages, page -> DiyPageEnum.INDEX.getName().equals(page.getName()), DiyPageDO::getProperty);
        String user = findFirst(pages, page -> DiyPageEnum.MY.getName().equals(page.getName()), DiyPageDO::getProperty);
        // 拼接返回
        return DiyTemplateConvert.INSTANCE.convertPropertyVo2(diyTemplate, home, user);
    }

}
