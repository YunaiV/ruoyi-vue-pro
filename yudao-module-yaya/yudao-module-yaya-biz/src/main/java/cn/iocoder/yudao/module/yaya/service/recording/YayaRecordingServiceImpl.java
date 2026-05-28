package cn.iocoder.yudao.module.yaya.service.recording;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.recording.YayaRecordingDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.recording.YayaRecordingMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_RECORDING_FILE_REQUIRED;

@Service
@Validated
public class YayaRecordingServiceImpl implements YayaRecordingService {

    private static final String STATUS_STORED = "stored";

    @Value("${yaya.recording.storage-root:/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/storage/recordings}")
    private String storageRoot;

    @Resource
    private YayaRecordingMapper recordingMapper;

    @Override
    public YayaAppRecordingRespVO createRecording(Long memberUserId, YayaAppRecordingUploadReqVO reqVO) throws Exception {
        requireMember(memberUserId);
        MultipartFile file = reqVO.getFile();
        if (file == null || file.isEmpty()) {
            throw exception(YAYA_RECORDING_FILE_REQUIRED);
        }

        Path memberDirectory = Path.of(storageRoot, String.valueOf(memberUserId));
        Files.createDirectories(memberDirectory);
        Path target = memberDirectory.resolve(UUID.randomUUID() + "-" + safeFilename(file.getOriginalFilename()));
        Files.write(target, file.getBytes());

        YayaRecordingDO recording = new YayaRecordingDO();
        recording.setMemberUserId(memberUserId);
        recording.setTopicId(reqVO.getTopicId());
        recording.setQuestionId(reqVO.getQuestionId());
        recording.setAttemptId(reqVO.getAttemptId());
        recording.setStoragePath(target.toString());
        recording.setMimeType(StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"));
        recording.setSizeBytes(file.getSize());
        recording.setDurationSeconds(reqVO.getDurationSeconds());
        recording.setChecksum("");
        recording.setStatus(STATUS_STORED);
        recording.setMetadata(Collections.emptyMap());
        recordingMapper.insert(recording);
        return toResp(recording);
    }

    private static YayaAppRecordingRespVO toResp(YayaRecordingDO recording) {
        return new YayaAppRecordingRespVO()
                .setId(recording.getId())
                .setTopicId(recording.getTopicId())
                .setQuestionId(recording.getQuestionId())
                .setAttemptId(recording.getAttemptId())
                .setStoragePath(recording.getStoragePath())
                .setMimeType(recording.getMimeType())
                .setSizeBytes(recording.getSizeBytes())
                .setDurationSeconds(recording.getDurationSeconds())
                .setStatus(recording.getStatus());
    }

    private static String safeFilename(String originalFilename) {
        String filename = StrUtil.blankToDefault(originalFilename, "recording.webm").replace('\\', '/');
        filename = StrUtil.subAfter(filename, "/", true);
        filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        return StrUtil.blankToDefault(filename, "recording.webm");
    }

    private static void requireMember(Long memberUserId) {
        if (memberUserId == null) {
            throw exception(YAYA_MEMBER_NOT_LOGIN);
        }
    }

}
