/**
 * AI 平台的枚举
 */
export const AiPlatformEnum = {
  TONG_YI: 'TongYi', // 阿里
  YI_YAN: 'YiYan', // 百度
  DEEP_SEEK: 'DeepSeek', // DeepSeek
  ZHI_PU: 'ZhiPu', // 智谱 AI
  XING_HUO: 'XingHuo', // 讯飞
  SiliconFlow: 'SiliconFlow', // 硅基流动
  OPENAI: 'OpenAI',
  Ollama: 'Ollama',
  STABLE_DIFFUSION: 'StableDiffusion', // Stability AI
  MIDJOURNEY: 'Midjourney', // Midjourney
  SUNO: 'Suno', // Suno AI
};

export const AiModelTypeEnum = {
  CHAT: 1, // 聊天
  IMAGE: 2, // 图像
  VOICE: 3, // 音频
  VIDEO: 4, // 视频
  EMBEDDING: 5, // 向量
  RERANK: 6, // 重排
};

export interface ImageModel {
  image?: string;
  key: string;
  name: string;
}

export const OtherPlatformEnum: ImageModel[] = [
  {
    key: AiPlatformEnum.TONG_YI,
    name: '通义万相',
  },
  {
    key: AiPlatformEnum.YI_YAN,
    name: '百度千帆',
  },
  {
    key: AiPlatformEnum.ZHI_PU,
    name: '智谱 AI',
  },
  {
    key: AiPlatformEnum.SiliconFlow,
    name: '硅基流动',
  },
];

/**
 * AI 图像生成状态的枚举
 */
export const AiImageStatusEnum = {
  IN_PROGRESS: 10, // 进行中
  SUCCESS: 20, // 已完成
  FAIL: 30, // 已失败
};

/**
 * AI 音乐生成状态的枚举
 */
export const AiMusicStatusEnum = {
  IN_PROGRESS: 10, // 进行中
  SUCCESS: 20, // 已完成
  FAIL: 30, // 已失败
};

/**
 * AI 写作类型的枚举
 */
export enum AiWriteTypeEnum {
  WRITING = 1, // 撰写
  REPLY, // 回复
}

// ========== 【图片 UI】相关的枚举 ==========

export const ImageHotWords = [
  '中国旗袍',
  '古装美女',
  '卡通头像',
  '机甲战士',
  '童话小屋',
  '中国长城',
]; // 图片热词

export const ImageHotEnglishWords = [
  'Chinese Cheongsam',
  'Ancient Beauty',
  'Cartoon Avatar',
  'Mech Warrior',
  'Fairy Tale Cottage',
  'The Great Wall of China',
]; // 图片热词（英文）

export const StableDiffusionSamplers: ImageModel[] = [
  {
    key: 'DDIM',
    name: 'DDIM',
  },
  {
    key: 'DDPM',
    name: 'DDPM',
  },
  {
    key: 'K_DPMPP_2M',
    name: 'K_DPMPP_2M',
  },
  {
    key: 'K_DPMPP_2S_ANCESTRAL',
    name: 'K_DPMPP_2S_ANCESTRAL',
  },
  {
    key: 'K_DPM_2',
    name: 'K_DPM_2',
  },
  {
    key: 'K_DPM_2_ANCESTRAL',
    name: 'K_DPM_2_ANCESTRAL',
  },
  {
    key: 'K_EULER',
    name: 'K_EULER',
  },
  {
    key: 'K_EULER_ANCESTRAL',
    name: 'K_EULER_ANCESTRAL',
  },
  {
    key: 'K_HEUN',
    name: 'K_HEUN',
  },
  {
    key: 'K_LMS',
    name: 'K_LMS',
  },
];

