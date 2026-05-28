package cn.iocoder.yudao.module.yaya.controller.app.recording;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;
import cn.iocoder.yudao.module.yaya.service.recording.YayaRecordingService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/yaya/recordings")
@Validated
public class YayaAppRecordingController {

    @Resource
    private YayaRecordingService recordingService;

    @PostMapping("")
    public CommonResult<YayaAppRecordingRespVO> createRecording(@Valid YayaAppRecordingUploadReqVO reqVO) throws Exception {
        return success(recordingService.createRecording(getLoginUserId(), reqVO));
    }

}
