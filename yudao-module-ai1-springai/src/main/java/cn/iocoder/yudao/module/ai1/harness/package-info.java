/**
 * Agent 运行时支撑：
 *
 * 这里是平台去调用大模型、MCP 服务、知识库和 SKILL 的客户端与工厂，
 * 不是注册给模型调用的 function tool。模型可调用的工具，在对话时由 MCP ToolCallback 和 SKILL 另行装配。
 */
package cn.iocoder.yudao.module.ai1.harness;
