import { request } from '@/common/js/request.js'

// 获得用户的基本信息
export function getUserInfo() {
  return request({
    url: 'member/user/profile/get',
    method: 'get'
  })
}

// 修改
export function updateNickname(nickname) {
	return request({
		url: 'member/user/profile/update-nickname',
		method: 'post',
		header: {
			"Content-Type": "application/x-www-form-urlencoded"
		},
		data: {
			nickname
		}
	})
}