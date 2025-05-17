package cn.iocoder.yudao.module.ai.framework.ai.core.model.ppt.wdd;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.wenduoduo.api.WenDuoDuoPptApi;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

/**
 * {@link WenDuoDuoPptApi} 集成测试
 *
 * @author xiaoxin
 */
@Disabled
public class WenDuoDuoPptApiTests {

    private final String token = ""; // API Token
    private final WenDuoDuoPptApi wenDuoDuoPptApi = new WenDuoDuoPptApi(token);

    @Test
    @Disabled
    public void testCreateApiToken() {
        // 准备参数
        String apiKey = "";
        WenDuoDuoPptApi.CreateTokenRequest request = new WenDuoDuoPptApi.CreateTokenRequest(apiKey);
        // 调用方法
        String token = wenDuoDuoPptApi.createApiToken(request);
        // 打印结果
        System.out.println(token);
    }

    /**
     * 创建任务
     */
    @Test
    @Disabled
    public void testCreateTask() {
        WenDuoDuoPptApi.ApiResponse apiResponse = wenDuoDuoPptApi.createTask(1, "dify 介绍", null);
        System.out.println(apiResponse);
    }


    @Test // 创建大纲
    @Disabled
    public void testGenerateOutlineRequest() {
        WenDuoDuoPptApi.CreateOutlineRequest request = new WenDuoDuoPptApi.CreateOutlineRequest(
                "1901539019628613632", "medium", null, null, null, null);
        // 调用
        Flux<Map<String, Object>> flux = wenDuoDuoPptApi.createOutline(request);
        StringBuffer contentBuffer = new StringBuffer();
        flux.doOnNext(chunk -> {
            contentBuffer.append(chunk.get("text"));
            if (Objects.equals(Integer.parseInt(String.valueOf(chunk.get("status"))), 4)) {
                // status 为 4，最终 markdown 结构树
                System.out.println(JsonUtils.toJsonString(chunk.get("result")));
                System.out.println(" ########################################################################");
            }
        }).then().block();
        // 打印结果
        System.out.println(contentBuffer);
    }

    /**
     * 修改大纲
     */
    @Test
    @Disabled
    public void testUpdateOutlineRequest() {
        WenDuoDuoPptApi.UpdateOutlineRequest request = new WenDuoDuoPptApi.UpdateOutlineRequest(
                "1901539019628613632", TEST_OUT_LINE_CONTENT, "精简一点，三个章节即可");
        // 调用
        Flux<Map<String, Object>> flux = wenDuoDuoPptApi.updateOutline(request);
        StringBuffer contentBuffer = new StringBuffer();
        flux.doOnNext(chunk -> {
            contentBuffer.append(chunk.get("text"));
            if (Objects.equals(Integer.parseInt(String.valueOf(chunk.get("status"))), 4)) {
                // status 为 4，最终 markdown 结构树
                System.out.println(JsonUtils.toJsonString(chunk.get("result")));
                System.out.println(" ########################################################################");
            }
        }).then().block();
        // 打印结果
        System.out.println(contentBuffer);

    }

    /**
     * 获取 PPT 模版分页
     */
    @Test
    @Disabled
    public void testGetPptTemplatePage() {
        // 准备参数
        WenDuoDuoPptApi.TemplateQueryRequest.Filter filter = new WenDuoDuoPptApi.TemplateQueryRequest.Filter(
                1, null, null, null);
        WenDuoDuoPptApi.TemplateQueryRequest request = new WenDuoDuoPptApi.TemplateQueryRequest(1, 10, filter);
        // 调用
        WenDuoDuoPptApi.PagePptTemplateInfo pptTemplatePage = wenDuoDuoPptApi.getTemplatePage(request);
        // 打印结果
        System.out.println(pptTemplatePage);
    }

    /**
     * 生成 PPT
     */
    @Test
    @Disabled
    public void testGeneratePptx() {
        // 准备参数
        WenDuoDuoPptApi.PptCreateRequest request = new WenDuoDuoPptApi.PptCreateRequest("1901539019628613632", "1805081814809960448", TEST_OUT_LINE_CONTENT);
        // 调用
        WenDuoDuoPptApi.PptInfo pptInfo = wenDuoDuoPptApi.create(request);
        // 打印结果
        System.out.println(pptInfo);
    }

