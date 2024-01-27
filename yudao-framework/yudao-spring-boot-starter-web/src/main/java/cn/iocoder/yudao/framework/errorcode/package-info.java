/**
 * 错误码 ErrorCode 的自动配置功能，提供如下功能：
 *
 * 1. 远程读取：项目启动时，从 system-service 服务，读取数据库中的 ErrorCode 错误码，实现错误码的提水可配置；
 * 2. 自动更新：管理员在管理后台修数据库中的 ErrorCode 错误码时，项目自动从 system-service 服务加载最新的 ErrorCode 错误码；
 * 3. 自动写入：项目启动时，将项目本地的错误码写到 system-server 服务中，方便管理员在管理后台编辑；
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.framework.errorcode;
