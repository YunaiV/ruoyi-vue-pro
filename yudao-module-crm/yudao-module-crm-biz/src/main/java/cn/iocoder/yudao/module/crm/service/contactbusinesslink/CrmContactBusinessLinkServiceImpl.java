package cn.iocoder.yudao.module.crm.service.contactbusinesslink;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.convert.contactbusinessslink.CrmContactBusinessLinkConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink.CrmContactBusinessLinkDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.crm.dal.mysql.contactbusinesslink.CrmContactBusinessLinkMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

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

    @Override
    public Long createContactBusinessLink(CrmContactBusinessLinkSaveReqVO createReqVO) {
        // 插入
        CrmContactBusinessLinkDO contactBusinessLink = BeanUtils.toBean(createReqVO, CrmContactBusinessLinkDO.class);
        contactBusinessLinkMapper.insert(contactBusinessLink);
        // 返回
        return contactBusinessLink.getId();
    }

    @Override
    public void createContactBusinessLinkBatch(List<CrmContactBusinessLinkSaveReqVO> createReqVOList) {
        // 插入
        List<CrmContactBusinessLinkDO> saveDoList = CrmContactBusinessLinkConvert.INSTANCE.convert(createReqVOList);
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
    public void deleteContactBusinessLink(List<CrmContactBusinessLinkSaveReqVO> createReqVO) {
        // 删除
        createReqVO.forEach(item -> {
            contactBusinessLinkMapper.delete(new LambdaQueryWrapperX<CrmContactBusinessLinkDO>()
                    .eq(CrmContactBusinessLinkDO::getBusinessId,item.getBusinessId())
                    .eq(CrmContactBusinessLinkDO::getContactId,item.getContactId())
                    .eq(CrmContactBusinessLinkDO::getDeleted,0));
        });
    }

    private void validateContactBusinessLinkExists(Long id) {
        if (contactBusinessLinkMapper.selectById(id) == null) {
            throw exception(CONTACT_BUSINESS_LINK_NOT_EXISTS);
        }
    }

    @Override
    public CrmContactBusinessLinkDO getContactBusinessLink(Long id) {
        return contactBusinessLinkMapper.selectById(id);
    }

    @Override
    public PageResult<CrmBusinessRespVO> getContactBusinessLinkPageByContact(CrmContactBusinessLinkPageReqVO pageReqVO) {
        CrmContactBusinessLinkPageReqVO crmContactBusinessLinkPageReqVO = new CrmContactBusinessLinkPageReqVO();
        crmContactBusinessLinkPageReqVO.setContactId(pageReqVO.getContactId());
        PageResult<CrmContactBusinessLinkDO> businessLinkDOS = contactBusinessLinkMapper.selectPageByContact(crmContactBusinessLinkPageReqVO);
        List<CrmBusinessDO> businessDOS = crmBusinessService.getBusinessList(CollectionUtils.convertList(businessLinkDOS.getList(),
                CrmContactBusinessLinkDO::getBusinessId));
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