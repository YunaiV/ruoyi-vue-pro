package cn.iocoder.yudao.module.mes.dal.mysql.wm.miscissue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 杂项出库明细 Mapper
 */
@Mapper
public interface MesWmMiscIssueDetailMapper extends BaseMapperX<MesWmMiscIssueDetailDO> {

    default List<MesWmMiscIssueDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmMiscIssueDetailDO::getLineId, lineId);
    }

    default List<MesWmMiscIssueDetailDO> selectListByIssueId(Long issueId) {
        return selectList(MesWmMiscIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByIssueId(Long issueId) {
        delete(MesWmMiscIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByLineId(Long lineId) {
        delete(MesWmMiscIssueDetailDO::getLineId, lineId);
    }

}
