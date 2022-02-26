package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件操作 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<FileDO>()
                .likeIfPresent("id", reqVO.getId())
                .likeIfPresent("type", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("create_time"));
    }

    default Long selectCountById(String id) {
        return selectCount(FileDO::getId, id);
    }

    /**
     * 基于 Path 获取文件
     * 实际上，是基于 ID 查询
     *
     * @param path 路径
     * @return 文件
     */
    default FileDO selectByPath(String path) {
        return selectById(path);
    }

}
