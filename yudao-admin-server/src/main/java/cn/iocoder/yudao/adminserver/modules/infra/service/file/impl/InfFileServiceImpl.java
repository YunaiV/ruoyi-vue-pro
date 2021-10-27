package cn.iocoder.yudao.adminserver.modules.infra.service.file.impl;

import cn.iocoder.yudao.adminserver.modules.infra.dal.mysql.file.InfFileMapper;
import cn.iocoder.yudao.adminserver.modules.infra.service.file.InfFileService;
import cn.iocoder.yudao.adminserver.modules.infra.controller.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class InfFileServiceImpl implements InfFileService {

    @Resource
    private InfFileMapper fileMapper;

    @Override
    public PageResult<InfFileDO> getFilePage(InfFilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

}
