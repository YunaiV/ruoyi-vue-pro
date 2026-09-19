package cn.iocoder.yudao.module.oa.dal.mysql.supply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * OA 用品领用申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaSupplyApplyMapper extends BaseMapperX<OaSupplyApplyDO> {

    default PageResult<OaSupplyApplyDO> selectPage(OaSupplyApplyPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaSupplyApplyDO>()
                .eq(OaSupplyApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaSupplyApplyDO::getNo, reqVO.getNo())
                .eqIfPresent(OaSupplyApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaSupplyApplyDO::getDeptId, reqVO.getDeptId())
                .betweenIfPresent(OaSupplyApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OaSupplyApplyDO::getId));
    }

    default OaSupplyApplyDO selectByNo(String no) {
        return selectOne(OaSupplyApplyDO::getNo, no);
    }

    default int updateStatusAndClearProcessInstanceId(Long id, Integer oldStatus, Integer status) {
        return update(new LambdaUpdateWrapper<OaSupplyApplyDO>().eq(OaSupplyApplyDO::getId, id)
                .eq(OaSupplyApplyDO::getStatus, oldStatus)
                .set(OaSupplyApplyDO::getStatus, status).set(OaSupplyApplyDO::getProcessInstanceId, null));
    }

}
