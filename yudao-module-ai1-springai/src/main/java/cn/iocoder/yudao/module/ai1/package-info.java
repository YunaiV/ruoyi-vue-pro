/**
 * ai1 模块，参考 XXL-AI 优化实现的 AI Agent 能力。
 * 例如说：Agent 编排、多供应商模型、MCP、SKILL、RAG 知识库、流式对话
 *
 * 1. Controller URL：以 /ai1/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 ai1_ 开头，方便在数据库中区分
 *
 * 注意，由于 Ai1 模块下容易和其它模块重名，所以类名都加 Ai1 前缀
 */
package cn.iocoder.yudao.module.ai1;