export const StableDiffusionStylePresets: ImageModel[] = [
  {
    key: '3d-model',
    name: '3d-model',
  },
  {
    key: 'analog-film',
    name: 'analog-film',
  },
  {
    key: 'anime',
    name: 'anime',
  },
  {
    key: 'cinematic',
    name: 'cinematic',
  },
  {
    key: 'comic-book',
    name: 'comic-book',
  },
  {
    key: 'digital-art',
    name: 'digital-art',
  },
  {
    key: 'enhance',
    name: 'enhance',
  },
  {
    key: 'fantasy-art',
    name: 'fantasy-art',
  },
  {
    key: 'isometric',
    name: 'isometric',
  },
  {
    key: 'line-art',
    name: 'line-art',
  },
  {
    key: 'low-poly',
    name: 'low-poly',
  },
  {
    key: 'modeling-compound',
    name: 'modeling-compound',
  },
  // neon-punk origami photographic pixel-art tile-texture
  {
    key: 'neon-punk',
    name: 'neon-punk',
  },
  {
    key: 'origami',
    name: 'origami',
  },
  {
    key: 'photographic',
    name: 'photographic',
  },
  {
    key: 'pixel-art',
    name: 'pixel-art',
  },
  {
    key: 'tile-texture',
    name: 'tile-texture',
  },
];

export const StableDiffusionClipGuidancePresets: ImageModel[] = [
  {
    key: 'NONE',
    name: 'NONE',
  },
  {
    key: 'FAST_BLUE',
    name: 'FAST_BLUE',
  },
  {
    key: 'FAST_GREEN',
    name: 'FAST_GREEN',
  },
  {
    key: 'SIMPLE',
    name: 'SIMPLE',
  },
  {
    key: 'SLOW',
    name: 'SLOW',
  },
  {
    key: 'SLOWER',
    name: 'SLOWER',
  },
  {
    key: 'SLOWEST',
    name: 'SLOWEST',
  },
];

export const Dall3Models: ImageModel[] = [
  {
    key: 'dall-e-3',
    name: 'DALL·E 3',
    image: `/static/imgs/ai/dall2.jpg`,
  },
  {
    key: 'dall-e-2',
    name: 'DALL·E 2',
    image: `/static/imgs/ai/dall3.jpg`,
  },
];

export const Dall3StyleList: ImageModel[] = [
  {
    key: 'vivid',
    name: '清晰',
    image: `/static/imgs/ai/qingxi.jpg`,
  },
  {
    key: 'natural',
    name: '自然',
    image: `/static/imgs/ai/ziran.jpg`,
  },
];
export const MidjourneyModels: ImageModel[] = [
  {
    key: 'midjourney',
    name: 'MJ',
    image: 'https://bigpt8.com/pc/_nuxt/mj.34a61377.png',
  },
  {
    key: 'niji',
    name: 'NIJI',
    image: 'https://bigpt8.com/pc/_nuxt/nj.ca79b143.png',
  },
];

export const MidjourneyVersions = [
  {
    value: '6.0',
    label: 'v6.0',
  },
  {
    value: '5.2',
    label: 'v5.2',
  },
  {
    value: '5.1',
    label: 'v5.1',
  },
  {
    value: '5.0',
    label: 'v5.0',
  },
  {
    value: '4.0',
    label: 'v4.0',
  },
];

export const NijiVersionList = [
  {
    value: '5',
    label: 'v5',
  },
];

export const MidjourneySizeList: ImageSize[] = [
  {
    key: '1:1',
    width: '1',
    height: '1',
    style: 'width: 30px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '3:4',
    width: '3',
    height: '4',
    style: 'width: 30px; height: 40px;background-color: #dcdcdc;',
  },
  {
    key: '4:3',
    width: '4',
    height: '3',
    style: 'width: 40px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '9:16',
    width: '9',
    height: '16',
    style: 'width: 30px; height: 50px;background-color: #dcdcdc;',
  },
  {
    key: '16:9',
    width: '16',
    height: '9',
    style: 'width: 50px; height: 30px;background-color: #dcdcdc;',
  },
];

