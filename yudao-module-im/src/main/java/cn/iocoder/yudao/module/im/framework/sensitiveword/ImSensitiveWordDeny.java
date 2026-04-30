package cn.iocoder.yudao.module.im.framework.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import com.github.houbb.sensitive.word.api.IWordDeny;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

// TODO @AI：是不是可以，直接塞入一个数组。而不是这里查询？？？
/**
 * 自定义敏感词数据来源：从 im_sensitive_word 表加载启用状态的敏感词
 * <p>
 * 由 {@link com.github.houbb.sensitive.word.bs.SensitiveWordBs} 在 init() 时回调，
 * 因此 CRUD 完成后调用 sensitiveWordBs.init() 即可实现热更新。
 *
 * @author 芋道源码
 */
@Component
public class ImSensitiveWordDeny implements IWordDeny {

    @Resource
    private ImSensitiveWordMapper sensitiveWordMapper;

    @Override
    public List<String> deny() {
        return convertList(
                sensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                ImSensitiveWordDO::getWord);
    }

}
