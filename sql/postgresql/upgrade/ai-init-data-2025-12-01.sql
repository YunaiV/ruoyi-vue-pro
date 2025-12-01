/*
 * 芋道 AI 模块 - PostgreSQL 初始化数据脚本
 *
 * Project: ruoyi-vue-pro
 * Module: yudao-module-ai
 * Database: PostgreSQL 12+
 * Version: 2025.12
 * Date: 2025-12-01
 *
 * 说明:
 * 本脚本提供 AI 模块的初始化数据，包含常用的 API 密钥配置、模型定义和聊天角色
 * 注意：API 密钥字段需要用户自行填入真实的密钥值
 *
 * 依赖枚举:
 * - AiPlatformEnum: TongYi, YiYan, DeepSeek, ZhiPu, XingHuo, DouBao, HunYuan,
 *                   SiliconFlow, MiniMax, Moonshot, BaiChuan, OpenAI, AzureOpenAI,
 *                   Anthropic, Gemini, Ollama, StableDiffusion, Midjourney, Suno, Grok
 * - AiModelTypeEnum: 1=对话, 2=图片, 3=语音, 4=视频, 5=向量, 6=重排序
 * - CommonStatusEnum: 0=开启, 1=关闭
 */

-- ----------------------------
-- AI API 密钥初始化数据
-- 注意：api_key 字段为占位符，需要用户自行替换为真实密钥
-- ----------------------------
DELETE FROM ai_api_key WHERE id BETWEEN 1 AND 20;

INSERT INTO ai_api_key (id, name, api_key, platform, url, status, creator, create_time, updater, update_time, deleted) VALUES
-- 国内平台
(1, '通义千问', 'sk-your-tongyi-api-key', 'TongYi', 'https://dashscope.aliyuncs.com', 1, '1', NOW(), '1', NOW(), false),
(2, '文心一言', 'your-yiyan-api-key', 'YiYan', 'https://aip.baidubce.com', 1, '1', NOW(), '1', NOW(), false),
(3, 'DeepSeek', 'sk-your-deepseek-api-key', 'DeepSeek', 'https://api.deepseek.com', 1, '1', NOW(), '1', NOW(), false),
(4, '智谱 AI', 'your-zhipu-api-key', 'ZhiPu', 'https://open.bigmodel.cn', 1, '1', NOW(), '1', NOW(), false),
(5, '讯飞星火', 'your-xinghuo-api-key', 'XingHuo', 'https://spark-api.xf-yun.com', 1, '1', NOW(), '1', NOW(), false),
(6, '字节豆包', 'your-doubao-api-key', 'DouBao', 'https://ark.cn-beijing.volces.com', 1, '1', NOW(), '1', NOW(), false),
(7, '腾讯混元', 'your-hunyuan-api-key', 'HunYuan', 'https://hunyuan.tencentcloudapi.com', 1, '1', NOW(), '1', NOW(), false),
(8, '硅基流动', 'sk-your-siliconflow-api-key', 'SiliconFlow', 'https://api.siliconflow.cn', 1, '1', NOW(), '1', NOW(), false),
(9, 'MiniMax', 'your-minimax-api-key', 'MiniMax', 'https://api.minimax.chat', 1, '1', NOW(), '1', NOW(), false),
(10, '月之暗面', 'sk-your-moonshot-api-key', 'Moonshot', 'https://api.moonshot.cn', 1, '1', NOW(), '1', NOW(), false),
(11, '百川智能', 'sk-your-baichuan-api-key', 'BaiChuan', 'https://api.baichuan-ai.com', 1, '1', NOW(), '1', NOW(), false),
-- 国外平台
(12, 'OpenAI', 'sk-your-openai-api-key', 'OpenAI', 'https://api.openai.com', 1, '1', NOW(), '1', NOW(), false),
(13, 'Azure OpenAI', 'your-azure-openai-api-key', 'AzureOpenAI', 'https://your-resource.openai.azure.com', 1, '1', NOW(), '1', NOW(), false),
(14, 'Anthropic', 'sk-ant-your-anthropic-api-key', 'Anthropic', 'https://api.anthropic.com', 1, '1', NOW(), '1', NOW(), false),
(15, 'Google Gemini', 'your-gemini-api-key', 'Gemini', 'https://generativelanguage.googleapis.com', 1, '1', NOW(), '1', NOW(), false),
(16, 'Ollama 本地', '', 'Ollama', 'http://localhost:11434', 1, '1', NOW(), '1', NOW(), false),
(17, 'Stable Diffusion', 'your-sd-api-key', 'StableDiffusion', 'https://api.stability.ai', 1, '1', NOW(), '1', NOW(), false),
(18, 'Midjourney', 'your-mj-api-key', 'Midjourney', 'https://api.midjourney.com', 1, '1', NOW(), '1', NOW(), false),
(19, 'Suno AI', 'your-suno-api-key', 'Suno', 'https://api.suno.ai', 1, '1', NOW(), '1', NOW(), false),
(20, 'Grok', 'your-grok-api-key', 'Grok', 'https://api.x.ai', 1, '1', NOW(), '1', NOW(), false);

