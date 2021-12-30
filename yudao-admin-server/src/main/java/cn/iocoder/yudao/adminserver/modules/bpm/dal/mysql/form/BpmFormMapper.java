package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.form;


import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmForm;
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
public interface BpmFormMapper extends BaseMapperX<BpmForm> {

    default PageResult<BpmForm> selectPage(BpmFormPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<BpmForm>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<BpmForm> selectList(BpmFormExportReqVO reqVO) {
        return selectList(new QueryWrapperX<BpmForm>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("form_json", reqVO.getFormJson())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

}
