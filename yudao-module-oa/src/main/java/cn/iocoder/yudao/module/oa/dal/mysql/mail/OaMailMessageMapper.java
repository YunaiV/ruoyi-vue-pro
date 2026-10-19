package cn.iocoder.yudao.module.oa.dal.mysql.mail;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.OaMailMessagePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailMessageDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业邮箱邮件索引 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaMailMessageMapper extends BaseMapperX<OaMailMessageDO> {

    default OaMailMessageDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(OaMailMessageDO::getId, id);
    }

    default Map<Long, Long> selectCountMapByAccountIdAndReadStatus(Long accountId, Boolean readStatus) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapper<OaMailMessageDO>()
                .select("folder_id AS FOLDER_ID", "COUNT(*) AS UNREAD_COUNT")
                .eq("account_id", accountId)
                .eq("read_status", readStatus)
                .groupBy("folder_id"));
        Map<Long, Long> result = new HashMap<>();
        rows.forEach(row -> result.put(((Number) row.get("FOLDER_ID")).longValue(),
                ((Number) row.get("UNREAD_COUNT")).longValue()));
        return result;
    }

    default List<OaMailMessageDO> selectListByFolderId(Long folderId) {
        return selectList(OaMailMessageDO::getFolderId, folderId);
    }

    default PageResult<OaMailMessageDO> selectPage(OaMailMessagePageReqVO reqVO, Long folderId) {
        LambdaQueryWrapperX<OaMailMessageDO> query = new LambdaQueryWrapperX<OaMailMessageDO>()
                .eq(OaMailMessageDO::getAccountId, reqVO.getAccountId())
                .eq(OaMailMessageDO::getFolderId, folderId)
                .eqIfPresent(OaMailMessageDO::getReadStatus, reqVO.getReadStatus())
                .eqIfPresent(OaMailMessageDO::getHasAttach, reqVO.getHasAttach());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            query.and(q -> q.like(OaMailMessageDO::getSubject, reqVO.getKeyword())
                    .or().like(OaMailMessageDO::getSender, reqVO.getKeyword()));
        }
        return selectPage(reqVO, query.orderByDesc(OaMailMessageDO::getReceiveTime)
                .orderByDesc(OaMailMessageDO::getId));
    }

}
