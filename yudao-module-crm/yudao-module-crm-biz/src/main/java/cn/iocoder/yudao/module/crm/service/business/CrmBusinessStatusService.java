package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商机状态 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessStatusService {

    /**
     * 创建商机状态
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBusinessStatus(@Valid CrmBusinessStatusSaveReqVO createReqVO);

    /**
     * 更新商机状态
     *
     * @param updateReqVO 更新信息
     */
    void updateBusinessStatus(@Valid CrmBusinessStatusSaveReqVO updateReqVO);

    /**
     * 删除商机状态
     *
     * @param id 编号
     */
    void deleteBusinessStatusType(Long id);

    /**
     * 获得商机状态组
     *
     * @param id 编号
     * @return 商机状态组
     */
    CrmBusinessStatusTypeDO getBusinessStatusType(Long id);

    /**
     * 校验商机状态组
     *
     * @param id 编号
     */
    void validateBusinessStatusType(Long id);

    /**
     * 获得商机状态组列表
     *
     * @return 商机状态组列表
     */
    List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList();

    /**
     * 获得商机状态组分页
     *
     * @param pageReqVO 分页查询
     * @return 商机状态组分页
     */
    PageResult<CrmBusinessStatusTypeDO> getBusinessStatusTypePage(PageParam pageReqVO);

    /**
     * 获得商机状态组列表
     *
     * @param ids 编号数组
     * @return 商机状态组列表
     */
    List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(Collection<Long> ids);

    /**
     * 获得商机状态组 Map
     *
     * @param ids 编号数组
     * @return 商机状态组 Map
     */
    default Map<Long, CrmBusinessStatusTypeDO> getBusinessStatusTypeMap(Collection<Long> ids) {
        return convertMap(getBusinessStatusTypeList(ids), CrmBusinessStatusTypeDO::getId);
    }

    /**
     * 获得指定类型的商机状态列表
     *
     * @param typeId 商机状态组编号
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusListByTypeId(Long typeId);

    /**
     * 获得商机状态列表
     *
     * @param ids 编号数组
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusList(Collection<Long> ids);

    /**
     * 获得商机状态 Map
     *
     * @param ids 编号数组
     * @return 商机状态 Map
     */
    default Map<Long, CrmBusinessStatusDO> getBusinessStatusMap(Collection<Long> ids) {
        return convertMap(getBusinessStatusList(ids), CrmBusinessStatusDO::getId);
    }

    /**
     * 获得商机状态
     *
     * @param id 编号
     * @return 商机状态
     */
    CrmBusinessStatusDO getBusinessStatus(Long id);

    /**
     * 校验商机状态
     *
     * @param statusTypeId 商机状态组编号
     * @param statusId     商机状态编号
     */
    CrmBusinessStatusDO validateBusinessStatus(Long statusTypeId, Long statusId);

}
