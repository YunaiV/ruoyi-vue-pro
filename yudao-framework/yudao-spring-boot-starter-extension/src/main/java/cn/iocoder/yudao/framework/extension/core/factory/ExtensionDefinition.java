package cn.iocoder.yudao.framework.extension.core.factory;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * @description 扩展定义（扩展坐标），标识唯一一个业务场景实现
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 23:14
 * @class cn.iocoder.yudao.framework.extension.core.factory.ExtensionDefinition.java
 */
@Setter
@Getter
public class ExtensionDefinition implements Serializable {

    /**
     * 业务场景唯一标识(id)
     */
    private String uniqueIdentify;

    /**
     * 扩展点实现类名称
     */
    private String extensionPointName;

    /**
     * 业务场景
     */
    private BusinessScenario businessScenario;

    /**
     * 扩展点实现类
     */
    private ExtensionPoint extensionPoint;

    /**
     * class
     */
    private Class extensionPointClass;

    public ExtensionDefinition() {
    }

    public ExtensionDefinition(@NotNull BusinessScenario businessScenario, @NotNull ExtensionPoint extensionPoint) {
        this.businessScenario = businessScenario;
        this.extensionPoint = extensionPoint;
        this.uniqueIdentify = this.businessScenario.getUniqueIdentity();
        this.extensionPointClass = this.extensionPoint.getClass();
    }

    /**
     * 构建definition
     * @param businessScenario
     * @param point
     * @return
     */
    public static ExtensionDefinition valueOf(@NotNull BusinessScenario businessScenario, @NotNull ExtensionPoint point) {
        return new ExtensionDefinition(businessScenario, point);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExtensionDefinition that = (ExtensionDefinition) o;
        return Objects.equals(uniqueIdentify, that.uniqueIdentify) && Objects.equals(extensionPointName, that.extensionPointName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueIdentify == null) ? 0 : uniqueIdentify.hashCode());
        result = prime * result + ((extensionPointName == null) ? 0 : extensionPointName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ExtensionDefinition{" +
                "uniqueIdentify='" + uniqueIdentify + '\'' +
                ", extensionPointName='" + extensionPointName + '\'' +
                '}';
    }
}
