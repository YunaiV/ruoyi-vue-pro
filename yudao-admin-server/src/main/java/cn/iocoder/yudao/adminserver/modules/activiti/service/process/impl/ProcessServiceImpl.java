package cn.iocoder.yudao.adminserver.modules.activiti.service.process.impl;

import cn.iocoder.yudao.adminserver.modules.activiti.service.process.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.FILE_UPLOAD_FAILED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 流程基础管理
 *
 * @author ZJQ
 * @date 2021/9/5 21:04
 */
@Service
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    private static final String BPMN20_XML = "bpmn20.xml";

    @Resource
    private RepositoryService repositoryService;

    /**
     * 上传流程文件，进行流程部署
     * @param multipartFile 上传文件
     */
    @Override
    public void deployProcess(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        try (InputStream inputStream = multipartFile.getInputStream()){
            Deployment deployment = getDeplymentByType(inputStream,fileName);
            //获取部署成功的流程模型
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            processDefinitions.forEach((processDefinition)->{
                //设置线上部署流程模型名字
                String proDefId = processDefinition.getId();
                repositoryService.setProcessDefinitionCategory(proDefId,fileName);
                log.info("流程文件部署成功，流程ID="+proDefId);
            });
        } catch (IOException e) {
           log.error("流程部署出现异常"+e);
        }
    }

    /**
     * 激活或者挂起流程模型实体
     * @param processDefinitionId 流程模型实体id
     * @param type 类型
     * @return 提示
     */
    @Override
    public String setActivOrHang(String processDefinitionId, String type) {
        String result = "无操作";
        switch (type){
            case "active":
                repositoryService.activateProcessDefinitionById(processDefinitionId,true,null);
                result = "已激活ID为【"+processDefinitionId+"】的流程模型实例";
                break;
            case "suspend":
                repositoryService.suspendProcessDefinitionById(processDefinitionId,true,null);
                result = "已挂起ID为【"+processDefinitionId+"】的流程模型实例";
                break;
            default:
                break;
        }
        return result;
    }


    /**
     * 根据上传文件类型对应实现不同方式的流程部署
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @return 文件部署流程
     */
    public Deployment getDeplymentByType(InputStream inputStream,String fileName){
        Deployment deployment;
        String type = FilenameUtils.getExtension(fileName);
        switch (type){
            case "bpmn":
                String baseName = FilenameUtils.getBaseName(fileName);
                deployment = repositoryService.createDeployment().addInputStream(baseName+"."+BPMN20_XML,inputStream).deploy();
                break;
            case "png":
                deployment = repositoryService.createDeployment().addInputStream(fileName,inputStream).deploy();
                break;
            case "zip":
            case "bar":
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                deployment = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
                break;
            default:
                throw exception(FILE_UPLOAD_FAILED);
        }
        return deployment;
    }
}
