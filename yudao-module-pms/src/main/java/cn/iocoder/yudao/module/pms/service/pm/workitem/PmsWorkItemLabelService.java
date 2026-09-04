package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 工作项标签 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemLabelService {

    /**
     * 创建工作项标签
     *
     * @param saveReqVO 保存信息
     * @return 标签编号
     */
    Long createWorkItemLabel(PmsWorkItemLabelSaveReqVO saveReqVO);

    /**
     * 更新工作项标签
     *
     * @param saveReqVO 保存信息
     */
    void updateWorkItemLabel(PmsWorkItemLabelSaveReqVO saveReqVO);

    /**
     * 删除工作项标签，工作项中的历史编号在展示时自动忽略
     *
     * @param id 标签编号
     */
    void deleteWorkItemLabel(Long id);

    /**
     * 获得匹配名称的工作项标签列表
     *
     * @param name 标签名称，模糊匹配
     * @return 标签列表
     */
    List<PmsWorkItemLabelDO> getWorkItemLabelList(String name);

    /**
     * 获得工作项标签列表
     *
     * @param ids 标签编号集合
     * @return 标签列表
     */
    List<PmsWorkItemLabelDO> getWorkItemLabelList(Collection<Long> ids);

    /**
     * 获得工作项标签 Map
     *
     * @param ids 标签编号集合
     * @return 工作项标签 Map
     */
    default Map<Long, PmsWorkItemLabelDO> getWorkItemLabelMap(Collection<Long> ids) {
        return convertMap(getWorkItemLabelList(ids), PmsWorkItemLabelDO::getId);
    }

    /**
     * 校验工作项标签编号都存在
     *
     * @param ids 标签编号集合
     */
    void validateWorkItemLabelIds(Collection<Long> ids);

}