-- 更新序列
SELECT setval('ai_api_key_seq', 20);

-- ----------------------------
-- AI 模型初始化数据
-- type: 1=对话, 2=图片, 3=语音, 4=视频, 5=向量, 6=重排序
-- status: 0=开启, 1=关闭
-- ----------------------------
DELETE FROM ai_model WHERE id BETWEEN 1 AND 50;

INSERT INTO ai_model (id, key_id, name, model, platform, type, sort, status, temperature, max_tokens, max_contexts, creator, create_time, updater, update_time, deleted) VALUES
-- DeepSeek 模型（推荐，性价比高）
(1, 3, 'DeepSeek V3', 'deepseek-chat', 'DeepSeek', 1, 1, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(2, 3, 'DeepSeek Coder', 'deepseek-coder', 'DeepSeek', 1, 2, 0, 0.3, 4096, 10, '1', NOW(), '1', NOW(), false),
(3, 3, 'DeepSeek Reasoner', 'deepseek-reasoner', 'DeepSeek', 1, 3, 0, 0.7, 8192, 10, '1', NOW(), '1', NOW(), false),
-- 通义千问模型
(4, 1, '通义千问 Max', 'qwen-max', 'TongYi', 1, 10, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(5, 1, '通义千问 Plus', 'qwen-plus', 'TongYi', 1, 11, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(6, 1, '通义千问 Turbo', 'qwen-turbo', 'TongYi', 1, 12, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(7, 1, '通义千问 VL Max', 'qwen-vl-max', 'TongYi', 2, 13, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- 智谱 AI 模型
(8, 4, 'GLM-4 Plus', 'glm-4-plus', 'ZhiPu', 1, 20, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(9, 4, 'GLM-4', 'glm-4', 'ZhiPu', 1, 21, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(10, 4, 'GLM-4V', 'glm-4v', 'ZhiPu', 2, 22, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- 月之暗面模型
(11, 10, 'Moonshot V1 128K', 'moonshot-v1-128k', 'Moonshot', 1, 30, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(12, 10, 'Moonshot V1 32K', 'moonshot-v1-32k', 'Moonshot', 1, 31, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- 硅基流动模型（可使用多种开源模型）
(13, 8, 'Qwen2.5-72B-Instruct', 'Qwen/Qwen2.5-72B-Instruct', 'SiliconFlow', 1, 40, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(14, 8, 'DeepSeek-V2.5', 'deepseek-ai/DeepSeek-V2.5', 'SiliconFlow', 1, 41, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- 字节豆包模型
(15, 6, '豆包 Pro 128K', 'doubao-pro-128k', 'DouBao', 1, 50, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(16, 6, '豆包 Pro 32K', 'doubao-pro-32k', 'DouBao', 1, 51, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- OpenAI 模型
(17, 12, 'GPT-4o', 'gpt-4o', 'OpenAI', 1, 60, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(18, 12, 'GPT-4o Mini', 'gpt-4o-mini', 'OpenAI', 1, 61, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(19, 12, 'GPT-4 Turbo', 'gpt-4-turbo', 'OpenAI', 1, 62, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(20, 12, 'GPT-3.5 Turbo', 'gpt-3.5-turbo', 'OpenAI', 1, 63, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(21, 12, 'DALL-E 3', 'dall-e-3', 'OpenAI', 2, 64, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
(22, 12, 'Text Embedding 3 Large', 'text-embedding-3-large', 'OpenAI', 5, 65, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
-- Anthropic Claude 模型
(23, 14, 'Claude 3.5 Sonnet', 'claude-3-5-sonnet-20241022', 'Anthropic', 1, 70, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(24, 14, 'Claude 3.5 Haiku', 'claude-3-5-haiku-20241022', 'Anthropic', 1, 71, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(25, 14, 'Claude 3 Opus', 'claude-3-opus-20240229', 'Anthropic', 1, 72, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- Google Gemini 模型
(26, 15, 'Gemini 1.5 Pro', 'gemini-1.5-pro', 'Gemini', 1, 80, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(27, 15, 'Gemini 1.5 Flash', 'gemini-1.5-flash', 'Gemini', 1, 81, 0, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
-- Ollama 本地模型（需要本地部署）
(28, 16, 'Llama 3.1 8B', 'llama3.1:8b', 'Ollama', 1, 90, 1, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(29, 16, 'Qwen2.5 7B', 'qwen2.5:7b', 'Ollama', 1, 91, 1, 0.7, 4096, 10, '1', NOW(), '1', NOW(), false),
(30, 16, 'DeepSeek Coder V2', 'deepseek-coder-v2:16b', 'Ollama', 1, 92, 1, 0.3, 4096, 10, '1', NOW(), '1', NOW(), false),
-- Stable Diffusion 图像模型
(31, 17, 'SDXL 1.0', 'stable-diffusion-xl-1024-v1-0', 'StableDiffusion', 2, 100, 1, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
(32, 17, 'SD 3.0', 'sd3', 'StableDiffusion', 2, 101, 1, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
-- Midjourney 图像模型
(33, 18, 'Midjourney V6', 'mj-v6', 'Midjourney', 2, 110, 1, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
-- Suno 音乐模型
(34, 19, 'Suno V3.5', 'suno-v3.5', 'Suno', 3, 120, 1, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
-- 向量模型
(35, 1, '通义 Embedding V3', 'text-embedding-v3', 'TongYi', 5, 130, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
(36, 4, 'Embedding-3', 'embedding-3', 'ZhiPu', 5, 131, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
-- 重排序模型
(37, 4, 'Reranker', 'reranker', 'ZhiPu', 6, 140, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false),
(38, 8, 'BGE Reranker V2 M3', 'BAAI/bge-reranker-v2-m3', 'SiliconFlow', 6, 141, 0, NULL, NULL, NULL, '1', NOW(), '1', NOW(), false);

-- 更新序列
SELECT setval('ai_model_seq', 50);

-- ----------------------------
-- AI 聊天角色初始化数据
-- public_status: true=公开, false=私有
-- status: 0=开启, 1=关闭
-- ----------------------------
DELETE FROM ai_chat_role WHERE id BETWEEN 1 AND 20;

INSERT INTO ai_chat_role (id, name, avatar, category, description, system_message, user_id, model_id, knowledge_ids, tool_ids, mcp_client_names, public_status, sort, status, creator, create_time, updater, update_time, deleted) VALUES
-- 通用助手
(1, 'AI 助手', 'https://static.iocoder.cn/avatar/ai-assistant.png', '通用', '通用 AI 助手，可以帮助您解答各种问题', '你是一个专业的 AI 助手，能够帮助用户解答各种问题。请用简洁、准确、有帮助的方式回答用户的问题。', NULL, 1, NULL, NULL, NULL, true, 1, 0, '1', NOW(), '1', NOW(), false),
-- 写作助手（内置角色）
(2, '写作助手', 'https://static.iocoder.cn/avatar/ai-writer.png', '创作', '专业写作助手，帮助生成创意内容', E'你是一位出色的写作助手，能够帮助用户生成创意和灵感，并在用户提供场景和提示词时生成对应的回复。你的任务包括：\n1. 撰写建议：根据用户提供的主题或问题，提供详细的写作建议、情节发展方向、角色设定以及背景描写，确保内容结构清晰、有逻辑。\n2. 回复生成：根据用户提供的场景和提示词，生成合适的对话或文字回复，确保语气和风格符合场景需求。\n除此之外不需要除了正文内容外的其他回复，如标题、开头、任何解释性语句或道歉。', NULL, 1, NULL, NULL, NULL, true, 2, 0, '1', NOW(), '1', NOW(), false),
-- 思维导图助手（内置角色）
(3, '导图助手', 'https://static.iocoder.cn/avatar/ai-mindmap.png', '效率', '思维导图生成助手', E'你是一位非常优秀的思维导图助手，你会把用户的所有提问都总结成思维导图，然后以 Markdown 格式输出。markdown 只需要输出一级标题，二级标题，三级标题，四级标题，最多输出四级，除此之外不要输出任何其他 markdown 标记。下面是一个合格的例子：\n# Geek-AI 助手\n## 完整的开源系统\n### 前端开源\n### 后端开源\n## 支持各种大模型\n### OpenAI\n### Azure\n### 文心一言\n### 通义千问\n## 集成多种收费方式\n### 支付宝\n### 微信\n除此之外不要任何解释性语句。', NULL, 1, NULL, NULL, NULL, true, 3, 0, '1', NOW(), '1', NOW(), false),
-- 代码助手
(4, '代码助手', 'https://static.iocoder.cn/avatar/ai-coder.png', '开发', '专业编程助手，支持多种编程语言', E'你是一位经验丰富的软件工程师和编程导师。你的任务是：\n1. 帮助用户解决编程问题，提供清晰的代码示例\n2. 解释代码的工作原理，使用简单易懂的语言\n3. 提供最佳实践建议和代码优化方案\n4. 支持 Java、Python、JavaScript、Go、Rust 等多种编程语言\n5. 代码要有适当的注释，便于理解\n请用代码块格式化代码输出，并指明使用的编程语言。', NULL, 2, NULL, NULL, NULL, true, 4, 0, '1', NOW(), '1', NOW(), false),
-- 翻译助手
(5, '翻译助手', 'https://static.iocoder.cn/avatar/ai-translator.png', '语言', '多语言翻译专家', E'你是一位专业的多语言翻译专家。你的任务是：\n1. 准确翻译用户提供的文本，保持原文的含义和语气\n2. 支持中英文、日文、韩文、法文、德文等多种语言互译\n3. 对于专业术语，提供准确的翻译并在必要时给出解释\n4. 如果原文有歧义，指出并提供多种可能的翻译\n5. 保持译文的流畅性和自然性\n默认将非中文内容翻译为中文，将中文内容翻译为英文。用户可以指定目标语言。', NULL, 1, NULL, NULL, NULL, true, 5, 0, '1', NOW(), '1', NOW(), false),
-- 数据分析师
(6, '数据分析师', 'https://static.iocoder.cn/avatar/ai-analyst.png', '分析', '数据分析与可视化专家', E'你是一位专业的数据分析师。你的任务是：\n1. 帮助用户分析数据，识别数据中的模式和趋势\n2. 提供数据可视化建议，选择合适的图表类型\n3. 编写数据分析相关的代码（Python、SQL 等）\n4. 解释统计概念和分析方法\n5. 提供数据清洗和预处理建议\n请使用清晰的结构展示分析结果，并提供可执行的代码示例。', NULL, 1, NULL, NULL, NULL, true, 6, 0, '1', NOW(), '1', NOW(), false),
-- 文案策划
(7, '文案策划', 'https://static.iocoder.cn/avatar/ai-copywriter.png', '营销', '营销文案创作专家', E'你是一位创意文案策划专家。你的任务是：\n1. 根据产品特点和目标受众，创作吸引人的营销文案\n2. 提供多种文案风格选择：专业、幽默、情感、简约等\n3. 撰写社交媒体文案、广告语、产品描述等\n4. 优化文案的 SEO 效果\n5. 提供 A/B 测试建议\n请根据用户需求，提供多个文案版本供选择。', NULL, 1, NULL, NULL, NULL, true, 7, 0, '1', NOW(), '1', NOW(), false),
-- 英语老师
(8, '英语老师', 'https://static.iocoder.cn/avatar/ai-english-teacher.png', '教育', '专业英语教学助手', E'你是一位经验丰富的英语老师。你的任务是：\n1. 帮助学生提高英语听说读写能力\n2. 解释语法规则，提供清晰的例句\n3. 纠正语法和用词错误，并解释原因\n4. 提供词汇学习建议和记忆技巧\n5. 根据学生水平调整教学内容\n6. 鼓励学生练习，给予积极反馈\n请用简单易懂的方式解释，必要时提供中文翻译。', NULL, 1, NULL, NULL, NULL, true, 8, 0, '1', NOW(), '1', NOW(), false),
-- 法律顾问
(9, '法律顾问', 'https://static.iocoder.cn/avatar/ai-lawyer.png', '专业', '法律咨询助手（仅供参考）', E'你是一位法律咨询助手。你的任务是：\n1. 提供法律知识科普和解答\n2. 解释法律条文和案例\n3. 提供法律文书写作建议\n4. 分析法律问题并给出建议方向\n\n重要声明：\n- 本助手提供的信息仅供参考，不构成法律意见\n- 具体法律问题请咨询专业律师\n- 请注意法律法规可能随时间更新\n- 不同地区法律规定可能有所不同', NULL, 1, NULL, NULL, NULL, true, 9, 0, '1', NOW(), '1', NOW(), false),
-- 产品经理
(10, '产品经理', 'https://static.iocoder.cn/avatar/ai-pm.png', '产品', '产品设计与规划专家', E'你是一位经验丰富的产品经理。你的任务是：\n1. 帮助分析用户需求，提炼核心功能\n2. 撰写产品需求文档（PRD）\n3. 设计用户故事和用例\n4. 提供产品优化建议\n5. 分析竞品，提供差异化建议\n6. 制定产品路线图\n请用结构化的方式输出，使用标准的产品文档格式。', NULL, 1, NULL, NULL, NULL, true, 10, 0, '1', NOW(), '1', NOW(), false);

-- 更新序列
SELECT setval('ai_chat_role_seq', 20);

SELECT 'AI 模块 PostgreSQL 初始化数据导入完成！' AS message;
