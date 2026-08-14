package cn.iocoder.yudao.module.hrm.dal.mysql.attendance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmAttendanceGroupMapper extends BaseMapperX<HrmAttendanceGroupDO> {

    default PageResult<HrmAttendanceGroupDO> selectPage(HrmAttendanceGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmAttendanceGroupDO>()
                .likeIfPresent(HrmAttendanceGroupDO::getName, reqVO.getName())
                .eqIfPresent(HrmAttendanceGroupDO::getDefaultStatus, reqVO.getDefaultStatus())
                .orderByDesc(HrmAttendanceGroupDO::getId));
    }

    default HrmAttendanceGroupDO selectByName(String name) {
        return selectFirstOne(HrmAttendanceGroupDO::getName, name);
    }

    default List<HrmAttendanceGroupDO> selectListOrderByDefaultStatusAndId() {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceGroupDO>()
                .orderByDesc(HrmAttendanceGroupDO::getDefaultStatus)
                .orderByDesc(HrmAttendanceGroupDO::getId));
    }

}
