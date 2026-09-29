package cn.iocoder.yudao.module.ai1.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI1 模型 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1ModelService {

    /**
     * 创建模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModel(@Valid Ai1ModelSaveReqVO createReqVO);

    /**
     * 更新模型
     *
     * @param updateReqVO 更新信息
     */
    void updateModel(@Valid Ai1ModelSaveReqVO updateReqVO);

    /**
     * 删除模型
     *
     * @param id 编号
     */
    void deleteModel(Long id);

    /**
     * 批量删除模型
     *
     * @param ids 编号列表
     */
    void deleteModelListByIds(List<Long> ids);

    /**
     * 获得模型
     *
     * @param id 编号
     * @return 模型
     */
    Ai1ModelDO getModel(Long id);

    /**
     * 校验模型是否存在
     *
     * @param id 编号
     * @return 模型
     */
    Ai1ModelDO validateModelExists(Long id);

    /**
     * 获得模型分页
     *
     * @param pageReqVO 分页查询
     * @return 模型分页
     */
    PageResult<Ai1ModelDO> getModelPage(Ai1ModelPageReqVO pageReqVO);

    /**
     * 获得指定供应商下的模型列表
     *
     * @param providerId 供应商编号
     * @return 模型列表
     */
    List<Ai1ModelDO> getModelListByProviderId(Long providerId);

    /**
     * 获得指定供应商、类型、状态的模型列表
     *
     * @param providerId 供应商编号，为空时不过滤
     * @param type       类型，为空时不过滤
     * @param status     状态
     * @return 模型列表
     */
    List<Ai1ModelDO> getModelListByProviderIdAndTypeAndStatus(Long providerId, Integer type, Integer status);

    /**
     * 获得模型列表
     *
     * @param ids 编号集合
     * @return 模型列表
     */
    List<Ai1ModelDO> getModelList(Collection<Long> ids);

    /**
     * 获得指定编号的模型 Map
     *
     * @param ids 编号集合
     * @return 模型 Map
     */
    default Map<Long, Ai1ModelDO> getModelMap(Collection<Long> ids) {
        return convertMap(getModelList(ids), Ai1ModelDO::getId);
    }

    /**
     * 获得指定供应商下的模型数量
     *
     * @param providerIds 供应商编号集合
     * @return 模型数量
     */
    Long getModelCountByProviderIds(Collection<Long> providerIds);

    /**
     * 拉取供应商远程可用的模型标识列表
     *
     * @param providerId 供应商编号
     * @return 模型标识列表
     */
    List<String> getRemoteModelList(Long providerId);

    /**
     * 批量导入远程模型
     *
     * @param importReqVO 导入信息
     * @return 实际导入数量
     */
    Integer importRemoteModelList(@Valid Ai1ModelImportReqVO importReqVO);

}
