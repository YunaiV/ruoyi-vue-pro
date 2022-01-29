package cn.iocoder.yudao.module.system.convert.logger;

import cn.iocoder.yudao.module.system.controller.logger.vo.operatelog.SysOperateLogExcelVO;
import cn.iocoder.yudao.module.system.controller.logger.vo.operatelog.SysOperateLogRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.operatelog.core.dto.OperateLogCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;

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
