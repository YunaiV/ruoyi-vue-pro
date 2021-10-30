package cn.iocoder.yudao.framework.extension.core;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.StringJoiner;

/**
 * @description 业务场景 = businessId + useCase + scenario, 用来标识系统中唯一的一个场景<br/>
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 22:19
 * @class cn.iocoder.yudao.framework.extension.core.BusinessScenario.java
 */
public class BusinessScenario implements Serializable {

    /**
     * 默认业务id
     */
    public final static String DEFAULT_BUSINESS_ID = "#defaultBusinessId#";

    /**
     * 默认用例
     */
    public final static String DEFAULT_USECASE = "#defaultUseCase#";

    /**
     * 默认场景
     */
    public final static String DEFAULT_SCENARIO = "#defaultScenario#";

    /**
     * 分隔符
     */
    private final static String DOT_SEPARATOR = ".";

    /**
     * 业务Id
     */
    private String businessId;

    /**
     * 用例
     */
    private String useCase;

    /**
     * 场景
     */
    private String scenario;

    public BusinessScenario() {
        this.businessId = DEFAULT_BUSINESS_ID;
        this.useCase = DEFAULT_USECASE;
        this.scenario = DEFAULT_SCENARIO;
    }

    public BusinessScenario(@NotNull String businessId, @NotNull String useCase, @NotNull String scenario) {
        this.businessId = businessId;
        this.useCase = useCase;
        this.scenario = scenario;
    }

    public BusinessScenario(@NotNull String scenario) {
        this();
        this.scenario = scenario;
    }

    public BusinessScenario(@NotNull String useCase, @NotNull String scenario) {
        this(DEFAULT_BUSINESS_ID, useCase, scenario);
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * 构建业务场景
     * @param businessId
     * @param useCase
     * @param scenario
     * @return
     */
    public static BusinessScenario valueOf(@NotNull String businessId, @NotNull String useCase, @NotNull String scenario) {
        return new BusinessScenario(businessId, useCase, scenario);
    }

    /**
     * 构建业务场景
     * @param useCase
     * @param scenario
     * @return
     */
    public static BusinessScenario valueOf(@NotNull String useCase, @NotNull String scenario) {
        return new BusinessScenario(useCase, scenario);
    }

    /**
     * 构建业务场景
     * @param scenario
     * @return
     */
    public static BusinessScenario valueOf(@NotNull String scenario) {
        return new BusinessScenario(scenario);
    }

    /**
     * 业务场景唯一标识
     * @return
     */
    public String getUniqueIdentity(){
        return new StringJoiner(DOT_SEPARATOR).add(businessId).add(useCase).add(scenario).toString();
    }

    @Override
    public String toString() {
        return "BusinessScenario{" +
                "businessId='" + businessId + '\'' +
                ", useCase='" + useCase + '\'' +
                ", scenario='" + scenario + '\'' +
                '}';
    }
}
