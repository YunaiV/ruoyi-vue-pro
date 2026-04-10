package cn.iocoder.yudao.module.mes.dal.mysql.wm.sn;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnGroupRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo.MesWmSnPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.sn.MesWmSnDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES SN 码 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmSnMapper extends BaseMapperX<MesWmSnDO> {

    /**
     * 按 UUID 分组查询 SN 码分页（聚合查询）
     */
    default PageResult<MesWmSnGroupRespVO> selectPageGroupByUuid(MesWmSnPageReqVO reqVO) {
        MPJLambdaWrapperX<MesWmSnDO> query = new MPJLambdaWrapperX<>();
        query.eqIfPresent(MesWmSnDO::getUuid, reqVO.getUuid())
                .likeIfPresent(MesWmSnDO::getCode, reqVO.getCode())
                .eqIfPresent(MesWmSnDO::getItemId, reqVO.getItemId())
                .likeIfPresent(MesWmSnDO::getBatchCode, reqVO.getBatchCode())
                .betweenIfPresent(MesWmSnDO::getCreateTime, reqVO.getCreateTime());
        query.selectAs(MesWmSnDO::getUuid, MesWmSnGroupRespVO::getUuid)
                .selectMax(MesWmSnDO::getItemId, MesWmSnGroupRespVO::getItemId)
                .selectMax(MesWmSnDO::getBatchCode, MesWmSnGroupRespVO::getBatchCode)
                .selectMax(MesWmSnDO::getWorkOrderId, MesWmSnGroupRespVO::getWorkOrderId)
                .selectMax(MesWmSnDO::getCreateTime, MesWmSnGroupRespVO::getCreateTime)
                .selectAs("COUNT(*)", MesWmSnGroupRespVO::getCount)
                .groupBy(MesWmSnDO::getUuid)
                .last("ORDER BY MAX(t.create_time) DESC"); // 避免 this is incompatible with sql_mode=only_full_group_by 报错
        return selectJoinPage(reqVO, MesWmSnGroupRespVO.class, query);
    }

    default List<MesWmSnDO> selectListByUuid(String uuid) {
        return selectList(new LambdaQueryWrapperX<MesWmSnDO>()
                .eq(MesWmSnDO::getUuid, uuid)
                .orderByAsc(MesWmSnDO::getId));
    }

    default int deleteByUuid(String uuid) {
        return delete(new LambdaQueryWrapperX<MesWmSnDO>()
                .eq(MesWmSnDO::getUuid, uuid));
    }

}
