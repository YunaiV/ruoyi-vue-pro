package cn.iocoder.yudao.module.mp.controller.admin.material;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.*;
import cn.iocoder.yudao.module.mp.convert.material.MpMaterialConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import cn.iocoder.yudao.module.mp.service.material.MpMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @芋艿：权限

@Api(tags = "管理后台 - 公众号素材")
@RestController
@RequestMapping("/mp/material")
@Validated
public class MpMaterialController {

    @Resource
    private MpMaterialService mpMaterialService;

    @ApiOperation("上传临时素材")
    @PostMapping("/upload-temporary")
    public CommonResult<MpMaterialUploadRespVO> uploadTemporaryMaterial(
            @Valid MpMaterialUploadTemporaryReqVO reqVO) throws IOException {
        MpMaterialDO material = mpMaterialService.uploadTemporaryMaterial(reqVO);
        return success(MpMaterialConvert.INSTANCE.convert(material));
    }

    @ApiOperation("上传永久素材")
    @PostMapping("/upload-permanent")
    public CommonResult<MpMaterialUploadRespVO> uploadPermanentMaterial(
            @Valid MpMaterialUploadPermanentReqVO reqVO) throws IOException {
        MpMaterialDO material = mpMaterialService.uploadPermanentMaterial(reqVO);
        return success(MpMaterialConvert.INSTANCE.convert(material));
    }

    @ApiOperation("上传图文内容中的图片")
    @PostMapping("/upload-news-image")
    public CommonResult<String> uploadNewsImage(@Valid MpMaterialUploadNewsImageReqVO reqVO)
            throws IOException {
        return success(mpMaterialService.uploadNewsImage(reqVO));
    }

    @ApiOperation("获得素材分页")
    @GetMapping("/page")
    public CommonResult<PageResult<MpMaterialRespVO>> getMaterialPage(@Valid MpMaterialPageReqVO pageReqVO) {
        PageResult<MpMaterialDO> pageResult = mpMaterialService.getMaterialPage(pageReqVO);
        return success(MpMaterialConvert.INSTANCE.convertPage(pageResult));
    }


}
