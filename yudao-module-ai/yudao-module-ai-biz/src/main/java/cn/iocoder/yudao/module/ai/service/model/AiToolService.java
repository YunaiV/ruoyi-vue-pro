package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.tool.AiToolPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.tool.AiToolSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiToolDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI 工具 Service 接口
 *
 * @author 芋道源码
 */
public interface AiToolService {

    /**
     * 创建工具
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTool(@Valid AiToolSaveReqVO createReqVO);

    /**
     * 更新工具
     *
     * @param updateReqVO 更新信息
     */
    void updateTool(@Valid AiToolSaveReqVO updateReqVO);

    /**
     * 删除工具
     *
     * @param id 编号
     */
    void deleteTool(Long id);

    /**
     * 校验工具是否存在
     *
     * @param id 编号
     */
    void validateToolExists(Long id);

    /**
     * 获得工具
     *
     * @param id 编号
     * @return 工具
     */
    AiToolDO getTool(Long id);

    /**
     * 获得工具列表
     *
     * @param ids 编号列表
     * @return 工具列表
     */
    List<AiToolDO> getToolList(Collection<Long> ids);

    /**
     * 获得工具分页
     *
     * @param pageReqVO 分页查询
     * @return 工具分页
     */
    PageResult<AiToolDO> getToolPage(AiToolPageReqVO pageReqVO);

    /**
     * 获得工具列表
     *
     * @param status 状态
     * @return 工具列表
     */
    List<AiToolDO> getToolListByStatus(Integer status);

}