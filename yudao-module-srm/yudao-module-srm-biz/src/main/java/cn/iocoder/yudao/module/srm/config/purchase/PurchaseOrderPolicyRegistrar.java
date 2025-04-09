package cn.iocoder.yudao.module.srm.config.purchase;

import cn.iocoder.yudao.framework.template.config.TemplatePolicyRegistrar;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderPolicyRegistrar implements TemplatePolicyRegistrar {
    @Override
    public void register(ConfigureBuilder builder) {
        builder.bind("products", new LoopRowTableRenderPolicy());
    }
}
