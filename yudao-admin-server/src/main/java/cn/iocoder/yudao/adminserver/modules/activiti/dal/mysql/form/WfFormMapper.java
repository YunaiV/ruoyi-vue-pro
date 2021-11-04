package cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.form;



import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.WfForm;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 动态表单 Mapper
 *
 * @author 风里雾里
 */
@Mapper
public interface WfFormMapper extends BaseMapperX<WfForm> {

    default PageResult<WfForm> selectPage(WfFormPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<WfForm>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

    default List<WfForm> selectList(WfFormExportReqVO reqVO) {
        return selectList(new QueryWrapperX<WfForm>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

}
