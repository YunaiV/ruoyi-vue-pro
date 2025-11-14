import type { SystemOperateLogApi } from '#/api/system/operate-log';

export interface OperateLogProps {
  logList: SystemOperateLogApi.OperateLog[]; // 操作日志列表
}
