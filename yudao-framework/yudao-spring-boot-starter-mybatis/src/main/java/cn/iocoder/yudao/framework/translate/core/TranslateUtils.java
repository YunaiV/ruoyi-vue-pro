package cn.iocoder.yudao.framework.translate.core;

import cn.hutool.core.collection.CollUtil;
import com.fhs.core.trans.vo.VO;
import com.fhs.trans.service.impl.TransService;

import java.util.List;

/**
 * VO 数据翻译 Utils
 *
 * @author 芋道源码
 */
public class TranslateUtils {

    private static TransService transService;

    public static void init(TransService transService) {
        TranslateUtils.transService = transService;
    }

    /**
     * 数据翻译
     *
     * 使用场景：无法使用 @TransMethodResult 注解的场景，只能通过手动触发翻译
     *
     * @param data 数据
     * @return 翻译结果
     */
    public static <T extends VO> List<T> translate(List<T> data) {
        if (CollUtil.isNotEmpty((data))) {
            transService.transBatch(data);
        }
        return data;
    }

}
