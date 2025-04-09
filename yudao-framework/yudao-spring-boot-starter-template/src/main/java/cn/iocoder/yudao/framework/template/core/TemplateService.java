package cn.iocoder.yudao.framework.template.core;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import org.springframework.core.io.Resource;

/**
 * 模板预加载接口
 * 用于定义模板预加载和缓存管理的相关方法，统一模板数据的获取和缓存刷新操作。 实现类应该负责将模板数据缓存在 Redis 或其他分布式缓存中，以提高性能和可靠性。
 */
public interface TemplateService {

    /**
     * 从资源对象中读取模板数据
     *
     * @param resource 资源对象
     * @return 模板字节数据
     */
    byte[] getTemplateBytes(Resource resource);

    /**
     * 从类路径获取模板并编译
     *
     * @param resource      模板路径，相对于类路径。purchase/order/外币采购合同_英文.docx。
     * @return 编译后的模板
     */
    XWPFTemplate buildXWPDFTemplate(Resource resource);

    /**
     * 重新编译模板
     *
     * @param resource      模板路径，相对于类路径。purchase/ order/ 外币采购合同_英文. docx。
     * @return 编译后的模板
     */
    XWPFTemplate reBuildXWPDFTemplate(Resource resource);

    //refreshTemplateBytes
    byte[] refreshTemplateBytes(Resource resource);

    /**
     * 从类路径获取模板并编译后的字节数组
     *
     * @param resource
     * @return byte[]
     */
    byte[] getTemplateBytesByPath(Resource resource);

}
