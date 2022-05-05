import request from "@/utils/request";

// 社交绑定，使用 code 授权码
export function socialBind(type, code, state) {
  return request({
    url: '/system/social-user/bind',
    method: 'post',
    data: {
      type,
      code,
      state,
    }
  })
}

// 取消社交绑定
export function socialUnbind(type, openid) {
  return request({
    url: '/system/social-user/unbind',
    method: 'delete',
    data: {
      type,
      openid
    }
  })
}
