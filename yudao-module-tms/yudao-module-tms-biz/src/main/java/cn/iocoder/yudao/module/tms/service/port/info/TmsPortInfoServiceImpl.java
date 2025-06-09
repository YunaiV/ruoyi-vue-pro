package cn.iocoder.yudao.module.tms.service.port.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.port.info.TmsPortInfoDO;
import cn.iocoder.yudao.module.tms.dal.mysql.port.info.TmsPortInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.PORT_INFO_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.PORT_INFO_NOT_EXISTS;

/**
 * TMS港口信息 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsPortInfoServiceImpl implements TmsPortInfoService {

    @Resource
    private TmsPortInfoMapper portInfoMapper;

    @Override
    public Integer createPortInfo(TmsPortInfoSaveReqVO createReqVO) {
        // 校验港口名称重复
        validatePortInfoNameDuplicate(null, createReqVO.getName());
        // 插入
        TmsPortInfoDO portInfo = BeanUtils.toBean(createReqVO, TmsPortInfoDO.class);
        portInfoMapper.insert(portInfo);
        // 返回
        return portInfo.getId();
    }

    @Override
    public void updatePortInfo(TmsPortInfoSaveReqVO updateReqVO) {
        // 校验存在
        validatePortInfoExists(updateReqVO.getId());
        // 校验港口名称重复
        validatePortInfoNameDuplicate(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        TmsPortInfoDO updateObj = BeanUtils.toBean(updateReqVO, TmsPortInfoDO.class);
        portInfoMapper.updateById(updateObj);
    }

    @Override
    public void deletePortInfo(Integer id) {
        // 校验存在
        validatePortInfoExists(id);
        // 删除
        portInfoMapper.deleteById(id);
    }

    private void validatePortInfoExists(Integer id) {
        if (portInfoMapper.selectById(id) == null) {
            throw exception(PORT_INFO_NOT_EXISTS);
        }
    }

    private void validatePortInfoNameDuplicate(Integer id, String name) {
        TmsPortInfoDO portInfo = portInfoMapper.selectOne(TmsPortInfoDO::getName, name);
        if (portInfo != null && !portInfo.getId().equals(id)) {
            throw exception(PORT_INFO_NAME_DUPLICATE, name);
        }
    }

    @Override
    public TmsPortInfoDO getPortInfo(Integer id) {
        return portInfoMapper.selectById(id);
    }

    @Override
    public PageResult<TmsPortInfoDO> getPortInfoPage(TmsPortInfoPageReqVO pageReqVO) {
        return portInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TmsPortInfoDO> getPortInfoList() {
        return portInfoMapper.selectList();
    }
}