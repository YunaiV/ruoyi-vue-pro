package cn.iocoder.yudao.module.oa.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactHandleReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactShareReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactCategoryDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactShareDO;
import cn.iocoder.yudao.module.oa.service.contact.OaContactCategoryService;
import cn.iocoder.yudao.module.oa.service.contact.OaContactService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 通讯录")
@RestController
@RequestMapping("/oa/contact")
@Validated
public class OaContactController {

    @Resource
    private OaContactService contactService;
    @Resource
    private OaContactCategoryService contactCategoryService;

    @Resource
    private AdminUserApi adminUserApi;

    // ==================== 我的联系人 ====================

    @PostMapping("/create")
    @Operation(summary = "创建外部联系人")
    @PreAuthorize("@ss.hasPermission('oa:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody OaContactSaveReqVO createReqVO) {
        return success(contactService.createContact(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新外部联系人")
    @PreAuthorize("@ss.hasPermission('oa:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody OaContactSaveReqVO updateReqVO) {
        contactService.updateContact(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外部联系人")
    @Parameter(name = "id", description = "联系人编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:contact:delete')")
    public CommonResult<Boolean> deleteContact(@RequestParam("id") Long id) {
        contactService.deleteContact(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/share")
    @Operation(summary = "共享外部联系人")
    public CommonResult<Boolean> shareContact(@Valid @RequestBody OaContactShareReqVO shareReqVO) {
        contactService.shareContact(shareReqVO.getContactId(), shareReqVO.getUserIds(), getLoginUserId());
        return success(true);
    }

    // ==================== 共享与我 ====================

    @DeleteMapping("/delete-received")
    @Operation(summary = "删除接收到的共享联系人")
    @Parameter(name = "contactId", description = "联系人编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteReceivedContact(@RequestParam("contactId") Long contactId) {
        contactService.deleteReceivedContact(contactId, getLoginUserId());
        return success(true);
    }

    @PutMapping("/handle-share")
    @Operation(summary = "处理外部联系人共享")
    public CommonResult<Boolean> handleContactShare(@Valid @RequestBody OaContactHandleReqVO handleReqVO) {
        contactService.handleContactShare(handleReqVO.getContactId(), handleReqVO.getCategoryId(), getLoginUserId());
        return success(true);
    }

    // ==================== 公共查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得外部联系人详情")
    @Parameter(name = "id", description = "联系人编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<OaContactRespVO> getContact(@RequestParam("id") Long id) {
        OaContactDO contact = contactService.getContact(id, getLoginUserId());
        return success(buildContactRespVO(contact));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得我的联系人分页")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<PageResult<OaContactRespVO>> getMyContactPage(@Valid OaContactPageReqVO pageReqVO) {
        PageResult<OaContactDO> pageResult = contactService.getMyContactPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildContactRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/received-page")
    @Operation(summary = "获得共享给我的联系人分页")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<PageResult<OaContactRespVO>> getReceivedContactPage(@Valid OaContactPageReqVO pageReqVO) {
        PageResult<OaContactDO> pageResult = contactService.getReceivedContactPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildContactRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/shared-page")
    @Operation(summary = "获得我共享的联系人分页")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<PageResult<OaContactRespVO>> getSharedContactPage(@Valid OaContactPageReqVO pageReqVO) {
        PageResult<OaContactShareDO> pageResult = contactService.getSharedContactPage(pageReqVO, getLoginUserId());
        List<OaContactDO> contacts = contactService.getContactList(convertSet(pageResult.getList(), OaContactShareDO::getContactId));
        // 拼接 VO
        Map<Long, OaContactRespVO> contactMap = convertMap(buildContactRespVOList(contacts), OaContactRespVO::getId);
        return success(new PageResult<>(convertList(pageResult.getList(), share -> {
            OaContactRespVO contact = BeanUtils.toBean(contactMap.get(share.getContactId()), OaContactRespVO.class);
            contact.setShare(CollUtil.findOne(contact.getShares(), item -> ObjectUtil.equal(item.getId(), share.getId())));
            return contact;
        }), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接联系人详情
     *
     * @param contact 联系人
     * @return 联系人响应
     */
    private OaContactRespVO buildContactRespVO(OaContactDO contact) {
        if (contact == null) {
            return null;
        }
        return CollUtil.getFirst(buildContactRespVOList(Collections.singletonList(contact)));
    }

    /**
     * 拼接外部联系人响应列表
     *
     * @param contacts 联系人列表
     * @return 联系人响应列表
     */
    private List<OaContactRespVO> buildContactRespVOList(List<OaContactDO> contacts) {
        if (CollUtil.isEmpty(contacts)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询共享关系
        Map<Long, List<OaContactShareDO>> contactShareListMap = contactService.getContactShareListMap(
                convertSet(contacts, OaContactDO::getId));
        List<OaContactShareDO> shares = convertListByFlatMap(contactShareListMap.values(), List::stream);
        // 1.2 批量查询创建人和接收人信息
        Set<Long> userIds = convertSet(contacts, contact -> NumberUtils.parseLong(contact.getCreator()));
        userIds.addAll(convertSet(shares, OaContactShareDO::getUserId));
        userIds.addAll(convertSet(shares, share -> NumberUtils.parseLong(share.getCreator())));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.3 批量查询创建人和接收人的分类
        Set<Long> categoryIds = convertSet(contacts, OaContactDO::getCategoryId);
        categoryIds.addAll(convertSet(shares, OaContactShareDO::getCategoryId));
        Map<Long, OaContactCategoryDO> categoryMap = contactCategoryService.getContactCategoryMap(categoryIds);
        Long loginUserId = getLoginUserId();
        // 2. 拼接展示字段，接收人仅能查看自己的共享关系
        return convertList(contacts, contactDO -> {
            OaContactRespVO contact = BeanUtils.toBean(contactDO, OaContactRespVO.class)
                    .setOwnerUserId(NumberUtils.parseLong(contactDO.getCreator()));
            MapUtils.findAndThen(categoryMap, contact.getCategoryId(),
                    category -> contact.setCategoryName(category.getName()));
            MapUtils.findAndThen(userMap, contact.getOwnerUserId(),
                    user -> contact.setOwnerUserName(user.getNickname()));
            List<OaContactShareDO> contactShares = contactShareListMap.getOrDefault(contact.getId(),
                    Collections.emptyList());
            // 仅展示本人发出的共享，不将初始化的本人关联计入共享人数
            List<OaContactShareDO> visibleContactShares = filterList(contactShares,
                    share -> ObjectUtil.equal(NumberUtils.parseLong(share.getCreator()), loginUserId)
                            && ObjectUtil.notEqual(share.getUserId(), loginUserId));
            contact.setShares(convertList(visibleContactShares, share -> {
                OaContactRespVO.Share shareVO = BeanUtils.toBean(share, OaContactRespVO.Share.class);
                MapUtils.findAndThen(categoryMap, share.getCategoryId(),
                        category -> shareVO.setCategoryName(category.getName()));
                MapUtils.findAndThen(userMap, NumberUtils.parseLong(share.getCreator()),
                        user -> shareVO.setCreatorName(user.getNickname()));
                MapUtils.findAndThen(userMap, share.getUserId(),
                        user -> shareVO.setUserName(user.getNickname()).setUserAvatar(user.getAvatar()));
                return shareVO;
            }));
            OaContactShareDO currentUserShare = CollUtil.findOne(contactShares,
                    share -> share.getUserId().equals(loginUserId));
            if (currentUserShare != null) {
                MapUtils.findAndThen(userMap, NumberUtils.parseLong(currentUserShare.getCreator()),
                        user -> contact.setSharerName(user.getNickname()));
                contact.setHandleStatus(currentUserShare.getHandleStatus())
                        .setSharedCategoryId(currentUserShare.getCategoryId());
                MapUtils.findAndThen(categoryMap, currentUserShare.getCategoryId(),
                        category -> contact.setSharedCategoryName(category.getName()));
            }
            return contact;
        });
    }

}
