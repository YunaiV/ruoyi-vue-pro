package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 文件 Service 接口
 *
 * @author 芋道源码
 */
public interface InfFileService {

    /**
     * 获得文件分页
     *
     * @param pageReqVO 分页查询
     * @return 文件分页
     */
    PageResult<InfFileDO> getFilePage(InfFilePageReqVO pageReqVO);

}
