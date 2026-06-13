package cn.iocoder.yudao.module.mes.service.md.client;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 客户 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdClientService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createClient(@Valid MesMdClientSaveReqVO createReqVO);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateClient(@Valid MesMdClientSaveReqVO updateReqVO);

    /**
     * 删除客户
     *
     * @param id 编号
     */
    void deleteClient(Long id);

    /**
     * 获得客户
     *
     * @param id 编号
     * @return 客户
     */
    MesMdClientDO getClient(Long id);

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    PageResult<MesMdClientDO> getClientPage(MesMdClientPageReqVO pageReqVO);

    /**
     * 获得客户列表
     *
     * @param ids 编号列表
     * @return 客户列表
     */
    List<MesMdClientDO> getClientList(Collection<Long> ids);

    /**
     * 获得客户 Map
     *
     * @param ids 编号列表
     * @return 客户 Map
     */
    default Map<Long, MesMdClientDO> getClientMap(Collection<Long> ids) {
        return convertMap(getClientList(ids), MesMdClientDO::getId);
    }

    /**
     * 校验客户是否存在
     *
     * @param id 编号
     */
    void validateClientExists(Long id);

    /**
     * 校验客户存在且启用
     *
     * @param id 编号
     */
    void validateClientExistsAndEnable(Long id);

    /**
     * 批量导入客户
     *
     * @param importClients 导入客户列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    MesMdClientImportRespVO importClientList(List<MesMdClientImportExcelVO> importClients, boolean updateSupport);

}
