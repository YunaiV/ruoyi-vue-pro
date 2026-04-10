package cn.iocoder.yudao.module.mes.dal.mysql.wm.productissue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 领料出库明细 Mapper
 */
@Mapper
public interface MesWmProductIssueDetailMapper extends BaseMapperX<MesWmProductIssueDetailDO> {

    default List<MesWmProductIssueDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmProductIssueDetailDO::getLineId, lineId);
    }

    default List<MesWmProductIssueDetailDO> selectListByIssueId(Long issueId) {
        return selectList(MesWmProductIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByIssueId(Long issueId) {
        delete(MesWmProductIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByLineId(Long lineId) {
        delete(MesWmProductIssueDetailDO::getLineId, lineId);
    }

}
