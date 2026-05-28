package cn.iocoder.yudao.module.yaya.service.recording;

import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;

public interface YayaRecordingService {

    YayaAppRecordingRespVO createRecording(Long memberUserId, YayaAppRecordingUploadReqVO reqVO) throws Exception;

}
