package cn.iocoder.yudao.module.oa.dal.mysql.resign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.resign.OaResignApplyDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 离职申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaResignApplyMapper extends BaseMapperX<OaResignApplyDO> {

    default PageResult<OaResignApplyDO> selectPage(Long userId, OaResignApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaResignApplyDO>()
                .eq(OaResignApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaResignApplyDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaResignApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(OaResignApplyDO::getCreateTime, OaResignApplyDO::getId));
    }

}
