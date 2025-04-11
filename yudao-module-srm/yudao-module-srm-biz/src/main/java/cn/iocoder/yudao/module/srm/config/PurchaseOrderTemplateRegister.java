package cn.iocoder.yudao.module.srm.config;

import cn.iocoder.yudao.framework.template.config.TemplatePolicy;
import cn.iocoder.yudao.framework.template.config.TemplateRegister;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.deepoove.poi.policy.RenderPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Component
public class PurchaseOrderTemplateRegister implements TemplateRegister {

    String FILEPATH = "classpath:purchase/order/*.docx";
    @Value("${srm.enable-preload:false}")
    private boolean enablePreload;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Override
    public List<TemplatePolicy> getTemplatePolicies() {
        ArrayList<TemplatePolicy> list = new ArrayList<>();
        Resource[] resources = null;
        try {
            resources = resourcePatternResolver.getResources(FILEPATH);
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL, FILEPATH, e);
        }
        for (Resource resource : resources) {
            HashMap<String, RenderPolicy> map = new HashMap<>();
            map.put("products", new LoopRowTableRenderPolicy());
//            map.put("products", new LoopRowTableRenderPolicy());
            list.add(TemplatePolicy.builder().policies(List.of(map)).enablePreload(enablePreload).resource(resource).build());
        }
        return list;
    }
}
