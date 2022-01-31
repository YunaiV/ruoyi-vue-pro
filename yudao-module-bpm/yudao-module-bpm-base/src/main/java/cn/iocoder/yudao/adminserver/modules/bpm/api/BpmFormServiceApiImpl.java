package cn.iocoder.yudao.adminserver.modules.bpm.api;

import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmFormConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmFormMapper;
import cn.iocoder.yudao.coreservice.modules.bpm.api.form.BpmFormServiceApi;
import cn.iocoder.yudao.coreservice.modules.bpm.api.form.dto.BpmFormDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
/**
 * 动态表单 Api Service 实现类
 *
 * @author 风里雾里
 * @author jason
 */
@Service
public class BpmFormServiceApiImpl implements BpmFormServiceApi {

    @Resource
    private BpmFormMapper formMapper;

    @Override
    public BpmFormDTO getForm(Long id) {
        return BpmFormConvert.INSTANCE.convert1(formMapper.selectById(id));
    }

    @Override
    public List<BpmFormDTO> getFormList(Collection<Long> ids) {
        return BpmFormConvert.INSTANCE.convertList(formMapper.selectBatchIds(ids));
    }
}
