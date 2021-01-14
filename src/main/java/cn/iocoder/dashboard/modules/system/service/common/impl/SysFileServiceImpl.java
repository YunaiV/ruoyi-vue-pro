package cn.iocoder.dashboard.modules.system.service.common.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.framework.file.config.FileProperties;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.common.SysFileMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.common.SysFileDO;
import cn.iocoder.dashboard.modules.system.service.common.SysFileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.FILE_PATH_EXISTS;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysFileServiceImpl implements SysFileService {

    @Resource
    private SysFileMapper fileMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public String createFile(String path, byte[] content) {
        if (fileMapper.selectCountById(path) > 0) {
            throw ServiceExceptionUtil.exception(FILE_PATH_EXISTS);
        }
        // 保存到数据库
        SysFileDO file = new SysFileDO();
        file.setId(path);
        file.setContent(content);
        fileMapper.insert(file);
        // 拼接路径返回
        return fileProperties.getBasePath() + path;
    }

    @Override
    public SysFileDO getFile(String path) {
        return fileMapper.selectById(path);
    }

}
