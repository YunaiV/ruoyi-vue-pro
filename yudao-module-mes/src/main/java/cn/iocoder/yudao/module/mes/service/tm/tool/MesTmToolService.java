package cn.iocoder.yudao.module.mes.service.tm.tool;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.MesTmToolPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.MesTmToolSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 工具台账 Service 接口
 *
 * @author 芋道源码
 */
public interface MesTmToolService {

    /**
     * 创建工具
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTool(@Valid MesTmToolSaveReqVO createReqVO);

    /**
     * 更新工具
     *
     * @param updateReqVO 更新信息
     */
    void updateTool(@Valid MesTmToolSaveReqVO updateReqVO);

    /**
     * 删除工具
     *
     * @param id 编号
     */
    void deleteTool(Long id);

    /**
     * 获得工具
     *
     * @param id 编号
     * @return 工具
     */
    MesTmToolDO getTool(Long id);

    /**
     * 获得工具分页
     *
     * @param pageReqVO 分页查询
     * @return 工具分页
     */
    PageResult<MesTmToolDO> getToolPage(MesTmToolPageReqVO pageReqVO);

    /**
     * 获得工具列表
     *
     * @return 工具列表
     */
    List<MesTmToolDO> getToolList();

    /**
     * 获得工具列表
     *
     * @param ids 编号列表
     * @return 工具列表
     */
    List<MesTmToolDO> getToolList(Collection<Long> ids);

    /**
     * 获得工具 Map
     *
     * @param ids 编号列表
     * @return 工具 Map
     */
    default Map<Long, MesTmToolDO> getToolMap(Collection<Long> ids) {
        return convertMap(getToolList(ids), MesTmToolDO::getId);
    }

}
