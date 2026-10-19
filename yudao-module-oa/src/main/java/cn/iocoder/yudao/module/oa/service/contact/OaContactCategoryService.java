package cn.iocoder.yudao.module.oa.service.contact;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category.OaContactCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactCategoryDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OA 联系人分类 Service 接口
 *
 * @author 芋道源码
 */
public interface OaContactCategoryService {

    /**
     * 创建联系人分类
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 分类编号
     */
    Long createContactCategory(OaContactCategorySaveReqVO createReqVO, Long userId);

    /**
     * 更新联系人分类
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateContactCategory(OaContactCategorySaveReqVO updateReqVO, Long userId);

    /**
     * 删除联系人分类
     *
     * @param id 分类编号
     * @param userId 用户编号
     */
    void deleteContactCategory(Long id, Long userId);

    /**
     * 获得用户的联系人分类列表
     *
     * @param userId 用户编号
     * @return 联系人分类列表
     */
    List<OaContactCategoryDO> getContactCategoryList(Long userId);

    /**
     * 获得分类详情
     *
     * @param id 分类编号
     * @param userId 用户编号
     * @return 分类
     */
    OaContactCategoryDO getContactCategory(Long id, Long userId);

    /**
     * 校验分类存在且属于当前用户
     *
     * @param id 分类编号
     * @param userId 用户编号
     * @return 分类
     */
    OaContactCategoryDO validateContactCategory(Long id, Long userId);

    /**
     * 获得指定编号的分类列表
     *
     * @param ids 分类编号集合
     * @return 分类列表
     */
    List<OaContactCategoryDO> getContactCategoryList(Collection<Long> ids);

    /**
     * 获得分类编号与分类的映射
     *
     * @param ids 分类编号集合
     * @return 分类 Map
     */
    default Map<Long, OaContactCategoryDO> getContactCategoryMap(Collection<Long> ids) {
        List<OaContactCategoryDO> categories = getContactCategoryList(ids);
        return CollectionUtils.convertMap(categories, OaContactCategoryDO::getId);
    }

}
