package cn.iocoder.yudao.module.crm.service.business;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.product.CrmBusinessProductSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductSaveReqVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.convert.businessproduct.CrmBusinessProductConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessProductMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.contactbusinesslink.CrmContactBusinessMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.CrmContractMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactBusinessService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_CONTRACT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;

/**
 * 商机 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessServiceImpl implements CrmBusinessService {

    @Resource
    private CrmBusinessMapper businessMapper;

    @Resource
    private CrmBusinessProductMapper businessProductMapper;
    @Resource
    private CrmContractMapper contractMapper;

    @Resource
    private CrmContactBusinessMapper contactBusinessMapper;
    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private CrmContactBusinessService contactBusinessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_BUSINESS_TYPE, subType = CRM_BUSINESS_CREATE_SUB_TYPE, bizNo = "{{#business.id}}",
            success = CRM_BUSINESS_CREATE_SUCCESS)
    public Long createBusiness(CrmBusinessSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1. 插入商机
        CrmBusinessDO business = BeanUtils.toBean(createReqVO, CrmBusinessDO.class)
                .setOwnerUserId(userId);
        businessMapper.insert(business);
        // TODO 商机待定：插入商机与产品的关联表；校验商品存在
        verifyCrmBusinessProduct(business.getId());
        if (!createReqVO.getProducts().isEmpty()) {
            createBusinessProducts(createReqVO.getProducts(), business.getId());
        }
        // TODO 商机待定：在联系人的详情页，如果直接【新建商机】，则需要关联下。这里要搞个 CrmContactBusinessDO 表
        createContactBusiness(business.getId(), createReqVO.getContactId());

        // 2. 创建数据权限
        // 设置当前操作的人为负责人
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_BUSINESS.getType())
                .setBizId(business.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("business", business);
        return business.getId();
    }
    /**
     * @param businessId 商机id
     * @param contactId 联系人id
     * @throws
     * @description 联系人与商机的关联
     * @author lzxhqs
     */
    private void createContactBusiness(Long businessId, Long contactId) {
        CrmContactBusinessDO contactBusiness = new CrmContactBusinessDO();
        contactBusiness.setBusinessId(businessId);
        contactBusiness.setContactId(contactId);
        contactBusinessMapper.insert(contactBusiness);

    }

    /**
     * @param products 产品集合
     * @description 插入商机产品关联表
     * @author lzxhqs
     */
    private void createBusinessProducts(List<CrmBusinessProductSaveReqVO> products, Long businessId) {
        List<CrmBusinessProductDO> list = new ArrayList<>();
        for (CrmBusinessProductSaveReqVO product : products) {
            CrmBusinessProductDO businessProductDO = CrmBusinessProductConvert.INSTANCE.convert(product);
            businessProductDO.setBusinessId(businessId);
            list.add(businessProductDO);
        }
        businessProductMapper.insertBatch(list);
    }

    /**
     * @param id businessId
     * @description 校验管理的产品存在则删除
     * @author lzxhqs
     */
    private void verifyCrmBusinessProduct(Long id) {
        CrmBusinessProductDO businessProductDO = businessProductMapper.selectByBusinessId(id);
        if (businessProductDO != null) {
            //通过商机Id删除
            businessProductMapper.deleteByBusinessId(id);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_BUSINESS_TYPE, subType = CRM_BUSINESS_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_BUSINESS_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateBusiness(CrmBusinessSaveReqVO updateReqVO) {
        // 1. 校验存在
        CrmBusinessDO oldBusiness = validateBusinessExists(updateReqVO.getId());

        // 2. 更新商机
        CrmBusinessDO updateObj = BeanUtils.toBean(updateReqVO, CrmBusinessDO.class);
        businessMapper.updateById(updateObj);
        // TODO 商机待定：插入商机与产品的关联表；校验商品存在
        verifyCrmBusinessProduct(updateReqVO.getId());
        if (!updateReqVO.getProducts().isEmpty()) {
            createBusinessProducts(updateReqVO.getProducts(), updateReqVO.getId());
        }

        // TODO @商机待定：如果状态发生变化，插入商机状态变更记录表
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldBusiness, CrmBusinessSaveReqVO.class));
        LogRecordContext.putVariable("businessName", oldBusiness.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_BUSINESS_TYPE, subType = CRM_BUSINESS_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_BUSINESS_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteBusiness(Long id) {
        // 校验存在
        CrmBusinessDO business = validateBusinessExists(id);
        // TODO @商机待定：需要校验有没关联合同。CrmContractDO 的 businessId 字段
        validateContractExists(id);

        // 删除
        businessMapper.deleteById(id);
        // 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_BUSINESS.getType(), id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("businessName", business.getName());
    }

    /**
     * @param businessId 商机id
     * @throws
     * @description 删除校验合同是关联合同
     * @author lzxhqs
     */
    private void validateContractExists(Long businessId) {
        CrmContractDO contract = contractMapper.selectByBizId(businessId);
        if(contract != null) {
            throw exception(BUSINESS_CONTRACT_EXISTS);
        }
    }

    private CrmBusinessDO validateBusinessExists(Long id) {
        CrmBusinessDO crmBusiness = businessMapper.selectById(id);
        if (crmBusiness == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        return crmBusiness;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_BUSINESS_TYPE, subType = CRM_BUSINESS_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_BUSINESS_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferBusiness(CrmBusinessTransferReqVO reqVO, Long userId) {
        // 1 校验商机是否存在
        CrmBusinessDO business = validateBusinessExists(reqVO.getId());

        // 2.1 数据权限转移
        permissionService.transferPermission(
                CrmBusinessConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_BUSINESS.getType()));
        // 2.2 设置新的负责人
        businessMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 记录操作日志上下文
        LogRecordContext.putVariable("business", business);
    }

    //======================= 查询相关 =======================

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmBusinessDO getBusiness(Long id) {
        return businessMapper.selectById(id);
    }

    @Override
    public List<CrmBusinessDO> getBusinessList(Collection<Long> ids, Long userId) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return businessMapper.selectBatchIds(ids, userId);
    }

    @Override
    public List<CrmBusinessDO> getBusinessList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return businessMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmBusinessDO> getBusinessPage(CrmBusinessPageReqVO pageReqVO, Long userId) {
        return businessMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmBusinessDO> getBusinessPageByCustomerId(CrmBusinessPageReqVO pageReqVO) {
        return businessMapper.selectPageByCustomerId(pageReqVO);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#pageReqVO.contactId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmBusinessDO> getBusinessPageByContact(CrmBusinessPageReqVO pageReqVO) {
        // 1. 查询关联的商机编号
        List<CrmContactBusinessDO> contactBusinessList = contactBusinessService.getContactBusinessListByContactId(
                pageReqVO.getContactId());
        if (CollUtil.isEmpty(contactBusinessList)) {
            return PageResult.empty();
        }
        // 2. 查询商机分页
        return businessMapper.selectPageByContactId(pageReqVO,
                convertSet(contactBusinessList, CrmContactBusinessDO::getBusinessId));
    }

    @Override
    public Long getBusinessCountByCustomerId(Long customerId) {
        return businessMapper.selectCount(CrmBusinessDO::getCustomerId, customerId);
    }

}
