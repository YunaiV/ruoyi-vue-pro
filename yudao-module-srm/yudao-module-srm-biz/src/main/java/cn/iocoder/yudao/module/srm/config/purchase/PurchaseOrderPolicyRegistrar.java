package cn.iocoder.yudao.module.srm.config.purchase;

import cn.iocoder.yudao.framework.template.config.TemplatePolicyRegistrar;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class PurchaseOrderPolicyRegistrar implements TemplatePolicyRegistrar {
    @Autowired
    PurchaseProperties purchaseProperties;
    @Override
    public void register(ConfigureBuilder builder) {
        builder.bind("products", new LoopRowTableRenderPolicy());
    }

    @Override
    public boolean enablePreload() {
        return purchaseProperties.isEnablePreload();
    }

    @Override
    public Collection<String> scanPath() {
        return purchaseProperties.getScanPath();
    }
}
