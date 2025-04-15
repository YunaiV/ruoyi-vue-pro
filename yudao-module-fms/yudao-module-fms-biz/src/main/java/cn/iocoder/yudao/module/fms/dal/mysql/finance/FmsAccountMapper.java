package cn.iocoder.yudao.module.fms.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.account.FmsAccountPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsAccountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 结算账户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsAccountMapper extends BaseMapperX<FmsAccountDO> {

    default PageResult<FmsAccountDO> selectPage(FmsAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FmsAccountDO>()
            .likeIfPresent(FmsAccountDO::getName, reqVO.getName())
            .likeIfPresent(FmsAccountDO::getNo, reqVO.getNo())
            .eqIfPresent(FmsAccountDO::getRemark, reqVO.getRemark())
            .orderByDesc(FmsAccountDO::getId));
    }

    default FmsAccountDO selectByDefaultStatus() {
        return selectOne(FmsAccountDO::getDefaultStatus, true);
    }

    default List<FmsAccountDO> selectListByStatus(Integer status) {
        return selectList(FmsAccountDO::getStatus, status);
    }

}