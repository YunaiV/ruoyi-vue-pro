package cn.iocoder.yudao.module.oms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductItemDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsProductRespSimpleVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemSaveReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductSaveReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.convert.OmsShopProductConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductItemService;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SHOP_PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_PRODUCT_LACK;

/**
 * OMS店铺产品 Service 实现类
 */
@Service
@Slf4j
public class OmsShopProductServiceImpl extends ServiceImpl<OmsShopProductMapper, OmsShopProductDO> implements OmsShopProductService {
    private final String CREATOR = "Admin";

    @Resource
    @Qualifier("omsShopProductOutputChannel")
    MessageChannel omsShopProductOutputChannel;
    @Resource
    private OmsShopProductMapper shopProductMapper;

    @Resource
    private OmsShopService omsShopService;

    @Resource
    private ErpProductApi erpProductApi;

    @Resource
    private DeptApi deptApi;
    @Resource
    private OmsShopProductItemService omsShopProductItemService;


    @Override
    public List<OmsShopProductDO> getByShopIds(List<Long> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OmsShopProductDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OmsShopProductDO::getShopId, shopIds);
        return shopProductMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public void createOrUpdateShopProductByPlatform(List<OmsShopProductSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            throw exception(OMS_SYNC_SHOP_PRODUCT_LACK);
        }

        List<OmsShopProductDO> shopProductDOs = OmsShopProductConvert.INSTANCE.toOmsShopProductDO(saveReqDTOs);

        List<OmsShopProductDO> createShopProducts = new ArrayList<>();
        List<OmsShopProductDO> updateShopProducts = new ArrayList<>();


        shopProductDOs.forEach(shopProductDO -> {
//            //用创建者区分是否是同步过来的数据还是运营新增的数据
            shopProductDO.setCreator(CREATOR);

            if (shopProductDO.getId() != null) {
                updateShopProducts.add(shopProductDO);
            } else {
                createShopProducts.add(shopProductDO);
            }
        });

        if (CollectionUtil.isNotEmpty(createShopProducts)) {
            shopProductMapper.insertBatch(createShopProducts);
        }

