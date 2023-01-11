package cn.iocoder.yudao.module.mp.controller.admin.material;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadTemporaryReqVO;
import cn.iocoder.yudao.module.mp.convert.material.MpMaterialConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import cn.iocoder.yudao.module.mp.service.material.MpMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

}
