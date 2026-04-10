package cn.iocoder.yudao.module.mes.dal.mysql.pro.card;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardProcessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 流转卡工序记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesProCardProcessMapper extends BaseMapperX<MesProCardProcessDO> {

    default PageResult<MesProCardProcessDO> selectPage(MesProCardProcessPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesProCardProcessDO>()
                .eqIfPresent(MesProCardProcessDO::getCardId, reqVO.getCardId())
                .eqIfPresent(MesProCardProcessDO::getProcessId, reqVO.getProcessId())
                .eqIfPresent(MesProCardProcessDO::getWorkstationId, reqVO.getWorkstationId())
                .eqIfPresent(MesProCardProcessDO::getUserId, reqVO.getUserId())
                .orderByAsc(MesProCardProcessDO::getSort));
    }

    default List<MesProCardProcessDO> selectListByCardId(Long cardId) {
        return selectList(MesProCardProcessDO::getCardId, cardId);
    }

    default void deleteByCardId(Long cardId) {
        delete(MesProCardProcessDO::getCardId, cardId);
    }

}
