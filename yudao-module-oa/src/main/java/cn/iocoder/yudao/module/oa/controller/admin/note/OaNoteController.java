package cn.iocoder.yudao.module.oa.controller.admin.note;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteFavoriteUpdateReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNotePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteShareUpdateReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteCategoryDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteDO;
import cn.iocoder.yudao.module.oa.service.note.OaNoteCategoryService;
import cn.iocoder.yudao.module.oa.service.note.OaNoteService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 笔记")
@RestController
@RequestMapping("/oa/note")
@Validated
public class OaNoteController {

    @Resource
    private OaNoteService noteService;
    @Resource
    private OaNoteCategoryService noteCategoryService;
    @Resource
    private AdminUserApi adminUserApi;

    // ==================== 我的笔记 ====================

    @PostMapping("/create")
    @Operation(summary = "创建笔记")
    @PreAuthorize("@ss.hasPermission('oa:note:create')")
    public CommonResult<Long> createNote(@Valid @RequestBody OaNoteSaveReqVO createReqVO) {
        return success(noteService.createNote(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新笔记")
    @PreAuthorize("@ss.hasPermission('oa:note:update')")
    public CommonResult<Boolean> updateNote(@Valid @RequestBody OaNoteSaveReqVO updateReqVO) {
        noteService.updateNote(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除笔记")
    @Parameter(name = "id", description = "笔记编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:note:delete')")
    public CommonResult<Boolean> deleteNote(@RequestParam("id") Long id) {
        noteService.deleteNote(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete-received")
    @Operation(summary = "移除收到的共享笔记")
    @Parameter(name = "id", description = "笔记编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteReceivedNote(@RequestParam("id") Long id) {
        noteService.deleteReceivedNote(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-favorite")
    @Operation(summary = "更新笔记收藏状态")
    public CommonResult<Boolean> updateNoteFavorite(@Valid @RequestBody OaNoteFavoriteUpdateReqVO updateReqVO) {
        noteService.updateNoteFavorite(updateReqVO.getId(), updateReqVO.getFavorite(), getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-share")
    @Operation(summary = "更新笔记共享接收人")
    @PreAuthorize("@ss.hasPermission('oa:note:update')")
    public CommonResult<Boolean> updateNoteShare(@Valid @RequestBody OaNoteShareUpdateReqVO updateReqVO) {
        noteService.updateNoteShare(updateReqVO.getId(), updateReqVO.getReceiverUserIds(), getLoginUserId());
        return success(true);
    }

    // ==================== 公共查询（我的笔记、共享给我） ====================

    @GetMapping("/get")
    @Operation(summary = "获得笔记详情")
    @Parameter(name = "id", description = "笔记编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:note:query')")
    public CommonResult<OaNoteRespVO> getNote(@RequestParam("id") Long id) {
        OaNoteDO note = noteService.getNote(id, getLoginUserId());
        return success(buildNoteRespVO(note));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得我的笔记分页")
    @PreAuthorize("@ss.hasPermission('oa:note:query')")
    public CommonResult<PageResult<OaNoteRespVO>> getMyNotePage(@Valid OaNotePageReqVO pageReqVO) {
        PageResult<OaNoteDO> pageResult = noteService.getMyNotePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildNoteRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/received-page")
    @Operation(summary = "获得共享给我的笔记分页")
    @PreAuthorize("@ss.hasPermission('oa:note:query')")
    public CommonResult<PageResult<OaNoteRespVO>> getReceivedNotePage(@Valid OaNotePageReqVO pageReqVO) {
        PageResult<OaNoteDO> pageResult = noteService.getReceivedNotePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildNoteRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接笔记详情
     *
     * @param note 笔记
     * @return 笔记响应
     */
    private OaNoteRespVO buildNoteRespVO(OaNoteDO note) {
        if (note == null) {
            return null;
        }
        return CollUtil.getFirst(buildNoteRespVOList(Collections.singletonList(note)));
    }

    /**
     * 拼接笔记响应列表
     *
     * @param notes 笔记列表
     * @return 笔记响应列表
     */
    private List<OaNoteRespVO> buildNoteRespVOList(List<OaNoteDO> notes) {
        if (CollUtil.isEmpty(notes)) {
            return Collections.emptyList();
        }
        // 1. 查询接收关系、用户及目录信息
        Map<Long, List<Long>> receiverUserIdListMap = noteService.getNoteReceiverUserIdListMap(
                convertSet(notes, OaNoteDO::getId));
        Set<Long> userIds = convertSet(notes, note -> NumberUtils.parseLong(note.getCreator()));
        receiverUserIdListMap.values().forEach(userIds::addAll);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, OaNoteCategoryDO> categoryMap = noteCategoryService.getNoteCategoryMap(convertSet(notes, OaNoteDO::getCategoryId));
        // 2. 转换笔记并拼接展示字段
        return convertList(notes, noteDO -> {
            OaNoteRespVO note = BeanUtils.toBean(noteDO, OaNoteRespVO.class)
                    .setCreatorUserId(NumberUtils.parseLong(noteDO.getCreator()));
            MapUtils.findAndThen(userMap, note.getCreatorUserId(), user -> note.setCreatorUserName(user.getNickname()));
            MapUtils.findAndThen(categoryMap, note.getCategoryId(), category -> note.setCategoryName(category.getName()));
            // 持有关系包含创建人，共享名单仅展示其他接收人
            List<Long> receiverUserIds = filterList(receiverUserIdListMap.getOrDefault(note.getId(), Collections.emptyList()),
                    userId -> ObjectUtil.notEqual(userId, note.getCreatorUserId()));
            note.setReceiverUserIds(receiverUserIds);
            note.setReceiverUserNames(convertList(receiverUserIds, receiverUserId -> {
                AdminUserRespDTO receiverUser = userMap.get(receiverUserId);
                return receiverUser != null ? receiverUser.getNickname() : null;
            }));
            return note;
        });
    }

}
