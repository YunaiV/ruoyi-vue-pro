package cn.iocoder.yudao.module.oa.service.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactShareDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OA 外部联系人 Service 接口
 *
 * @author 芋道源码
 */
public interface OaContactService {

    /**
     * 创建外部联系人
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 联系人编号
     */
    Long createContact(OaContactSaveReqVO createReqVO, Long userId);

    /**
     * 更新外部联系人
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateContact(OaContactSaveReqVO updateReqVO, Long userId);

    /**
     * 删除外部联系人
     *
     * @param id 联系人编号
     * @param userId 用户编号
     */
    void deleteContact(Long id, Long userId);

    /**
     * 删除接收到的共享联系人
     *
     * @param contactId 联系人编号
     * @param userId 用户编号
     */
    void deleteReceivedContact(Long contactId, Long userId);

    /**
     * 获得外部联系人
     *
     * @param id 联系人编号
     * @param userId 用户编号
     * @return 外部联系人
     */
    OaContactDO getContact(Long id, Long userId);

    /**
     * 获得我的联系人分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 联系人分页
     */
    PageResult<OaContactDO> getMyContactPage(OaContactPageReqVO pageReqVO, Long userId);

    /**
     * 获得共享给我的联系人分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 联系人分页
     */
    PageResult<OaContactDO> getReceivedContactPage(OaContactPageReqVO pageReqVO, Long userId);

    /**
     * 获得我共享的联系人分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 联系人分页
     */
    PageResult<OaContactShareDO> getSharedContactPage(OaContactPageReqVO pageReqVO, Long userId);

    // TODO DONE @AI：方法注释只描述查询职责，与同类 Service 保持一致。
    /**
     * 获得联系人列表
     *
     * @param ids 联系人编号集合
     * @return 联系人列表
     */
    List<OaContactDO> getContactList(Collection<Long> ids);

    /**
     * 共享外部联系人
     *
     * @param contactId 联系人编号
     * @param userIds 共享接收人用户编号集合
     * @param currentUserId 当前用户编号
     */
    void shareContact(Long contactId, Collection<Long> userIds, Long currentUserId);

    /**
     * 处理外部联系人共享
     *
     * @param contactId 联系人编号
     * @param categoryId 接收人分类编号
     * @param userId 用户编号
     */
    void handleContactShare(Long contactId, Long categoryId, Long userId);

    /**
     * 获得联系人共享记录 Map
     *
     * @param contactIds 联系人编号集合
     * @return 联系人编号与共享记录列表的 Map
     */
    default Map<Long, List<OaContactShareDO>> getContactShareListMap(Collection<Long> contactIds) {
        List<OaContactShareDO> list = getContactShareList(contactIds);
        return CollectionUtils.convertMultiMap(list, OaContactShareDO::getContactId);
    }

    /**
     * 获得联系人共享记录列表
     *
     * @param contactIds 联系人编号集合
     * @return 共享记录列表
     */
    List<OaContactShareDO> getContactShareList(Collection<Long> contactIds);

    /**
     * 清空分类下的联系人及接收关系分类编号
     *
     * @param categoryId 分类编号
     */
    void clearContactCategoryId(Long categoryId);

}
