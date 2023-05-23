package cn.iocoder.yudao.module.jl.convert.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;

/**
 * 销售线索中的项目售前支持人员 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2managerConvert {

    JoinSaleslead2managerConvert INSTANCE = Mappers.getMapper(JoinSaleslead2managerConvert.class);

    JoinSaleslead2managerDO convert(JoinSaleslead2managerCreateReqVO bean);

    JoinSaleslead2managerDO convert(JoinSaleslead2managerUpdateReqVO bean);

    JoinSaleslead2managerRespVO convert(JoinSaleslead2managerDO bean);

    List<JoinSaleslead2managerRespVO> convertList(List<JoinSaleslead2managerDO> list);

    PageResult<JoinSaleslead2managerRespVO> convertPage(PageResult<JoinSaleslead2managerDO> page);

    List<JoinSaleslead2managerExcelVO> convertList02(List<JoinSaleslead2managerDO> list);

}
