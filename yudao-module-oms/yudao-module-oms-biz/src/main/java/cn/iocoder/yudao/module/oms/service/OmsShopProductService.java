package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * OMS店铺产品 Service 接口
 *
 * @author 索迈管理员
 */
public interface OmsShopProductService extends IService<OmsShopProductDO> {


    /**
     * @Description: 按平台创建或更新店铺产品信息
     * @return:
     */
    void createOrUpdateShopProductByPlatform(List<OmsShopProductSaveReqDTO> saveReqDTOs);

    /**
     * @param shopId 店铺ID
     * @Description: 通过店铺ID获取店铺产品列表
     * @return: @return {@link List }<{@link OmsShopProductDO }>
     */
    List<OmsShopProductDO> getByShopId(Long shopId);


    /**
     * 获得分页查询装配好的VO
     **/
    CommonResult<PageResult<OmsShopProductRespVO>> getShopProductPageVO(@Valid OmsShopProductPageReqVO pageReqVO);

    /**
     * 获得OMS 店铺产品分页
     *
     * @param pageReqVO 分页查询
     * @return OMS 店铺产品分页
     */
    PageResult<OmsShopProductDO> getShopProductPage(OmsShopProductPageReqVO pageReqVO);
}