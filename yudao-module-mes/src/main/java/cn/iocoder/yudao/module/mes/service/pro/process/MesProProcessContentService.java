package cn.iocoder.yudao.module.mes.service.pro.process;

import cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content.MesProProcessContentSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessContentDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * MES 生产工序内容 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProProcessContentService {

    /**
     * 创建生产工序内容
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProcessContent(@Valid MesProProcessContentSaveReqVO createReqVO);

    /**
     * 更新生产工序内容
     *
     * @param updateReqVO 更新信息
     */
    void updateProcessContent(@Valid MesProProcessContentSaveReqVO updateReqVO);

    /**
     * 删除生产工序内容
     *
     * @param id 编号
     */
    void deleteProcessContent(Long id);

    /**
     * 获得生产工序内容
     *
     * @param id 编号
     * @return 生产工序内容
     */
    MesProProcessContentDO getProcessContent(Long id);

    /**
     * 获得生产工序内容列表（按工序编号）
     *
     * @param processId 工序编号
     * @return 生产工序内容列表
     */
    List<MesProProcessContentDO> getProcessContentListByProcessId(Long processId);

    /**
     * 删除生产工序内容（按工序编号）
     *
     * @param processId 工序编号
     */
    void deleteProcessContentByProcessId(Long processId);

}
