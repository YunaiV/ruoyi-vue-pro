package cn.iocoder.yudao.module.ai1.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI1 供应商 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1ProviderService {

    /**
     * 创建供应商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProvider(@Valid Ai1ProviderSaveReqVO createReqVO);

    /**
     * 更新供应商
     *
     * @param updateReqVO 更新信息
     */
    void updateProvider(@Valid Ai1ProviderSaveReqVO updateReqVO);

    /**
     * 删除供应商
     *
     * @param id 编号
     */
    void deleteProvider(Long id);

    /**
     * 批量删除供应商
     *
     * @param ids 编号列表
     */
    void deleteProviderListByIds(List<Long> ids);

    /**
     * 获得供应商
     *
     * @param id 编号
     * @return 供应商
     */
    Ai1ProviderDO getProvider(Long id);

    /**
     * 校验供应商是否存在
     *
     * @param id 编号
     * @return 供应商
     */
    Ai1ProviderDO validateProviderExists(Long id);

    /**
     * 获得供应商分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商分页
     */
    PageResult<Ai1ProviderDO> getProviderPage(Ai1ProviderPageReqVO pageReqVO);

    /**
     * 获得指定状态的供应商列表
     *
     * @param status 状态
     * @return 供应商列表
     */
    List<Ai1ProviderDO> getProviderListByStatus(Integer status);

    /**
     * 获得供应商列表
     *
     * @param ids 编号集合
     * @return 供应商列表
     */
    List<Ai1ProviderDO> getProviderList(Collection<Long> ids);

    /**
     * 获得供应商 Map
     *
     * @param ids 编号集合
     * @return 供应商 Map
     */
    default Map<Long, Ai1ProviderDO> getProviderMap(Collection<Long> ids) {
        return convertMap(getProviderList(ids), Ai1ProviderDO::getId);
    }

    /**
     * 测试供应商连通性
     *
     * @param id 编号
     * @return 测试结果
     */
    Ai1ProviderConnectRespVO testProviderConnect(Long id);

}
