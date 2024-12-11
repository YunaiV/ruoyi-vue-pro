package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.dal.supporting.VirtualTableInit;
import cn.iocoder.yudao.module.erp.service.product.tvstand.ErpProductTvStandServiceImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@RequiredArgsConstructor
public class ErpProductServiceImpl implements ErpProductService {
    @Resource
    MessageChannel erpProductChannel;
    private final ErpProductMapper productMapper;
    private final ErpProductCategoryService productCategoryService;
    private final ErpProductUnitService productUnitService;
    private final DeptApi deptApi;
    private final ErpCustomRuleMapper customRuleMapper;
    private static final ReentrantLock LOCK = new ReentrantLock();
    private final AdminUserApi userApi;
    private final ApplicationContext applicationContext;




    @Override
    public Long createProduct(ErpProductSaveReqVO createReqVO) {
        //TODO 暂时编号不是系统自动生成，后续添加生成规则，流水号的递增由编号来判断，编号相同流水号便自增
        //校验是否存在相同的产品编码
        validateProductCodeUnique(null, createReqVO.getBarCode());
        //校验颜色，型号，系列是否已经有存在相同的产品
        boolean b = validateProductColorAndSeriesAndModel(null,createReqVO.getColor(), createReqVO.getModel(), createReqVO.getSeries());
        if (b){
            //获取递增后流水号
            Integer serial = increaseSerial(createReqVO.getColor(), createReqVO.getModel(), createReqVO.getSeries());
            createReqVO.setSerial(serial);
        }
        //校验部门id的合法性
        validateDept(createReqVO.getDeptId());
        //获取分类id
        Long categoryId = createReqVO.getCategoryId();
        //校验产品分类是否存在
        validateProductCategory(categoryId);
        //校验人员id是否存在
        validatePerson(createReqVO.getProductOwnerId(), createReqVO.getIndustrialDesignerId(), createReqVO.getResearchDeveloperId(), createReqVO.getMaintenanceEngineerId());
        //获取产品分类虚拟表
        Class<?> aClass = VirtualTableInit.getTableMap().get(categoryId);
        if (aClass != null){
            extraServiceImpl(aClass,createReqVO);
        }
        // 插入产品
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class);
        //将图片的实体和指导价的实体转为json字符串
        if (CollUtil.isNotEmpty(createReqVO.getImageUrl())){
            product.setImageUrl(JSONUtil.toJsonStr(createReqVO.getImageUrl()));
        }
        if (CollUtil.isNotEmpty(createReqVO.getGuidePrice())){
            product.setGuidePrice(JSONUtil.toJsonStr(createReqVO.getGuidePrice()));
        }
        ThrowUtil.ifSqlThrow(productMapper.insert(product),DB_INSERT_ERROR);
        // 返回
        return product.getId();
    }

    private void extraServiceImpl(Class<?> aClass, ErpProductSaveReqVO vo) {
        try {
            Object serviceImpl = applicationContext.getBean(aClass);
            if (serviceImpl instanceof ErpProductTvStandServiceImpl){
                ((ErpProductTvStandServiceImpl)serviceImpl).validationField(vo);
            }
            //TODO 其他类型请在此添加else if
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProduct(ErpProductSaveReqVO updateReqVO) {
        Long id = updateReqVO.getId();
        // 校验存在
        validateProductExists(id);
        //校验不同的id下是否存在相同的产品编码
        validateProductCodeUnique(updateReqVO.getId(), updateReqVO.getBarCode());
        //校验颜色，型号，系列是否已经有存在相同的产品
        boolean b = validateProductColorAndSeriesAndModel(id,updateReqVO.getColor(), updateReqVO.getModel(), updateReqVO.getSeries());
        if (b){
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
        validatePerson(updateReqVO.getProductOwnerId(), updateReqVO.getIndustrialDesignerId(), updateReqVO.getResearchDeveloperId(), updateReqVO.getMaintenanceEngineerId());
        //获取产品分类虚拟表
        Class<?> aClass = VirtualTableInit.getTableMap().get(categoryId);
        if (aClass != null){
            extraServiceImpl(aClass,updateReqVO);
        }
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        //将图片的实体和指导价的实体转为json字符串
        if (CollUtil.isNotEmpty(updateReqVO.getImageUrl())){
            updateObj.setImageUrl(JSONUtil.toJsonStr(updateReqVO.getImageUrl()));
        }
        if (CollUtil.isNotEmpty(updateReqVO.getGuidePrice())){
            updateObj.setGuidePrice(JSONUtil.toJsonStr(updateReqVO.getGuidePrice()));
        }
        ThrowUtil.ifSqlThrow(productMapper.updateById(updateObj),DB_UPDATE_ERROR);
        //同步数据
        var dtos = customRuleMapper.selectProductAllInfoListById(id);
        erpProductChannel.send(MessageBuilder.withPayload(dtos).build());
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        //TODO 后续如果还存在其他关联，请做校验
        // 删除
        ThrowUtil.ifSqlThrow(productMapper.deleteById(id),DB_DELETE_ERROR);
    }

    @Override
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
            ThrowUtil.ifThrow(!product.getStatus(), PRODUCT_NOT_ENABLE,product.getName());
        }
        return list;
    }

    public void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductCodeUnique(Long id, String code) {
        ErpProductDO product = productMapper.selectByCode(code);
        if (ObjUtil.isEmpty(product)){
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null){
            throw exception(PRODUCT_CODE_DUPLICATE);
        }
        if (!product.getId().equals(id)) {
            throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
        }
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        List<ErpProductDO> list = productMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(list, ErpProductDO::getDeptId));
        Map<Long, AdminUserRespDTO> poUserMap = userApi.getUserMap(convertSet(list, ErpProductDO::getProductOwnerId));
        Map<Long, AdminUserRespDTO> idUserMap = userApi.getUserMap(convertSet(list, ErpProductDO::getIndustrialDesignerId));
        Map<Long, AdminUserRespDTO> rdUserMap = userApi.getUserMap(convertSet(list, ErpProductDO::getResearchDeveloperId));
        Map<Long, AdminUserRespDTO> meUserMap = userApi.getUserMap(convertSet(list, ErpProductDO::getMaintenanceEngineerId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> product.setUnitName(unit.getName()));
            MapUtils.findAndThen(deptMap, product.getDeptId(),
                    dept -> product.setDeptName(dept.getName()));
            MapUtils.findAndThen(poUserMap, product.getProductOwnerId(),
                    user -> product.setProductManagerName(user.getNickname()));
            MapUtils.findAndThen(idUserMap, product.getIndustrialDesignerId(),
                    user -> product.setIndustrialDesignerName(user.getNickname()));
            MapUtils.findAndThen(rdUserMap, product.getResearchDeveloperId(),
                    user -> product.setResearchDeveloperName(user.getNickname()));
            MapUtils.findAndThen(meUserMap, product.getMaintenanceEngineerId(),
                    user -> product.setMaintenanceEngineerName(user.getNickname()));
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

    private boolean validateProductColorAndSeriesAndModel(Long id,String color, String model, String series) {
        List<ErpProductDO> products = productMapper.selectByColorAndSeriesAndModel(color,model,series);
        //如果id不为空，并且包含在集合内，则表示为更新
        if (id != null && products.stream().anyMatch(product -> product.getId().equals(id))){
            return false;
        }
        return CollUtil.isNotEmpty(products);
    }

    /**
     * @Author Wqh
     * @Description 根据编码查询出最大的流水号
     * @Date 10:06 2024/10/22
     * @Param [barCode]
     * @return java.lang.Integer
     **/
    private Integer increaseSerial(String color, String model, String series) {
        try {
            LOCK.lock();
            ErpProductDO erpProductDO = productMapper.selectMaxSerialByColorAndModelAndSeries(color, model, series);
            if (ObjUtil.isNotEmpty(erpProductDO)){
                Integer serial = erpProductDO.getSerial();
                //判断序列号是否已超过99，超过则抛出异常
                ThrowUtil.ifThrow(serial >= 99,PRODUCT_SERIAL_OVER_LIMIT);
                return ++serial;
            }else {
                return 0;
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * @Author Wqh
     * @Description 校验传入的分类是否存在
     * @Date 17:15 2024/11/15
     * @Param [categoryId]
     * @return void
     **/
    private void validateProductCategory(Long categoryId) {
        ErpProductCategoryDO productCategory = productCategoryService.getProductCategory(categoryId);
        ThrowUtil.ifEmptyThrow(productCategory,PRODUCT_CATEGORY_NOT_EXISTS);
    }

    /**
     * @Author Wqh
     * @Description 产品相关人员id在系统中是否能查到
     * @Date 9:18 2024/11/18
     * @Param [userIds]
     **/
    private void validatePerson(Long ... userIds) {
        ThrowUtil.ifThrow(Arrays.stream(userIds)
                .filter(Objects::nonNull)
                .anyMatch(userId -> ObjUtil
                        .isEmpty(userApi.getUser(userId))), USER_NOT_EXISTS);
    }

    /**
     * @Author Wqh
     * @Description 校验部门的合法性
    {当前只允许二级部门和三级部门上传}
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
}