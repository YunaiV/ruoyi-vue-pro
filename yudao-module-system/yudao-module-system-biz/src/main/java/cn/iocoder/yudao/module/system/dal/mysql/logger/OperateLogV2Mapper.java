package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogV2DO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface OperateLogV2Mapper extends BaseMapperX<OperateLogV2DO> {

    default PageResult<OperateLogV2DO> selectPage(OperateLogPageReqVO reqVO, Collection<Long> userIds) {
        LambdaQueryWrapperX<OperateLogV2DO> query = new LambdaQueryWrapperX<OperateLogV2DO>()
                .likeIfPresent(OperateLogV2DO::getModule, reqVO.getModule())
                .inIfPresent(OperateLogV2DO::getUserId, userIds);
        query.orderByDesc(OperateLogV2DO::getId); // 降序
        return selectPage(reqVO, query);
    }

}
