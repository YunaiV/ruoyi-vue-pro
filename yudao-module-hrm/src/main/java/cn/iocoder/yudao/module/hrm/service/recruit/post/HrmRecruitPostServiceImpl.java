package cn.iocoder.yudao.module.hrm.service.recruit.post;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.post.HrmRecruitPostMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_POST_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * 招聘职位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmRecruitPostServiceImpl implements HrmRecruitPostService {

    @Resource
    private HrmRecruitPostMapper recruitPostMapper;

    @Resource
    private HrmRecruitPostTypeService recruitPostTypeService;

    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;

    @Override
    @LogRecord(type = HRM_RECRUIT_POST_TYPE, subType = HRM_RECRUIT_POST_CREATE_SUB_TYPE,
            bizNo = "{{#recruitPostId}}", success = HRM_RECRUIT_POST_CREATE_SUCCESS)
    public Long createRecruitPost(HrmRecruitPostSaveReqVO createReqVO) {
        // 1. 校验招聘职位关联
        validateRecruitPost(createReqVO);

        // 2. 插入招聘职位。新职位统一为招聘中状态，后续通过状态接口停止招聘
        HrmRecruitPostDO recruitPost = BeanUtils.toBean(createReqVO, HrmRecruitPostDO.class)
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus());
        recruitPostMapper.insert(recruitPost);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitPostId", recruitPost.getId());
        return recruitPost.getId();
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_POST_TYPE, subType = HRM_RECRUIT_POST_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_RECRUIT_POST_UPDATE_SUCCESS)
    public void updateRecruitPost(HrmRecruitPostSaveReqVO updateReqVO) {
        // 1. 校验招聘职位及其关联
        HrmRecruitPostDO recruitPost = validateRecruitPostExists(updateReqVO.getId());
        validateRecruitPost(updateReqVO);

        // 2. 更新招聘职位
        recruitPostMapper.updateForEdit(BeanUtils.toBean(updateReqVO, HrmRecruitPostDO.class));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(recruitPost, HrmRecruitPostSaveReqVO.class));
    }

    @Override
    public HrmRecruitPostDO getRecruitPost(Long id) {
        return recruitPostMapper.selectById(id);
    }

    @Override
    public HrmRecruitPostDO validateRecruitPostExists(Long id) {
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(id);
        if (recruitPost == null) {
            throw exception(RECRUIT_POST_NOT_EXISTS);
        }
        return recruitPost;
    }

    @Override
    public List<HrmRecruitPostDO> getRecruitPostList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return recruitPostMapper.selectByIds(ids);
    }

    @Override
    public PageResult<HrmRecruitPostDO> getRecruitPostPage(HrmRecruitPostPageReqVO pageReqVO) {
        return recruitPostMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmRecruitPostDO> getRecruitPostSimpleList() {
        return recruitPostMapper.selectListByStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus());
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_POST_TYPE, subType = HRM_RECRUIT_POST_UPDATE_STATUS_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_RECRUIT_POST_UPDATE_STATUS_SUCCESS)
    public void updateRecruitPostStatus(HrmRecruitPostStatusReqVO reqVO) {
        // 1. 校验招聘职位是否存在
        HrmRecruitPostDO recruitPost = validateRecruitPostExists(reqVO.getId());

        // 2. 更新招聘职位状态
        HrmRecruitPostDO updateObj = BeanUtils.toBean(reqVO, HrmRecruitPostDO.class);
        if (HrmRecruitPostStatusEnum.STOPPED.getStatus().equals(reqVO.getStatus())) {
            updateObj.setStopReason(reqVO.getStopReason());
        } else {
            updateObj.setStopReason("");
        }
        recruitPostMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitPost", recruitPost);
    }

    @Override
    public Map<Integer, Long> getRecruitPostStatusCount(HrmRecruitPostPageReqVO pageReqVO) {
        return recruitPostMapper.selectCountMapByStatus(pageReqVO);
    }

    private void validateRecruitPost(HrmRecruitPostSaveReqVO reqVO) {
        // 1. 校验职位类型和用人部门存在
        recruitPostTypeService.validateRecruitPostTypeExists(reqVO.getPostTypeId());
        if (reqVO.getDeptId() != null) {
            deptApi.validateDeptList(Collections.singleton(reqVO.getDeptId()));
        }

        // 2. 校验招聘负责人和面试官存在
        Set<Long> employeeIds = new HashSet<>(CollUtil.emptyIfNull(reqVO.getInterviewEmployeeIds()));
        CollectionUtils.addIfNotNull(employeeIds, reqVO.getOwnerEmployeeId());
        employeeService.validateEmployeeListByEntryStatus(
                employeeIds, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

}
