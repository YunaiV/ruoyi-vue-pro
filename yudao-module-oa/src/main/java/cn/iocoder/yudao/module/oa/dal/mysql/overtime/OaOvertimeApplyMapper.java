package cn.iocoder.yudao.module.oa.dal.mysql.overtime;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.overtime.OaOvertimeApplyDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 加班申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaOvertimeApplyMapper extends BaseMapperX<OaOvertimeApplyDO> {

    default PageResult<OaOvertimeApplyDO> selectPage(Long userId, OaOvertimeApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaOvertimeApplyDO>()
                .eq(OaOvertimeApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaOvertimeApplyDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaOvertimeApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(OaOvertimeApplyDO::getCreateTime, OaOvertimeApplyDO::getId));
    }

}
