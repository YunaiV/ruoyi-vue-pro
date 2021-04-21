package cn.iocoder.dashboard.modules.infra.service.file.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.file.config.FileProperties;
import cn.iocoder.dashboard.modules.infra.controller.file.vo.InfFilePageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.file.InfFileMapper;
import cn.iocoder.dashboard.modules.infra.service.file.InfFileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.FILE_NOT_EXISTS;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.FILE_PATH_EXISTS;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class InfFileServiceImpl implements InfFileService {

    @Resource
    private InfFileMapper fileMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public String createFile(String path, byte[] content) {
        if (fileMapper.selectCountById(path) > 0) {
            throw exception(FILE_PATH_EXISTS);
        }
        // 保存到数据库
        InfFileDO file = new InfFileDO();
        file.setId(path);
        file.setType(FileTypeUtil.getType(new ByteArrayInputStream(content)));
        file.setContent(content);
        fileMapper.insert(file);
        // 拼接路径返回
        return fileProperties.getBasePath() + path;
    }

    @Override
    public void deleteFile(String id) {
        // 校验存在
        this.validateFileExists(id);
        // 更新
        fileMapper.deleteById(id);
    }

    private void validateFileExists(String id) {
        if (fileMapper.selectById(id) == null) {
            throw exception(FILE_NOT_EXISTS);
        }
    }

    @Override
    public InfFileDO getFile(String path) {
        return fileMapper.selectById(path);
    }

    @Override
    public PageResult<InfFileDO> getFilePage(InfFilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

}
