import { request } from '@/common/js/request.js'

// 手机号 + 密码登陆
export function login(mobile, password) {
  return request({
    url: 'login',
    method: 'post',
    data: {
		mobile, password
	}
  })
}

// 手机号 + 验证码登陆
export function smsLogin(mobile, code) {
  return request({
    url: 'sms-login',
    method: 'post',
    data: {
		mobile, code
	}
  })
}

// 发送手机验证码
export function sendSmsCode(mobile, scene) {
	return request({
	  url: 'send-sms-code',
	  method: 'post',
	  data: {
			mobile, scene
		}
	})
}