package cn.iocoder.yudao.module.mes.service.tm.tool;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolTypeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 工具类型 Service 接口
 *
 * @author 芋道源码
 */
public interface MesTmToolTypeService {

    /**
     * 创建工具类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createToolType(@Valid MesTmToolTypeSaveReqVO createReqVO);

    /**
     * 更新工具类型
     *
     * @param updateReqVO 更新信息
     */
    void updateToolType(@Valid MesTmToolTypeSaveReqVO updateReqVO);

    /**
     * 删除工具类型
     *
     * @param id 编号
     */
    void deleteToolType(Long id);

    /**
     * 获得工具类型
     *
     * @param id 编号
     * @return 工具类型
     */
    MesTmToolTypeDO getToolType(Long id);

    /**
     * 获得工具类型分页
     *
     * @param pageReqVO 分页查询
     * @return 工具类型分页
     */
    PageResult<MesTmToolTypeDO> getToolTypePage(MesTmToolTypePageReqVO pageReqVO);

    /**
     * 获得工具类型列表
     *
     * @return 工具类型列表
     */
    List<MesTmToolTypeDO> getToolTypeList();

    /**
     * 校验工具类型是否存在
     *
     * @param id 编号
     */
    void validateToolTypeExists(Long id);

    /**
     * 获得工具类型列表
     *
     * @param ids 编号数组
     * @return 工具类型列表
     */
    List<MesTmToolTypeDO> getToolTypeList(Collection<Long> ids);

    /**
     * 获得工具类型 Map
     *
     * @param ids 编号数组
     * @return 工具类型 Map
     */
    default Map<Long, MesTmToolTypeDO> getToolTypeMap(Collection<Long> ids) {
        return convertMap(getToolTypeList(ids), MesTmToolTypeDO::getId);
    }

}