    private final String TEST_OUT_LINE_CONTENT = """
            # Dify：新一代AI应用开发平台
            
            ## 1 什么是Dify
            ### 1.1 Dify定义：AI应用开发平台
            #### 1.1.1 低代码开发
            Dify是一个低代码AI应用开发平台，旨在简化AI应用的构建过程，让开发者无需编写大量代码即可快速创建各种智能应用。
            #### 1.1.2 核心功能
            Dify的核心功能包括数据集成、模型选择、流程编排和应用部署，提供一站式解决方案，加速AI应用的落地和迭代。
            #### 1.1.3 开源与商业
            Dify提供开源版本和商业版本，满足不同用户的需求，开源版本适合个人开发者和小型团队，商业版本则提供更强大的功能和技术支持。
            
            ### 1.2 Dify解决的问题：AI开发痛点
            #### 1.2.1 开发周期长
            传统AI应用开发周期长，需要大量的人力和时间投入，Dify通过可视化界面和预置组件，大幅缩短开发周期。
            #### 1.2.2 技术门槛高
            AI技术门槛高，需要专业的知识和技能，Dify降低技术门槛，让更多开发者能够参与到AI应用的开发中来。
            #### 1.2.3 部署和维护复杂
            AI应用的部署和维护复杂，需要专业的运维团队，Dify提供自动化的部署和维护工具，简化流程，降低成本。
            
            ### 1.3 Dify发展历程
            #### 1.3.1 早期探索
            Dify的早期版本主要关注于自然语言处理领域的应用，通过集成各种NLP模型，提供文本分类、情感分析等功能。
            #### 1.3.2 功能扩展
            随着用户需求的不断增长，Dify的功能逐渐扩展到图像识别、语音识别等领域，支持更多类型的AI应用。
            #### 1.3.3 生态建设
            Dify积极建设开发者生态，提供丰富的文档、教程和案例，帮助开发者更好地使用Dify平台，共同推动AI技术的发展。
            
            ## 2 Dify的核心功能
            ### 2.1 数据集成：连接各种数据源
            #### 2.1.1 支持多种数据源
            Dify支持连接各种数据源，包括关系型数据库、NoSQL数据库、文件系统、云存储等，满足不同场景的数据需求。
            #### 2.1.2 数据转换和清洗
            Dify提供数据转换和清洗功能，可以将不同格式的数据转换为统一的格式，并去除无效数据，提高数据质量。
            #### 2.1.3 数据安全
            Dify注重数据安全，采用各种安全措施保护用户的数据，包括数据加密、访问控制、权限管理等。
            
            ### 2.2 模型选择：丰富的AI模型库
            #### 2.2.1 预置模型
            Dify预置了丰富的AI模型，包括自然语言处理、图像识别、语音识别等领域的模型，开发者可以直接使用这些模型，无需自行训练，极大的简化了开发流程。
            #### 2.2.2 自定义模型
            Dify支持开发者上传自定义模型，满足个性化的需求。开发者可以将自己训练的模型部署到Dify平台上，与其他开发者共享。
            #### 2.2.3 模型评估
            Dify提供模型评估功能，可以对不同模型进行评估，选择最优的模型，提高应用性能。
            
            ### 2.3 流程编排：可视化流程设计器
            #### 2.3.1 可视化界面
            Dify提供可视化的流程设计器，开发者可以通过拖拽组件的方式，设计AI应用的流程，无需编写代码，简单高效。
            #### 2.3.2 灵活的流程控制
            Dify支持灵活的流程控制，可以根据不同的条件执行不同的分支，实现复杂的业务逻辑。
            #### 2.3.3 实时调试
            Dify提供实时调试功能，可以在设计流程的过程中，实时查看流程的执行结果，及时发现和解决问题。
            
            ### 2.4 应用部署：一键部署和管理
            #### 2.4.1 快速部署
            Dify提供一键部署功能，可以将AI应用快速部署到各种环境，包括本地环境、云环境、容器环境等。
            #### 2.4.2 自动伸缩
            Dify支持自动伸缩，可以根据应用的负载自动调整资源，保证应用的稳定性和性能。
            #### 2.4.3 监控和告警
            Dify提供监控和告警功能，可以实时监控应用的状态，并在出现问题时及时告警，方便运维人员进行处理。
            
            ## 3 Dify的特点和优势
            ### 3.1 低代码：降低开发门槛
            #### 3.1.1 可视化开发
            Dify采用可视化开发模式，开发者无需编写大量代码，只需通过拖拽组件即可完成AI应用的开发，降低了开发门槛。
            #### 3.1.2 预置组件
            Dify预置了丰富的组件，包括数据源组件、模型组件、流程控制组件等，开发者可以直接使用这些组件，提高开发效率。
            #### 3.1.3 减少代码量
            Dify可以显著减少代码量，降低开发难度，让更多开发者能够参与到AI应用的开发中来。
            
            ### 3.2 灵活：满足不同场景需求
            #### 3.2.1 支持多种数据源
            Dify支持多种数据源，可以连接各种数据源，满足不同场景的数据需求。
            #### 3.2.2 支持自定义模型
            Dify支持自定义模型，开发者可以将自己训练的模型部署到Dify平台上，满足个性化的需求。
            #### 3.2.3 灵活的流程控制
            Dify支持灵活的流程控制，可以根据不同的条件执行不同的分支，实现复杂的业务逻辑。
            
            ### 3.3 高效：加速应用落地
            #### 3.3.1 快速开发
            Dify通过可视化界面和预置组件，大幅缩短开发周期，加速AI应用的落地。
            #### 3.3.2 快速部署
            Dify提供一键部署功能，可以将AI应用快速部署到各种环境，提高部署效率。
            #### 3.3.3 自动化运维
            Dify提供自动化的运维工具，简化运维流程，降低运维成本。
            
            ### 3.4 开放：构建繁荣生态
            #### 3.4.1 开源社区
            Dify拥有活跃的开源社区，开发者可以在社区中交流经验、分享资源、共同推动Dify的发展。
            #### 3.4.2 丰富的文档
            Dify提供丰富的文档、教程和案例，帮助开发者更好地使用Dify平台。
            #### 3.4.3 API支持
            Dify提供API支持，开发者可以通过API将Dify集成到自己的系统中，扩展Dify的功能。
            
            ## 4 Dify的使用场景
            ### 4.1 智能客服：提升客户服务质量
            #### 4.1.1 自动回复
            Dify可以用于构建智能客服系统，实现自动回复客户的常见问题，提高客户服务效率。
            #### 4.1.2 情感分析
            Dify可以对客户的语音或文本进行情感分析，判断客户的情绪，并根据情绪提供个性化的服务。
            #### 4.1.3 知识库问答
            Dify可以构建知识库问答系统，让客户通过提问的方式获取所需的信息，提高客户满意度。
            
            ### 4.2 金融风控：提高风险识别能力
            #### 4.2.1 欺诈检测
            Dify可以用于构建金融风控系统，实现欺诈检测，识别可疑交易，降低风险。
            #### 4.2.2 信用评估
            Dify可以对用户的信用进行评估，并根据评估结果提供不同的金融服务。
            #### 4.2.3 反洗钱
            Dify可以用于反洗钱，识别可疑资金流动，防止犯罪行为。
            
            ### 4.3 智慧医疗：提升医疗服务水平
            #### 4.3.1 疾病诊断
            Dify可以用于辅助疾病诊断，提高诊断准确率，缩短诊断时间。
            #### 4.3.2 药物研发
            Dify可以用于药物研发，加速新药的发现和开发。
            #### 4.3.3 智能健康管理
            Dify可以构建智能健康管理系统，为用户提供个性化的健康建议和服务。
            
            ### 4.4 智慧城市：提升城市管理效率
            #### 4.4.1 交通优化
            Dify可以用于交通优化，提高交通效率，缓解交通拥堵。
            #### 4.4.2 环境监测
            Dify可以用于环境监测，实时监测空气质量、水质等环境指标，及时发现和解决环境问题。
            #### 4.4.3 智能安防
            Dify可以用于智能安防，提高城市安全水平，预防犯罪行为。
            
            ## 5 Dify的成功案例
            ### 5.1 Case 1：某电商平台的智能客服
            #### 5.1.1 项目背景
            该电商平台客户服务压力大，人工客服成本高，需要一种智能化的解决方案。
            #### 5.1.2 解决方案
            使用Dify构建智能客服系统，实现自动回复客户的常见问题，并根据客户的情绪提供个性化的服务。
            #### 5.1.3 效果
            客户服务效率提高50%，客户满意度提高20%，人工客服成本降低30%。
            
            ### 5.2 Case 2：某银行的金融风控系统
            #### 5.2.1 项目背景
            该银行面临日益增长的金融风险，需要一种更有效的风险识别和控制手段。
            #### 5.2.2 解决方案
            使用Dify构建金融风控系统，实现欺诈检测、信用评估和反洗钱等功能，提高风险识别能力。
            #### 5.2.3 效果
            欺诈交易识别率提高40%，信用评估准确率提高30%，洗钱风险降低25%。
            
            ### 5.3 Case 3：某医院的辅助疾病诊断系统
            #### 5.3.1 项目背景
            该医院医生工作压力大，疾病诊断准确率有待提高，需要一种辅助诊断工具。
            #### 5.3.2 解决方案
            使用Dify构建辅助疾病诊断系统，根据患者的病历和症状，提供诊断建议，提高诊断准确率。
            #### 5.3.3 效果
            疾病诊断准确率提高20%，诊断时间缩短15%，医生工作效率提高10%。
            
            ## 6 Dify的未来展望
            ### 6.1 技术升级
            #### 6.1.1 模型优化
            Dify将不断优化预置模型，提高模型性能，并支持更多类型的AI模型。
            #### 6.1.2 流程引擎升级
            Dify将升级流程引擎，提高流程的灵活性和可扩展性，支持更复杂的业务逻辑。
            #### 6.1.3 平台性能优化
            Dify将不断优化平台性能，提高平台的稳定性和可靠性，满足大规模应用的需求。
            
            ### 6.2 生态建设
            #### 6.2.1 社区建设
            Dify将继续加强开源社区建设，吸引更多开发者参与，共同推动Dify的发展。
            #### 6.2.2 合作伙伴拓展
            Dify将拓展合作伙伴，与更多的企业和机构合作，共同推动AI技术的应用。
            #### 6.2.3 应用商店
            Dify将构建应用商店，让开发者可以分享自己的应用，用户可以购买和使用这些应用，构建繁荣的生态系统。
            
            ### 6.3 应用领域拓展
            #### 6.3.1 智能制造
            Dify将拓展到智能制造领域，为企业提供智能化的生产管理和质量控制解决方案。
            #### 6.3.2 智慧农业
            Dify将拓展到智慧农业领域，为农民提供智能化的种植和养殖管理解决方案。
            #### 6.3.3 更多领域
            Dify将拓展到更多领域，为各行各业提供智能化的解决方案，推动社会发展。
            
            ## 7 总结
            ### 7.1 Dify的价值
            #### 7.1.1 降低AI开发门槛
            Dify通过低代码的方式，让更多开发者能够参与到AI应用的开发中来。
            #### 7.1.2 加速AI应用落地
            Dify提供一站式解决方案，加速AI应用的落地和迭代。
            #### 7.1.3 构建繁荣的AI生态
            Dify通过开源社区和应用商店，构建繁荣的AI生态系统。
            
            ### 7.2 共同发展
            #### 7.2.1 欢迎加入Dify社区
            欢迎更多开发者加入Dify社区，共同推动Dify的发展。
            #### 7.2.2 合作共赢
            期待与更多的企业和机构合作，共同推动AI技术的应用。
            #### 7.2.3 共创未来
            让我们一起用AI技术改变世界，共创美好未来。
            """;

}
