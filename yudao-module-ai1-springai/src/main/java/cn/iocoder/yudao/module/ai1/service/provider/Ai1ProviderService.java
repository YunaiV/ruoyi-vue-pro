package cn.iocoder.yudao.module.ai1.service.provider;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import cn.iocoder.yudao.module.ai1.tool.provider.Ai1ProviderTool;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

// TODO @AI：Provider 改成供应商；
/**
 * AI1 Provider Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1ProviderService {

    /**
     * 创建 Provider
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProvider(@Valid Ai1ProviderSaveReqVO createReqVO);

    /**
     * 更新 Provider
     *
     * @param updateReqVO 更新信息
     */
    void updateProvider(@Valid Ai1ProviderSaveReqVO updateReqVO);

    /**
     * 删除 Provider
     *
     * @param id 编号
     */
    void deleteProvider(Long id);

    /**
     * 批量删除 Provider
     *
     * @param ids 编号列表
     */
    void deleteProviderListByIds(List<Long> ids);

    /**
     * 获得 Provider
     *
     * @param id 编号
     * @return Provider
     */
    Ai1ProviderDO getProvider(Long id);

    /**
     * 校验 Provider 是否存在
     *
     * @param id 编号
     * @return Provider
     */
    Ai1ProviderDO validateProviderExists(Long id);

    /**
     * 获得 Provider 分页
     *
     * @param pageReqVO 分页查询
     * @return Provider 分页
     */
    PageResult<Ai1ProviderDO> getProviderPage(Ai1ProviderPageReqVO pageReqVO);

    /**
     * 获得指定状态的 Provider 列表
     *
     * @param status 状态
     * @return Provider 列表
     */
    List<Ai1ProviderDO> getProviderListByStatus(Integer status);

    /**
     * 获得 Provider 列表
     *
     * @param ids 编号集合
     * @return Provider 列表
     */
    List<Ai1ProviderDO> getProviderList(Collection<Long> ids);

    /**
     * 连通测试：GET /models 优先，失败回退 POST /chat/completions
     *
     * @param id 编号
     * @return 探测结果
     */
    Ai1ProviderTool.ConnectResult testConnect(Long id);

    /**
     * 拉取远程可用模型标识列表
     *
     * @param id 编号
     * @return 模型标识列表
     */
    List<String> getRemoteModelList(Long id);

    // TODO @AI：这个是不是应该放到 AiModelService 里呀？因为它本质是模型噢
    /**
     * 解析模型运行时快照
     *
     * @param providerId Provider 编号
     * @param modelId    模型编号
     * @return 运行时快照
     */
    Ai1ProviderRuntime getProviderRuntime(Long providerId, Long modelId);

}
