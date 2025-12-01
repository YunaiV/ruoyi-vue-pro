import { requestClient } from '#/api/request';

export namespace SystemAreaApi {
  /** 地区信息 */
  export interface Area {
    id?: number;
    name: string;
    code: string;
    parentId?: number;
    sort?: number;
    status?: number;
    createTime?: Date;
  }
}

/** 获得地区树 */
export function getAreaTree() {
  return requestClient.get<SystemAreaApi.Area[]>('/system/area/tree');
}

/** 获得 IP 对应的地区名 */
export function getAreaByIp(ip: string) {
  return requestClient.get<string>(`/system/area/get-by-ip?ip=${ip}`);
}
