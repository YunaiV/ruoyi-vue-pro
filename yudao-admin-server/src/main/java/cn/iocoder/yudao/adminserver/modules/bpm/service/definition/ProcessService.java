package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import org.springframework.web.multipart.MultipartFile;

/**
 * 流程基础管理
 *
 * @author ZJQ
 * @date 2021/9/5 21:00
 */
@Deprecated
public interface ProcessService {

    /**
     * 上传流程文件，进行流程模型部署
     * @param multipartFile 上传文件
     */
    void deployProcess(MultipartFile multipartFile);

}
