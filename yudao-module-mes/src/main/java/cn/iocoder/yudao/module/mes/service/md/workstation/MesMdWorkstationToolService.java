package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.tool.MesMdWorkstationToolSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationToolDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 工装夹具资源 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdWorkstationToolService {

    /**
     * 创建工装夹具资源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkstationTool(@Valid MesMdWorkstationToolSaveReqVO createReqVO);

    /**
     * 更新工装夹具资源
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkstationTool(@Valid MesMdWorkstationToolSaveReqVO updateReqVO);

    /**
     * 删除工装夹具资源
     *
     * @param id 编号
     */
    void deleteWorkstationTool(Long id);

    /**
     * 获得工装夹具资源列表
     *
     * @param workstationId 工作站编号
     * @return 工装夹具资源列表
     */
    List<MesMdWorkstationToolDO> getWorkstationToolListByWorkstationId(Long workstationId);

    /**
     * 按工作站编号删除工装夹具资源（级联删除）
     *
     * @param workstationId 工作站编号
     */
    void deleteWorkstationToolByWorkstationId(Long workstationId);

    /**
     * 获得指定工具类型的工装夹具资源数量
     *
     * @param toolTypeId 工具类型编号
     * @return 数量
     */
    Long getWorkstationToolCountByToolTypeId(Long toolTypeId);

}