        if (CollectionUtil.isNotEmpty(updateShopProducts)) {
            shopProductMapper.updateById(updateShopProducts);
        }
        log.info("sync shop product success,salesShopId:{},shopProductCount:{}", shopProductDOs.get(0).getShopId(), shopProductDOs.size());
    }

    @Override
    public CommonResult<PageResult<OmsShopProductRespVO>> getShopProductPageVO(OmsShopProductPageReqVO pageReqVO) {

        PageResult<OmsShopProductDO> pageResult = this.getShopProductPage(pageReqVO);
        PageResult<OmsShopProductRespVO> pageResultVO = BeanUtils.toBean(pageResult, OmsShopProductRespVO.class);
        Set<Long> shopIds = StreamX.from(pageResultVO.getList()).toSet(OmsShopProductRespVO::getShopId);
        Map<Long, OmsShopRespVO> shopVoMap = omsShopService.getShopMapByIds(shopIds);
        // 装配对象
        StreamX.from(pageResultVO.getList()).assemble(shopVoMap, OmsShopProductRespVO::getShopId, OmsShopProductRespVO::setShop);

        List<Long> deptIds = StreamX.from(pageResultVO.getList()).filter(ObjectUtil::isNotNull).toList(OmsShopProductRespVO::getDeptId);
        List<DeptRespDTO> deptDTOList = deptApi.getDeptList(deptIds);
        deptDTOList = StreamX.from(deptDTOList).filter(ObjectUtil::isNotNull).toList();
        StreamX.from(pageResultVO.getList()).assemble(deptDTOList, DeptRespDTO::getId, OmsShopProductRespVO::getDeptId, (prod, dept) -> {
            if (dept != null) {
                prod.setDeptName(dept.getName());
            }
        });
        //装配产品编码*数量
        StreamX.from(pageResultVO.getList()).assemble(pageResultVO.getList(), OmsShopProductRespVO::getId, OmsShopProductRespVO::getId, (prod, original) -> {
            List<OmsShopProductItemRespVO> items = getShopProductVoModel(original.getId()).getItems();
            if (CollectionUtil.isEmpty(items)) {
                return;
            }
            String productCodeAndQty = items.stream().map(item -> item.getProduct().getBarCode() + "*" + item.getQty()).collect(Collectors.joining(", "));
            prod.setProductCodeAndQty(productCodeAndQty);
        });
        return success(pageResultVO);
    }

    @Override
    public PageResult<OmsShopProductDO> getShopProductPage(OmsShopProductPageReqVO pageReqVO) {
        return shopProductMapper.selectPage(pageReqVO);
    }


    @Override
    public OmsShopProductRespVO getShopProductVoModel(Long id) {
        OmsShopProductDO shopProduct = shopProductMapper.selectById(id);
        if (shopProduct == null) {
            throw exception(OMS_SHOP_PRODUCT_NOT_EXISTS);
        }
        OmsShopProductRespVO respVO = BeanUtils.toBean(shopProduct, OmsShopProductRespVO.class);
        List<OmsShopProductItemDO> items = omsShopProductItemService.getShopProductItemsByProductId(shopProduct.getId());
        List<OmsShopProductItemRespVO> itemRespVOS = BeanUtils.toBean(items, OmsShopProductItemRespVO.class);
        List<ErpProductRespDTO> productList = erpProductApi.getProductVOList(StreamX.from(items).map(OmsShopProductItemDO::getProductId).toList());
        List<OmsProductRespSimpleVO> simpleProductList = BeanUtils.toBean(productList, OmsProductRespSimpleVO.class);
        StreamX.from(itemRespVOS).assemble(simpleProductList, OmsProductRespSimpleVO::getId, OmsShopProductItemRespVO::getProductId, OmsShopProductItemRespVO::setProduct);
        respVO.setItems(itemRespVOS);
        // 店铺
        OmsShopRespVO shopDO = omsShopService.getShopById(respVO.getShopId());
        respVO.setShop(BeanUtils.toBean(shopDO, OmsShopRespVO.class));
        //设置部门信息
        if (respVO.getDeptId() != null) {
            DeptRespDTO deptDTO = deptApi.getDept(respVO.getDeptId());
            if (deptDTO != null) {
                respVO.setDeptName(deptDTO.getName());
            }
        }
        return respVO;
    }

    @Transactional
    @Override
    public Long createShopProductWithItems(OmsShopProductSaveReqVO createReqVO) {
        Long id = createShopProduct(createReqVO);
        createReqVO.setId(id);
        updateShopProductWithItems(createReqVO);
        sendToEccang(createReqVO.getId());
        return id;
    }

    @Override
    public Long createShopProduct(OmsShopProductSaveReqVO createReqVO) {
        // 插入
        OmsShopProductDO shopProduct = BeanUtils.toBean(createReqVO, OmsShopProductDO.class);
        shopProduct.setExternalId(shopProduct.getCode() + shopProduct.getShopId());
        shopProductMapper.insert(shopProduct);
        // 返回
        return shopProduct.getId();
    }

    @Override
    @Transactional
    public void updateShopProductWithItems(OmsShopProductSaveReqVO updateReqVO) {
        // 保存主表
        this.updateShopProduct(updateReqVO);
        // 处理重表数据
        List<OmsShopProductItemDO> itemsInDB = omsShopProductItemService.getShopProductItemsByProductId(updateReqVO.getId());
        Set<Long> itemIdsInDB = StreamX.from(itemsInDB).toSet(OmsShopProductItemDO::getId);
        Set<Long> itemIdsToDelete = new HashSet<>(itemIdsInDB);

        // 循环处理
        for (OmsShopProductItemSaveReqVO itemVO : updateReqVO.getItems()) {
            // 此ID不使用前端传入的ID
            itemVO.setShopProductId(updateReqVO.getId());
            // id为空的需要新增
            if (itemVO.getId() == null) {
                omsShopProductItemService.createShopProductItem(itemVO);
            } else {
                omsShopProductItemService.updateShopProductItem(itemVO);
            }
            // 移除不需要删除的ID
            itemIdsToDelete.remove(itemVO.getId());
        }

        omsShopProductItemService.deleteShopProductItemsByIds(new ArrayList<>(itemIdsToDelete));


        sendToEccang(updateReqVO.getId());

    }

    private void sendToEccang(Long id) {
        OmsShopProductRespVO productVo = this.getShopProductVoModel(id);
        OmsShopProductDTO productDTO = OmsShopProductConvert.INSTANCE.toOmsShopProductDTO(productVo);
        // 条件判断
        if (productDTO == null) {
            return;
        }


        // 店铺需要维护别名
        if (StrUtils.isEmpty(productDTO.getShop().getName())) {
            return;
        }


        // 与产品有关联关系
        if (!CollectionUtils.isEmpty(productDTO.getItems())) {
            for (OmsShopProductItemDTO item : productDTO.getItems()) {
                if (item.getProduct() == null) {
                    return;
                }
            }
        }

        omsShopProductOutputChannel.send(MessageBuilder.withPayload(productDTO).build());

    }

    @Override
    public void updateShopProduct(OmsShopProductSaveReqVO updateReqVO) {
        // 校验存在
        validateShopProductExists(updateReqVO.getId());
        // 更新
        OmsShopProductDO updateObj = BeanUtils.toBean(updateReqVO, OmsShopProductDO.class);
        shopProductMapper.updateById(updateObj);
    }

    private void validateShopProductExists(Long id) {
        if (shopProductMapper.selectById(id) == null) {
            throw exception(OmsErrorCodeConstants.OMS_SHOP_PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public void deleteShopProduct(Long id) {
        // 校验存在
        validateShopProductExists(id);
        // 删除
        shopProductMapper.deleteById(id);
    }

}