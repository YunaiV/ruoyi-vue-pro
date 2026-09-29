package cn.iocoder.yudao.module.ai1.dal.mysql.mcp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo.Ai1McpPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI1 MCP 服务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1McpMapper extends BaseMapperX<Ai1McpDO> {

    default PageResult<Ai1McpDO> selectPage(Ai1McpPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1McpDO>()
                .likeIfPresent(Ai1McpDO::getName, reqVO.getName())
                .eqIfPresent(Ai1McpDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1McpDO::getId));
    }

    default List<Ai1McpDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1McpDO>()
                .eq(Ai1McpDO::getStatus, status)
                .orderByAsc(Ai1McpDO::getId));
    }

    default void updateForSave(Ai1McpDO mcp) {
        update(mcp, new LambdaUpdateWrapper<Ai1McpDO>().eq(Ai1McpDO::getId, mcp.getId())
                .set(mcp.getUrl() == null, Ai1McpDO::getUrl, null));
    }

}
