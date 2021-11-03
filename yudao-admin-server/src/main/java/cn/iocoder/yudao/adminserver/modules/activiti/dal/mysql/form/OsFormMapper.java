package cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.form;



import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.OsFormDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 动态表单 Mapper
 *
 * @author 芋艿
 */
@Mapper
public interface OsFormMapper extends BaseMapperX<OsFormDO> {

    default PageResult<OsFormDO> selectPage(OsFormPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<OsFormDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

    default List<OsFormDO> selectList(OsFormExportReqVO reqVO) {
        return selectList(new QueryWrapperX<OsFormDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

}
