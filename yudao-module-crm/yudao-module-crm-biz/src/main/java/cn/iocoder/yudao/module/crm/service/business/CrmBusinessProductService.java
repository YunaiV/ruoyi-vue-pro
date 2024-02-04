package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;

import java.util.List;

/**
 * 商机产品关联表 Service 接口
 *
 * @author lzxhqs
 */
public interface CrmBusinessProductService {

    /**
     * 批量新增商机产品关联数据
     *
     * @param list 商机产品集合
     */
    void createBusinessProductBatch(List<CrmBusinessProductDO> list);

    /**
     * 批量更新商机产品表
     *
     * @param list 商机产品数据集合
     */
    void updateBusinessProductBatch(List<CrmBusinessProductDO> list);

    /**
     * 批量删除
     *
     * @param list 需要删除的商机产品集合
     */
    void deleteBusinessProductBatch(List<CrmBusinessProductDO> list);

    /**
     * 根据商机编号，删除商机产品关联数据
     *
     * @param businessId 商机id
     */
    void deleteBusinessProductByBusinessId(Long businessId);

    /**
     * 根据商机编号，获取商机产品关联数据集合
     *
     * @param businessId 商机编号
     */
    List<CrmBusinessProductDO> getBusinessProductListByBusinessId(Long businessId);

    /**
     * 根据合同编号，获得合同关联的商品列表
     *
     * @param contractId 合同编号
     * @return 关联的商品列表
     */
    List<CrmBusinessProductDO> getBusinessProductListByContractId(Long contractId);

}
