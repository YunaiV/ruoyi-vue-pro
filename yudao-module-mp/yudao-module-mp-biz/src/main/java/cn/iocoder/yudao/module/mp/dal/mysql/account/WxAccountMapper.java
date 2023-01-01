package cn.iocoder.yudao.module.mp.dal.mysql.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountPageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公众号账户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountMapper extends BaseMapperX<WxAccountDO> {

    default PageResult<WxAccountDO> selectPage(WxAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxAccountDO>()
                .likeIfPresent(WxAccountDO::getName, reqVO.getName())
                .eqIfPresent(WxAccountDO::getAccount, reqVO.getAccount())
                .eqIfPresent(WxAccountDO::getAppId, reqVO.getAppId())
                .betweenIfPresent(WxAccountDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountDO::getId));
    }

    default List<WxAccountDO> selectList(WxAccountExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxAccountDO>()
                .likeIfPresent(WxAccountDO::getName, reqVO.getName())
                .eqIfPresent(WxAccountDO::getAccount, reqVO.getAccount())
                .eqIfPresent(WxAccountDO::getAppId, reqVO.getAppId())
                .betweenIfPresent(WxAccountDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountDO::getId));
    }

}
