package cn.iocoder.yudao.adminserver.modules.activiti.service.process;

import org.springframework.web.multipart.MultipartFile;

/**
 * 流程基础管理
 *
 * @author ZJQ
 * @date 2021/9/5 21:00
 */
public interface ProcessService {

    /**
     * 上传流程文件，进行流程模型部署
     * @param multipartFile 上传文件
     */
    void deployProcess(MultipartFile multipartFile);


    /**
     * 激活或者挂起流程模型实体
     * @param processDefinitionId 流程模型实体id
     * @param type 类型
     * @return 状态
     */
    String setActivOrHang(String processDefinitionId,String type);


}
