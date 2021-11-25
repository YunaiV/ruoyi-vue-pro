const BASE_URL = 'http://127.0.0.1:28080/api/';
import { msg } from './util'

export const request = (options) => {
	return new Promise((resolve, reject) => {
		// 发起请求
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: {
				'Authorization': '' // TODO 芋艿：带 token
			}
		}).then(res => {
			debugger
			res = res[1];
			const statusCode = res.statusCode;
			if (statusCode !== 200) {
				msg('请求失败，请重试');
				return;
			}
			
			const code = res.data.code;
			const message = res.data.msg;
			// Token 过期，引导重新登陆
			if (code === 401) {
				msg('登录信息已过期，请重新登录');
				store.commit('logout');
				reject('无效的登录信息');
				return;
			}
			// 其它失败情况
			if (code > 0) {
				msg(message);
				reject(message);
				return;
			}
			resolve(res.data.data);
		}).catch((err) => {
			reject(err);
		})
	})
}
