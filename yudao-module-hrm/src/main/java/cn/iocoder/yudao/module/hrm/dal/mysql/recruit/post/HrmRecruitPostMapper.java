package cn.iocoder.yudao.module.hrm.dal.mysql.recruit.post;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HrmRecruitPostMapper extends BaseMapperX<HrmRecruitPostDO> {

    default PageResult<HrmRecruitPostDO> selectPage(HrmRecruitPostPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .eqIfPresent("status", reqVO.getStatus())
                .orderByDesc("id"));
    }

    default Map<Integer, Long> selectCountMapByStatus(HrmRecruitPostPageReqVO reqVO) {
        QueryWrapperX<HrmRecruitPostDO> query = buildQueryWrapper(reqVO);
        query.select("status", "COUNT(id) AS count").groupBy("status");
        return CollectionUtils.convertMap(selectMaps(query),
                record -> MapUtil.getInt(record, "status"),
                record -> MapUtil.getLong(record, "count"));
    }

    default List<HrmRecruitPostDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitPostDO>()
                .eq(HrmRecruitPostDO::getStatus, status)
                .orderByDesc(HrmRecruitPostDO::getId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateForEdit(HrmRecruitPostDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmRecruitPostDO>()
                .eq(HrmRecruitPostDO::getId, updateObj.getId())
                .set(updateObj.getDeptId() == null, HrmRecruitPostDO::getDeptId, null)
                .set(updateObj.getAreaId() == null, HrmRecruitPostDO::getAreaId, null)
                .set(updateObj.getRecruitNum() == null, HrmRecruitPostDO::getRecruitNum, null)
                .set(updateObj.getReason() == null, HrmRecruitPostDO::getReason, null)
                .set(updateObj.getWorkTime() == null, HrmRecruitPostDO::getWorkTime, null)
                .set(updateObj.getEducationRequire() == null, HrmRecruitPostDO::getEducationRequire, null)
                .set(updateObj.getMinSalary() == null, HrmRecruitPostDO::getMinSalary, null)
                .set(updateObj.getMaxSalary() == null, HrmRecruitPostDO::getMaxSalary, null)
                .set(updateObj.getSalaryUnit() == null, HrmRecruitPostDO::getSalaryUnit, null)
                .set(updateObj.getMinAge() == null, HrmRecruitPostDO::getMinAge, null)
                .set(updateObj.getMaxAge() == null, HrmRecruitPostDO::getMaxAge, null)
                .set(updateObj.getLatestEntryTime() == null, HrmRecruitPostDO::getLatestEntryTime, null)
                .set(updateObj.getOwnerEmployeeId() == null, HrmRecruitPostDO::getOwnerEmployeeId, null)
                .set(updateObj.getInterviewEmployeeIds() == null, HrmRecruitPostDO::getInterviewEmployeeIds, null)
                .set(updateObj.getDescription() == null, HrmRecruitPostDO::getDescription, null)
                .set(updateObj.getEmergencyLevel() == null, HrmRecruitPostDO::getEmergencyLevel, null)
                .set(updateObj.getPostTypeId() == null, HrmRecruitPostDO::getPostTypeId, null));
    }

    private QueryWrapperX<HrmRecruitPostDO> buildQueryWrapper(HrmRecruitPostPageReqVO reqVO) {
        return new QueryWrapperX<HrmRecruitPostDO>()
                .likeIfPresent("post_name", reqVO.getPostName())
                .eqIfPresent("job_nature", reqVO.getJobNature())
                .eqIfPresent("area_id", reqVO.getAreaId())
                .eqIfPresent("dept_id", reqVO.getDeptId())
                .eqIfPresent("owner_employee_id", reqVO.getOwnerEmployeeId())
                .eqIfPresent("post_type_id", reqVO.getPostTypeId())
                .betweenIfPresent("create_time", reqVO.getCreateTime());
    }

}
