package cn.iocoder.yudao.module.mes.dal.mysql.wm.returnissue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 生产退料明细 Mapper
 */
@Mapper
public interface MesWmReturnIssueDetailMapper extends BaseMapperX<MesWmReturnIssueDetailDO> {

    default List<MesWmReturnIssueDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmReturnIssueDetailDO::getLineId, lineId);
    }

    default List<MesWmReturnIssueDetailDO> selectListByIssueId(Long issueId) {
        return selectList(MesWmReturnIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByIssueId(Long issueId) {
        delete(MesWmReturnIssueDetailDO::getIssueId, issueId);
    }

    default void deleteByLineId(Long lineId) {
        delete(MesWmReturnIssueDetailDO::getLineId, lineId);
    }

}
