package cn.iocoder.yudao.module.oa.service.contact;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category.OaContactCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactCategoryDO;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Collection;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_CATEGORY_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_CATEGORY_NOT_EXISTS;

/**
 * OA 联系人分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaContactCategoryServiceImpl implements OaContactCategoryService {

    @Resource
    private OaContactCategoryMapper contactCategoryMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaContactService contactService;

    @Override
    public Long createContactCategory(OaContactCategorySaveReqVO createReqVO, Long userId) {
        // 1. 校验分类名称唯一
        validateContactCategoryNameUnique(null, userId, createReqVO.getName());

        // 2. 新增联系人分类
        OaContactCategoryDO category = BeanUtils.toBean(createReqVO, OaContactCategoryDO.class)
                .setUserId(userId);
        contactCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContactCategory(OaContactCategorySaveReqVO updateReqVO, Long userId) {
        // 1.1 校验联系人分类属于当前用户
        validateContactCategory(updateReqVO.getId(), userId);
        // 1.2 校验分类名称唯一
        validateContactCategoryNameUnique(updateReqVO.getId(), userId, updateReqVO.getName());

        // 2. 更新联系人分类
        contactCategoryMapper.updateById(BeanUtils.toBean(updateReqVO, OaContactCategoryDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContactCategory(Long id, Long userId) {
        // 1. 校验联系人分类属于当前用户
        validateContactCategory(id, userId);

        // 2.1 清空本人联系人及接收关系中的分类
        contactService.clearContactCategoryId(id);
        // 2.2 删除联系人分类
        contactCategoryMapper.deleteById(id);
    }

    @Override
    public OaContactCategoryDO getContactCategory(Long id, Long userId) {
        return validateContactCategory(id, userId);
    }

    @Override
    public List<OaContactCategoryDO> getContactCategoryList(Long userId) {
        return contactCategoryMapper.selectListByUserId(userId);
    }


    @Override
    public List<OaContactCategoryDO> getContactCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return contactCategoryMapper.selectByIds(ids);
    }

    @Override
    public OaContactCategoryDO validateContactCategory(Long id, Long userId) {
        OaContactCategoryDO category = contactCategoryMapper.selectById(id);
        if (category == null || ObjectUtil.notEqual(category.getUserId(), userId)) {
            throw exception(CONTACT_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    /**
     * 校验同一用户的分类名称唯一
     *
     * @param id 当前分类编号，新增时为空
     * @param userId 用户编号
     * @param name 分类名称
     */
    private void validateContactCategoryNameUnique(Long id, Long userId, String name) {
        OaContactCategoryDO category = contactCategoryMapper.selectByUserIdAndName(userId, name);
        if (category != null && ObjectUtil.notEqual(category.getId(), id)) {
            throw exception(CONTACT_CATEGORY_NAME_DUPLICATE);
        }
    }

}
