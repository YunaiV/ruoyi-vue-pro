import { requestClient } from '#/api/request';

export namespace MemberSignInConfigApi {
  /** 积分签到规则信息 */
  export interface SignInConfig {
    id?: number;
    day?: number;
    point?: number;
    experience?: number;
    status?: number;
  }
}

/** 查询积分签到规则列表 */
export function getSignInConfigList() {
  return requestClient.get<MemberSignInConfigApi.SignInConfig[]>(
    '/member/sign-in/config/list',
  );
}

/** 查询积分签到规则详情 */
export function getSignInConfig(id: number) {
  return requestClient.get<MemberSignInConfigApi.SignInConfig>(
    `/member/sign-in/config/get?id=${id}`,
  );
}

/** 新增积分签到规则 */
export function createSignInConfig(data: MemberSignInConfigApi.SignInConfig) {
  return requestClient.post('/member/sign-in/config/create', data);
}

/** 修改积分签到规则 */
export function updateSignInConfig(data: MemberSignInConfigApi.SignInConfig) {
  return requestClient.put('/member/sign-in/config/update', data);
}

/** 删除积分签到规则 */
export function deleteSignInConfig(id: number) {
  return requestClient.delete(`/member/sign-in/config/delete?id=${id}`);
}
