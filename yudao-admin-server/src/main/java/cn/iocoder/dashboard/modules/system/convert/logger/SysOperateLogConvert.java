package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.dto.OperateLogCreateReqDTO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogRespVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.util.collection.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;

@Mapper
public interface SysOperateLogConvert {

    SysOperateLogConvert INSTANCE = Mappers.getMapper(SysOperateLogConvert.class);

    SysOperateLogDO convert(OperateLogCreateReqDTO bean);

    PageResult<SysOperateLogRespVO> convertPage(PageResult<SysOperateLogDO> page);

    SysOperateLogRespVO convert(SysOperateLogDO bean);

    default List<SysOperateLogExcelVO> convertList(List<SysOperateLogDO> list, Map<Long, SysUserDO> userMap) {
        return list.stream().map(operateLog -> {
            SysOperateLogExcelVO excelVO = convert02(operateLog);
            MapUtils.findAndThen(userMap, operateLog.getId(), user -> excelVO.setUserNickname(user.getNickname()));
            excelVO.setSuccessStr(SUCCESS.getCode().equals(operateLog.getResultCode()) ? "成功" : "失败");
            return excelVO;
        }).collect(Collectors.toList());
    }

    SysOperateLogExcelVO convert02(SysOperateLogDO bean);

}
