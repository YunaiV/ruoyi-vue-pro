package cn.iocoder.yudao.module.mp.dal.mysql.subscribetext;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo.*;

/**
 * 关注欢迎语 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxSubscribeTextMapper extends BaseMapperX<WxSubscribeTextDO> {

    default PageResult<WxSubscribeTextDO> selectPage(WxSubscribeTextPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxSubscribeTextDO>()
                .eqIfPresent(WxSubscribeTextDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxSubscribeTextDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxSubscribeTextDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxSubscribeTextDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxSubscribeTextDO::getId));
    }

    default List<WxSubscribeTextDO> selectList(WxSubscribeTextExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxSubscribeTextDO>()
                .eqIfPresent(WxSubscribeTextDO::getMsgType, reqVO.getMsgType())
                .eqIfPresent(WxSubscribeTextDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxSubscribeTextDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxSubscribeTextDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxSubscribeTextDO::getId));
    }

}
