package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessLinkPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessLinkSaveReqVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessLinkDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contactbusinesslink.CrmContactBusinessLinkMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTACT_BUSINESS_LINK_NOT_EXISTS;

// TODO @puhui999：数据权限的校验；每个操作；
/**
 * 联系人商机关联 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmContactBusinessLinkServiceImpl implements CrmContactBusinessLinkService {

    @Resource
    private CrmContactBusinessLinkMapper contactBusinessLinkMapper;
    @Resource
    private CrmBusinessService crmBusinessService;

    @Resource
    private CrmContactService crmContactService;

    @Override
    public Long createContactBusinessLink(CrmContactBusinessLinkSaveReqVO createReqVO) {
        CrmContactBusinessLinkDO contactBusinessLink = BeanUtils.toBean(createReqVO, CrmContactBusinessLinkDO.class);
        contactBusinessLinkMapper.insert(contactBusinessLink);
        return contactBusinessLink.getId();
    }

    @Override
    public void createContactBusinessLinkBatch(List<CrmContactBusinessLinkSaveReqVO> createReqVOList) {
        // 插入
        CrmContactDO contactDO = crmContactService.getContact(createReqVOList.stream().findFirst().get().getContactId());
        Assert.notNull(contactDO,ErrorCodeConstants.CONTACT_NOT_EXISTS.getMsg());
        List<CrmContactBusinessLinkDO> saveDoList = new ArrayList<CrmContactBusinessLinkDO>();
        createReqVOList.forEach(item -> {
            CrmBusinessDO crmBusinessDO = crmBusinessService.getBusiness(item.getBusinessId());
            if(crmBusinessDO == null){
                throw exception(BUSINESS_NOT_EXISTS);
            }
            // 判重
            CrmContactBusinessLinkDO crmContactBusinessLinkDO = contactBusinessLinkMapper.selectOne(new LambdaQueryWrapper<CrmContactBusinessLinkDO>()
                    .eq(CrmContactBusinessLinkDO::getBusinessId,item.getBusinessId())
                    .eq(CrmContactBusinessLinkDO::getContactId,item.getContactId()));
            if(crmContactBusinessLinkDO == null){
                saveDoList.add(BeanUtils.toBean(item,CrmContactBusinessLinkDO.class));
            }
        });
        contactBusinessLinkMapper.insertBatch(saveDoList);
    }

    @Override
    public void updateContactBusinessLink(CrmContactBusinessLinkSaveReqVO updateReqVO) {
        // 校验存在
        validateContactBusinessLinkExists(updateReqVO.getId());
        // 更新
        CrmContactBusinessLinkDO updateObj = BeanUtils.toBean(updateReqVO, CrmContactBusinessLinkDO.class);
        contactBusinessLinkMapper.updateById(updateObj);
    }

    @Override
    public void deleteContactBusinessLink(List<Long> businessContactIds) {
        // 删除
        contactBusinessLinkMapper.deleteBatchIds(businessContactIds);
    }

    private void validateContactBusinessLinkExists(Long id) {
        if (contactBusinessLinkMapper.selectById(id) == null) {
            throw exception(CONTACT_BUSINESS_LINK_NOT_EXISTS);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContactBusinessLinkDO getContactBusinessLink(Long id) {
        return contactBusinessLinkMapper.selectById(id);
    }

    @Override
    public PageResult<CrmBusinessRespVO> getContactBusinessLinkPageByContact(CrmContactBusinessLinkPageReqVO pageReqVO) {
        CrmContactBusinessLinkPageReqVO crmContactBusinessLinkPageReqVO = new CrmContactBusinessLinkPageReqVO();
        crmContactBusinessLinkPageReqVO.setContactId(pageReqVO.getContactId());
        PageResult<CrmContactBusinessLinkDO> businessLinkDOS = contactBusinessLinkMapper.selectPageByContact(crmContactBusinessLinkPageReqVO);
        List<CrmBusinessDO> businessDOS = crmBusinessService.getBusinessList(CollectionUtils.convertList(businessLinkDOS.getList(),
                CrmContactBusinessLinkDO::getBusinessId), getLoginUserId());
        PageResult<CrmBusinessRespVO> pageResult = new PageResult<CrmBusinessRespVO>();
        pageResult.setList(CrmBusinessConvert.INSTANCE.convert(businessDOS));
        pageResult.setTotal(businessLinkDOS.getTotal());
        return pageResult;

    }

    @Override
    public PageResult<CrmContactBusinessLinkDO> getContactBusinessLinkPage(CrmContactBusinessLinkPageReqVO pageReqVO) {
        return contactBusinessLinkMapper.selectPage(pageReqVO);
    }

}