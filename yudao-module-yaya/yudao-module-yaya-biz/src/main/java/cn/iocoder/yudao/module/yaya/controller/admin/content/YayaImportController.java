package cn.iocoder.yudao.module.yaya.controller.admin.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaImportResultRespVO;
import cn.iocoder.yudao.module.yaya.service.content.YayaImportService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/yaya")
public class YayaImportController {

    @Resource
    private YayaImportService importService;

    @PostMapping("/import-batches/{season}:preview")
    @PreAuthorize("@ss.hasPermission('yaya:import:preview')")
    public CommonResult<YayaImportResultRespVO> previewImport(@PathVariable("season") String season) {
        return success(YayaImportResultRespVO.from(importService.previewImport(season)));
    }

    @PostMapping("/import-batches/{season}:run")
    @PreAuthorize("@ss.hasPermission('yaya:import:run')")
    public CommonResult<YayaImportResultRespVO> runImport(@PathVariable("season") String season) {
        return success(YayaImportResultRespVO.from(importService.runImport(season)));
    }

}
