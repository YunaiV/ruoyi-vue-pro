package cn.iocoder.yudao.module.srm.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.json.GuidePriceJson;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.srm.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.srm.dal.redis.ErpRedisKeyConstants.PRODUCT;
import static cn.iocoder.yudao.module.srm.dal.redis.ErpRedisKeyConstants.PRODUCT_LIST;
import static cn.iocoder.yudao.module.srm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.ERP_CUSTOM_RULE_CHANNEL;
import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.ERP_PRODUCT_CHANNEL;

/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */

@Service
@Validated
public class ErpProductServiceImpl implements ErpProductService {

    @Resource(name = ERP_CUSTOM_RULE_CHANNEL)
    MessageChannel ruleChannel;
    @Resource(name = ERP_PRODUCT_CHANNEL)
    MessageChannel productChannel;
    @Resource
    protected ErpProductMapper productMapper;
    @Resource
    ErpProductCategoryService productCategoryService;
    @Resource
    ErpProductUnitService productUnitService;
    @Resource
    DeptApi deptApi;
    @Resource
    AdminUserApi userApi;
    @Resource
    ErpCustomRuleApi erpCustomRuleApi;


    private final ReentrantLock LOCK = new ReentrantLock();

    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, ErpProductBO.class);
    }

    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, ErpProductBO.class);
    }

    public void validateFields(ErpProductSaveReqVO saveReqVO) {
        BeanUtils.areAllNonNullFieldsPresent(saveReqVO, ErpProductBO.class);
    }

    @Override
    @CacheEvict(cacheNames = PRODUCT_LIST, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ErpProductSaveReqVO createReqVO) {
        //TODO 暂时编号不是系统自动生成，后续添加生成规则，流水号的递增由编号来判断，编号相同流水号便自增
        //校验是否存在相同的产品编码
        validateProductCodeUnique(null, createReqVO.getBarCode());
        //检验是否存在相同的产品名称
        validateProductNameUnique(null, createReqVO.getName());
        //校验颜色，型号，系列是否已经有存在相同的产品
        boolean validateProductColorAndSeriesAndModel = validateProductColorAndSeriesAndModel(null, createReqVO.getColor(), createReqVO.getModel(), createReqVO.getSeries());
        if (validateProductColorAndSeriesAndModel) {
            //获取递增后流水号
            Integer serial = increaseSerial(createReqVO.getColor(), createReqVO.getModel(), createReqVO.getSeries());
            createReqVO.setSerial(serial);
        }
        //校验部门id的合法性
        validateDept(createReqVO.getDeptId());
        //校验产品分类是否存在
        validateProductCategory(createReqVO.getCategoryId());
        //校验人员id是否存在
        validatePerson(createReqVO);
        //生产对应的BO
        Object productBO = toBO(createReqVO);
        // 插入产品
        ErpProductDO product = BeanUtils.toBean(productBO, ErpProductDO.class);
        //将图片的实体和指导价的实体转为json字符串
        if (CollUtil.isNotEmpty(createReqVO.getSecondaryImageUrlList())) {
            product.setSecondaryImageUrls(JSONUtil.toJsonStr(createReqVO.getSecondaryImageUrlList()));
        }
        if (CollUtil.isNotEmpty(createReqVO.getGuidePriceList())) {
            product.setGuidePrices(JSONUtil.toJsonStr(createReqVO.getGuidePriceList()));
        }
        //转换国别代码
        if (CollUtil.isNotEmpty(createReqVO.getPatentCountryCodeList())) {
            product.setPatentCountryCodes(JSONUtil.toJsonStr(createReqVO.getPatentCountryCodeList()));
        }
        ThrowUtil.ifSqlThrow(productMapper.insert(product), DB_INSERT_ERROR);
        this.syncProduct(product, false);
        // 返回
        //创建产品的同时创建产品-海关分类表数据。

        return product.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(cacheNames = PRODUCT_LIST, allEntries = true),
        @CacheEvict(cacheNames = PRODUCT, key = "#updateReqVO.id") //返回是null，暂时删除
    })
    public void updateProduct(ErpProductSaveReqVO updateReqVO) {
        Long productId = updateReqVO.getId();
        // 校验存在
        validateProductExists(productId);
        //校验不同的id下是否存在相同的产品编码
        validateProductCodeUnique(updateReqVO.getId(), updateReqVO.getBarCode());
        //检验是否存在相同的产品名称
        validateProductNameUnique(productId, updateReqVO.getName());
        //校验颜色，型号，系列是否已经有存在相同的产品
        boolean validateProductColorAndSeriesAndModel = validateProductColorAndSeriesAndModel(productId, updateReqVO.getColor(), updateReqVO.getModel(), updateReqVO.getSeries());
        if (validateProductColorAndSeriesAndModel) {
            //获取递增后流水号
            Integer serial = increaseSerial(updateReqVO.getColor(), updateReqVO.getModel(), updateReqVO.getSeries());
            updateReqVO.setSerial(serial);
        }
        //校验部门有效性
        validateDept(updateReqVO.getDeptId());
        //获取分类id
        Long categoryId = updateReqVO.getCategoryId();
        //校验产品分类是否存在
        validateProductCategory(categoryId);
        //校验人员id是否存在
        validatePerson(updateReqVO);
        //生产对应的BO
        Object productBO = toBO(updateReqVO);
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(productBO, ErpProductDO.class);
        //将图片的实体和指导价的实体转为json字符串
        if (CollUtil.isNotEmpty(updateReqVO.getSecondaryImageUrlList())) {
            updateObj.setSecondaryImageUrls(JSONUtil.toJsonStr(updateReqVO.getSecondaryImageUrlList()));
        } else {
            updateObj.setSecondaryImageUrls("");
        }
        if (CollUtil.isNotEmpty(updateReqVO.getGuidePriceList())) {
            updateObj.setGuidePrices(JSONUtil.toJsonStr(updateReqVO.getGuidePriceList()));
        } else {
            updateObj.setGuidePrices("");
        }
        //转换国别代码
        if (CollUtil.isNotEmpty(updateReqVO.getPatentCountryCodeList())) {
            updateObj.setPatentCountryCodes(JSONUtil.toJsonStr(updateReqVO.getPatentCountryCodeList()));
        } else {
            updateObj.setPatentCountryCodes("");
        }
        ThrowUtil.ifSqlThrow(productMapper.updateById(updateObj), DB_UPDATE_ERROR);
        this.syncProduct(updateObj, true);
    }

    private void syncProduct(ErpProductDO productDO, Boolean isUpdate) {
        if (isUpdate) {
            //更新产品时->覆盖n个海关规则
            //找到产品id对应的所有海关规则DTO(含海关信息+海关分类)，如果没有海关分类信息(产品逻辑必须有)，那么就不更新海关规则
            List<ErpCustomRuleDTO> dtos = new ArrayList<>();
            // 从产品ID获取海关规则 + 从分类ID获取海关规则
            Optional.ofNullable(erpCustomRuleApi.listDTOsByProductId(productDO.getId()))
                .ifPresent(dtos::addAll); // 如果不为空，添加到dtos列表中
            Optional.ofNullable(productDO.getCustomCategoryId())
                .map(erpCustomRuleApi::getErpCustomRule) // 获取分类ID对应的海关规则DTO
                .ifPresent(dtos::add); // 如果存在DTO，添加到dtos列表中
            // 如果dtos中有数据，则发送消息
            if (!dtos.isEmpty()) {
                ruleChannel.send(MessageBuilder.withPayload(dtos.stream().distinct().toList()).build());
            }
            //获取创建人id
            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
            ErpProductDTO erpProductDTO = BeanUtils.toBean(productDO, ErpProductDTO.class);
            erpProductDTO.setCreator(String.valueOf(loginUserId));
            //同步数据
            productChannel.send(MessageBuilder.withPayload(List.of(erpProductDTO)).build());
        } else {
            //获取创建人id
            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
            ErpProductDTO erpProductDTO = BeanUtils.toBean(productDO, ErpProductDTO.class);
            erpProductDTO.setCreator(String.valueOf(loginUserId));
            //同步数据
            productChannel.send(MessageBuilder.withPayload(List.of(erpProductDTO)).build());
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = PRODUCT_LIST, allEntries = true),
        @CacheEvict(cacheNames = PRODUCT, key = "#id")
    })
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        //TODO 后续如果还存在其他关联，请做校验
        // 删除
        ThrowUtil.ifSqlThrow(productMapper.deleteById(id), DB_DELETE_ERROR);
    }

    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "#ids", unless = "#result == null")
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            ThrowUtil.ifEmptyThrow(product, PRODUCT_NOT_EXISTS);
            //校验产品是否是启用状态
            ThrowUtil.ifThrow(!product.getStatus(), PRODUCT_NOT_ENABLE, product.getName());
        }
        return list;
    }

    public void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductNameUnique(Long id, String name) {
        ErpProductDO product = productMapper.selectByName(name);
        if (ObjUtil.isEmpty(product)) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(PRODUCT_NAME_DUPLICATE);
        }
        if (!product.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
        }
    }

    private void validateProductCodeUnique(Long id, String code) {
        ErpProductDO product = productMapper.selectByCode(code);
        if (ObjUtil.isEmpty(product)) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(PRODUCT_CODE_DUPLICATE);
        }
        if (!product.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
        }
    }

    @Override
    @Cacheable(cacheNames = PRODUCT, key = "#id", unless = "#result == null")
    public ErpProductRespVO getProduct(Long id) {
        ErpProductDO erpProductDO = productMapper.selectById(id);
        ErpProductRespVO productRespVO = BeanUtils.toBean(erpProductDO, ErpProductRespVO.class);
        if (StrUtil.isNotBlank(erpProductDO.getGuidePrices())) {
            productRespVO.setGuidePriceList(JSONUtil.toList(erpProductDO.getGuidePrices(), GuidePriceJson.class));
        }
        if (StrUtil.isNotBlank(erpProductDO.getSecondaryImageUrls())) {
            productRespVO.setSecondaryImageUrlList(JSONUtil.toList(erpProductDO.getSecondaryImageUrls(), String.class));
        }
        if (StrUtil.isNotBlank(erpProductDO.getPatentCountryCodes())) {
            productRespVO.setPatentCountryCodeList(JSONUtil.toList(erpProductDO.getPatentCountryCodes(), Integer.class));
        }
        return productRespVO;
    }

    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "'VO'+#status", unless = "#result == null")
    public List<ErpProductRespVO> getProductVOListByStatus(Boolean status) {
        List<ErpProductDO> list = productMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }
    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "'DTO'+#status", unless = "#result == null")
    public List<ErpProductRespDTO> getProductDTOListByStatus(Boolean status) {
        List<ErpProductDO> erpProductDOs = productMapper.selectListByStatus(status);
        return buildProductDTOList(erpProductDOs);
    }

    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "'VO'+#ids", unless = "#result == null")
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        return buildProductVOList(list);
    }

    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "#ids", unless = "#result == null")
    public List<ErpProductDO> listProducts(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return productMapper.selectBatchIds(ids);
    }

    @Override
    @Cacheable(cacheNames = PRODUCT_LIST, key = "'VO'+#pageReqVO", unless = "#result == null")
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 初始化源列表
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
            convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
            convertSet(list, ErpProductDO::getUnitId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(list, ErpProductDO::getDeptId));
        // 获取用户信息并合并调用
        Map<Long, AdminUserRespDTO> userMaps = Stream.of(
                convertSet(list, ErpProductDO::getProductOwnerId),
                convertSet(list, ErpProductDO::getIndustrialDesignerId),
                convertSet(list, ErpProductDO::getResearchDeveloperId),
                convertSet(list, ErpProductDO::getMaintenanceEngineerId))
            .flatMap(Set::stream)
            .distinct()
            .collect(Collectors.toMap(id -> id, id -> userApi.getUser(id)));
        //创建者map
        Map<Long, AdminUserRespDTO> createrMap = userApi.getUserMap(
            convertSet(list, purchaseOrder -> Long.parseLong(purchaseOrder.getCreator())));
        //更新者map
        Map<Long, AdminUserRespDTO> updaterMap = userApi.getUserMap(
            convertSet(list, purchaseOrder -> Long.parseLong(purchaseOrder.getUpdater())));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                unit -> product.setUnitName(unit.getName()));
            MapUtils.findAndThen(deptMap, product.getDeptId(),
                dept -> product.setDeptName(dept.getName()));
            MapUtils.findAndThen(userMaps, product.getProductOwnerId(),
                user -> product.setProductOwnerName(user.getNickname()));
            MapUtils.findAndThen(userMaps, product.getIndustrialDesignerId(),
                user -> product.setIndustrialDesignerName(user.getNickname()));
            MapUtils.findAndThen(userMaps, product.getResearchDeveloperId(),
                user -> product.setResearchDeveloperName(user.getNickname()));
            MapUtils.findAndThen(userMaps, product.getMaintenanceEngineerId(),
                user -> product.setMaintenanceEngineerName(user.getNickname()));
            Optional.ofNullable(product.getCreator()).ifPresent(creator -> product.setCreator(createrMap.get(Long.parseLong(creator)).getNickname()));
            Optional.ofNullable(product.getUpdater()).ifPresent(updater -> product.setUpdater(updaterMap.get(Long.parseLong(updater)).getNickname()));
            //将指导价转化为集合
            list.stream()
                .filter(productDo -> productDo.getId().equals(product.getId()) && StrUtil.isNotBlank(productDo.getGuidePrices()))
                .findFirst().ifPresent(productDo ->
                    product.setGuidePriceList(JSONUtil.toList(productDo.getGuidePrices(), GuidePriceJson.class)));
            // 将次图转为集合
            list.stream()
                .filter(productDo -> productDo.getId().equals(product.getId()) && StrUtil.isNotBlank(productDo.getSecondaryImageUrls()))
                .findFirst().ifPresent(productDo ->
                    product.setSecondaryImageUrlList(JSONUtil.toList(productDo.getSecondaryImageUrls(), String.class)));
        });
    }

    private List<ErpProductRespDTO> buildProductDTOList(List<ErpProductDO> erpProductDOs) {
        if (CollUtil.isEmpty(erpProductDOs)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(erpProductDOs, ErpProductDO::getUnitId));

        return BeanUtils.toBean(erpProductDOs, ErpProductRespDTO.class, erpProductRespDTO -> {
            MapUtils.findAndThen(unitMap, erpProductRespDTO.getUnitId(),
                    unit -> erpProductRespDTO.setUnitName(unit.getName()));
        });
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return productMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return productMapper.selectCountByUnitId(unitId);
    }

    private boolean validateProductColorAndSeriesAndModel(Long id, String color, String model, String series) {
        List<ErpProductDO> products = productMapper.selectByColorAndSeriesAndModel(color, model, series);
        //如果id不为空，并且包含在集合内，则表示为更新
        if (id != null && products.stream().anyMatch(product -> product.getId().equals(id))) {
            return false;
        }
        return CollUtil.isNotEmpty(products);
    }

    /**
     * @return java.lang.Integer
     * @Author Wqh
     * @Description 根据编码查询出最大的流水号
     * @Date 10:06 2024/10/22
     * @Param [barCode]
     **/
    private Integer increaseSerial(String color, String model, String series) {
        try {
            LOCK.lock();
            ErpProductDO erpProductDO = productMapper.selectMaxSerialByColorAndModelAndSeries(color, model, series);
            if (ObjUtil.isNotEmpty(erpProductDO)) {
                Integer serial = erpProductDO.getSerial();
                //判断序列号是否已超过99，超过则抛出异常
                ThrowUtil.ifThrow(serial >= 99, PRODUCT_SERIAL_OVER_LIMIT);
                return ++serial;
            } else {
                return 0;
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * @return void
     * @Author Wqh
     * @Description 校验传入的分类是否存在
     * @Date 17:15 2024/11/15
     * @Param [categoryId]
     **/
    private void validateProductCategory(Long categoryId) {
        ErpProductCategoryDO productCategory = productCategoryService.getProductCategory(categoryId);
        ThrowUtil.ifEmptyThrow(productCategory, PRODUCT_CATEGORY_NOT_EXISTS);
    }

    /**
     * @Author Wqh
     * @Description 产品相关人员id在系统中是否能查到
     * @Date 9:18 2024/11/18
     * @Param [userIds]
     **/
    private void validatePerson(ErpProductSaveReqVO createReqVO) {
        Long[] userIdArray = {
            createReqVO.getProductOwnerId(),
            createReqVO.getIndustrialDesignerId(),
            createReqVO.getResearchDeveloperId(),
            createReqVO.getMaintenanceEngineerId()
        };
        ThrowUtil.ifThrow(Arrays.stream(userIdArray)
            .filter(Objects::nonNull)
            .anyMatch(userId -> ObjUtil
                .isEmpty(userApi.getUser(userId))), USER_NOT_EXISTS);
    }

    /**
     * @Author Wqh
     * @Description 校验部门的合法性
     * {当前只允许二级部门和三级部门上传}
     * @Date 14:51 2024/10/30
     * @Param [deptId]
     **/
    private void validateDept(Long deptId) {
        List<Integer> levels = Arrays.asList(2, 3);
        //获取部门等级
        Integer level = deptApi.getDeptLevel(deptId);
        //判断登记是否符合要求
        ThrowUtil.ifThrow(!levels.contains(level), DEPT_LEVEL_NOT_MATCH);
    }

    @Override
    public List<Long> listProductIdByBarCode(String barCode) {
        if (StrUtil.isBlank(barCode)) {
            return Collections.emptyList();
        }
        productMapper.selectIdListByBarCode(barCode);
        return List.of();
    }
}