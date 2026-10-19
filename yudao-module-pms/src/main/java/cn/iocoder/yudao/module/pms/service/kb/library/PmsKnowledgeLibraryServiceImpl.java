package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibrarySaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * PMS 知识库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeLibraryServiceImpl implements PmsKnowledgeLibraryService {

    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeLibraryMemberService memberService;
    @Resource
    private PmsKnowledgeGroupService knowledgeGroupService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeRecycleService recycleService;
    @Resource
    private PmsKnowledgeLibraryTemplateService libraryTemplateService;

    @Resource
    private PermissionApi permissionApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLibrary(PmsKnowledgeLibrarySaveReqVO saveReqVO, Long userId) {
        // 1. 创建知识库
        PmsKnowledgeLibraryDO library = BeanUtils.toBean(saveReqVO, PmsKnowledgeLibraryDO.class)
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
        libraryMapper.insert(library);

        // 2. 创建人和初始成员加入知识库
        memberService.createLibraryMemberList(library.getId(), userId,
                saveReqVO.getAdminUserIds(), saveReqVO.getMemberUserIds());

        // 3. 使用所选模板创建知识库文档
        if (saveReqVO.getTemplateId() != null) {
            libraryTemplateService.createTemplateDocumentList(saveReqVO.getTemplateId(), library.getId(), userId);
        }
        return library.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLibrary(PmsKnowledgeLibrarySaveReqVO saveReqVO, Long userId) {
        // 1.1 普通库设置由管理员维护
        PmsKnowledgeLibraryDO library = memberService.validateLibraryAdmin(saveReqVO.getId(), userId);
        // 1.2 公开库转私有仅限创建人
        if (Boolean.TRUE.equals(library.getOpenStatus())
                && Boolean.FALSE.equals(saveReqVO.getOpenStatus())) {
            memberService.validateLibraryCreator(library.getId(), userId);
        }

        // 2. 更新知识库基本信息
        libraryMapper.updateForEdit(BeanUtils.toBean(saveReqVO, PmsKnowledgeLibraryDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLibrary(Long id, Long userId) {
        // 1. 校验当前用户是知识库创建人
        PmsKnowledgeLibraryDO library = memberService.validateLibraryCreator(id, userId);

        // 2. 将知识库及其内容移入回收站
        recycleService.recycleLibrary(library, userId);
    }

    @Override
    public void updateLibraryToRecycled(Long id, Long userId, LocalDateTime deleteTime) {
        libraryMapper.updateById(new PmsKnowledgeLibraryDO().setId(id)
                .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                .setDeleteUserId(userId).setDeleteTime(deleteTime));
    }

    @Override
    public void restoreLibrary(Long id) {
        libraryMapper.updateToRestoreById(id, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

    @Override
    public void deleteLibraryPermanently(Long id) {
        libraryMapper.deleteById(id);
    }

    @Override
    public PmsKnowledgeLibraryDO getLibrary(Long id) {
        return libraryMapper.selectById(id);
    }

    @Override
    public PageResult<PmsKnowledgeLibraryDO> getLibraryPage(PmsKnowledgeLibraryPageReqVO pageReqVO, Long userId) {
        // 1. 获得当前用户参与及可读的知识库，并按个人分组进一步过滤
        List<Long> memberLibraryIds = memberService.getJoinedLibraryIdList(userId);
        List<Long> filterLibraryIds = null;
        if (pageReqVO.getGroupId() != null) {
            List<Long> readableLibraryIds = memberService.getReadableLibraryIdList(userId);
            filterLibraryIds = knowledgeGroupService.filterLibraryIdListByGroup(
                    pageReqVO.getGroupId(), userId, readableLibraryIds);
            if (CollUtil.isEmpty(filterLibraryIds)) {
                return PageResult.empty();
            }
        }
        // 2. 超级管理员查询全部知识库，普通用户查询公开或已加入知识库
        boolean includeAll = permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());

        // 3. 执行分页查询
        return libraryMapper.selectPage(pageReqVO, memberLibraryIds, includeAll, filterLibraryIds,
                PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

    @Override
    public List<PmsKnowledgeLibraryDO> getLibraryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return libraryMapper.selectByIds(ids);
    }

    @Override
    public List<Long> getLibraryIdList(Boolean openStatus) {
        return libraryMapper.selectIdList(openStatus, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

}