export interface ImageSize {
  height: string;
  key: string;
  name?: string;
  style: string;
  width: string;
}
export const Dall3SizeList: ImageSize[] = [
  {
    key: '1024x1024',
    name: '1:1',
    width: '1024',
    height: '1024',
    style: 'width: 30px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '1024x1792',
    name: '3:5',
    width: '1024',
    height: '1792',
    style: 'width: 30px; height: 50px;background-color: #dcdcdc;',
  },
  {
    key: '1792x1024',
    name: '5:3',
    width: '1792',
    height: '1024',
    style: 'width: 50px; height: 30px;background-color: #dcdcdc;',
  },
];

// ========== 【写作 UI】相关的枚举 ==========

/** 写作点击示例时的数据 */
export const WriteExample = {
  write: {
    prompt: 'vue',
    data: 'Vue.js 是一种用于构建用户界面的渐进式 JavaScript 框架。它的核心库只关注视图层，易于上手，同时也便于与其他库或已有项目整合。\n\nVue.js 的特点包括：\n- 响应式的数据绑定：Vue.js 会自动将数据与 DOM 同步，使得状态管理变得更加简单。\n- 组件化：Vue.js 允许开发者通过小型、独立和通常可复用的组件构建大型应用。\n- 虚拟 DOM：Vue.js 使用虚拟 DOM 实现快速渲染，提高了性能。\n\n在 Vue.js 中，一个典型的应用结构可能包括：\n1. 根实例：每个 Vue 应用都需要一个根实例作为入口点。\n2. 组件系统：可以创建自定义的可复用组件。\n3. 指令：特殊的带有前缀 v- 的属性，为 DOM 元素提供特殊的行为。\n4. 插值：用于文本内容，将数据动态地插入到 HTML。\n5. 计算属性和侦听器：用于处理数据的复杂逻辑和响应数据变化。\n6. 条件渲染：根据条件决定元素的渲染。\n7. 列表渲染：用于显示列表数据。\n8. 事件处理：响应用户交互。\n9. 表单输入绑定：处理表单输入和验证。\n10. 组件生命周期钩子：在组件的不同阶段执行特定的函数。\n\nVue.js 还提供了官方的路由器 Vue Router 和状态管理库 Vuex，以支持构建复杂的单页应用（SPA）。\n\n在开发过程中，开发者通常会使用 Vue CLI，这是一个强大的命令行工具，用于快速生成 Vue 项目脚手架，集成了诸如 Babel、Webpack 等现代前端工具，以及热重载、代码检测等开发体验优化功能。\n\nVue.js 的生态系统还包括大量的第三方库和插件，如 Vuetify（UI 组件库）、Vue Test Utils（测试工具）等，这些都极大地丰富了 Vue.js 的开发生态。\n\n总的来说，Vue.js 是一个灵活、高效的前端框架，适合从小型项目到大型企业级应用的开发。它的易用性、灵活性和强大的社区支持使其成为许多开发者的首选框架之一。',
  },
  reply: {
    originalContent: '领导，我想请假',
    prompt: '不批',
    data: '您的请假申请已收悉，经核实和考虑，暂时无法批准您的请假申请。\n\n如有特殊情况或紧急事务，请及时与我联系。\n\n祝工作顺利。\n\n谢谢。',
  },
};

// ========== 【思维导图 UI】相关的枚举 ==========

/** 思维导图已有内容生成示例 */
export const MindMapContentExample = `# Java 技术栈

## 核心技术
### Java SE
### Java EE

## 框架
### Spring
#### Spring Boot
#### Spring MVC
#### Spring Data
### Hibernate
### MyBatis

## 构建工具
### Maven
### Gradle

## 版本控制
### Git
### SVN

## 测试工具
### JUnit
### Mockito
### Selenium

## 应用服务器
### Tomcat
### Jetty
### WildFly

## 数据库
### MySQL
### PostgreSQL
### Oracle
### MongoDB

## 消息队列
### Kafka
### RabbitMQ
### ActiveMQ

## 微服务
### Spring Cloud
### Dubbo

## 容器化
### Docker
### Kubernetes

## 云服务
### AWS
### Azure
### Google Cloud

## 开发工具
### IntelliJ IDEA
### Eclipse
### Visual Studio Code`;
