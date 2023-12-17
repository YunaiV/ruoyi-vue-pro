package cn.iocoder.yudao.module.crm.service.business;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.*;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessLinkPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessLinkDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessMapper;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;

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
    private CrmPermissionService crmPermissionService;

    @Resource
    private CrmContactService crmContactService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBusiness(CrmBusinessCreateReqVO createReqVO, Long userId) {
        // 插入
        CrmBusinessDO business = CrmBusinessConvert.INSTANCE.convert(createReqVO);
        businessMapper.insert(business);

        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_BUSINESS.getType())
                .setBizId(business.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人

        // 返回
        return business.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#updateReqVO.id",
            level = CrmPermissionLevelEnum.WRITE)
    public void updateBusiness(CrmBusinessUpdateReqVO updateReqVO) {
        // 校验存在
        validateBusinessExists(updateReqVO.getId());
        // 更新
        CrmBusinessDO updateObj = CrmBusinessConvert.INSTANCE.convert(updateReqVO);
        businessMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, level = CrmPermissionLevelEnum.WRITE)
    public void deleteBusiness(Long id) {
        // 校验存在
        validateBusinessExists(id);
        // 删除
        businessMapper.deleteById(id);
    }

    private CrmBusinessDO validateBusinessExists(Long id) {
        CrmBusinessDO crmBusiness = businessMapper.selectById(id);
        if (crmBusiness == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        return crmBusiness;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS,bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmBusinessDO getBusiness(Long id) {
        return businessMapper.selectById(id);
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
        // 1. 获取当前用户能看的分页数据
        // TODO @puhui999：如果业务的数据量比较大，in 太多可能有性能问题噢；看看是不是搞成 join 连表了；可以微信讨论下；
        List<CrmPermissionDO> permissions = crmPermissionService.getPermissionListByBizTypeAndUserId(
                CrmBizTypeEnum.CRM_BUSINESS.getType(), userId);
        Set<Long> ids = convertSet(permissions, CrmPermissionDO::getBizId);
        if (CollUtil.isEmpty(ids)) { // 没得说明没有什么给他看的
            return PageResult.empty();
        }

        // 2. 获取商机分页数据
        return businessMapper.selectPage(pageReqVO, ids);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmBusinessDO> getBusinessPageByCustomer(CrmContractPageReqVO pageReqVO) {
        return businessMapper.selectPageByCustomer(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferBusiness(CrmBusinessTransferReqVO reqVO, Long userId) {
        // 1 校验商机是否存在
        validateBusinessExists(reqVO.getId());

        // 2. 数据权限转移
        crmPermissionService.transferPermission(
                CrmBusinessConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_BUSINESS.getType()));

        // 3. TODO 记录转移日志
    }
    @Override
    public PageResult<CrmBusinessRespVO> getBusinessPageByContact(CrmContactBusinessLinkPageReqVO pageReqVO) {
        CrmContactBusinessLinkPageReqVO crmContactBusinessLinkPageReqVO = new CrmContactBusinessLinkPageReqVO();
        crmContactBusinessLinkPageReqVO.setContactId(pageReqVO.getContactId());
        PageResult<CrmContactBusinessLinkDO> businessLinkDOS = crmContactService.selectBusinessPageByContact(crmContactBusinessLinkPageReqVO);
        if (CollUtil.isEmpty(businessLinkDOS.getList())){
            return PageResult.empty();
        }
        List<CrmBusinessDO> businessList = this.getBusinessList(CollectionUtils.convertList(businessLinkDOS.getList(),
                CrmContactBusinessLinkDO::getBusinessId));
        if (CollUtil.isEmpty(businessList)){
            return PageResult.empty();
        }
        PageResult<CrmBusinessRespVO> pageResult = new PageResult<CrmBusinessRespVO>();
        List<CrmBusinessRespVO> respVOList = BeanUtils.toBean(businessList,CrmBusinessRespVO.class);
        Map<Long,Long> businessContactMap = CollectionUtils.convertMap(businessLinkDOS.getList(),
                CrmContactBusinessLinkDO::getBusinessId,CrmContactBusinessLinkDO::getId);
        respVOList.forEach(item -> {
            item.setBusinessContactId(businessContactMap.get(item.getId()));
        });
        pageResult.setList(respVOList);
        pageResult.setTotal(businessLinkDOS.getTotal());
        return pageResult;

    }
}
