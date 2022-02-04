import store from '@/store'
import { msg, getAuthToken } from './util'

const BASE_URL = 'http://127.0.0.1:28080/api/';

export const request = (options) => {
	return new Promise((resolve, reject) => {
		// 发起请求
		const authToken = getAuthToken();
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: {
				...options.header,
				'Authorization': authToken ? `Bearer ${authToken}` : ''
			}
		}).then(res => {
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
				// reject('无效的登录信息');
				return;
			}
			// 系统异常
			if (code === 500) {
				msg('系统异常，请稍后重试');
				reject(new Error(message));
				return;
			}
			// 其它失败情况
			if (code > 0) {
				msg(message);
				// 提供 code + msg，可以基于 code 做进一步的处理。当然，一般情况下是不需要的。
				// 不需要的场景：手机登录时，密码不正确；
				// 需要的场景：微信登录时，未绑定手机，后端会返回一个 code 码，前端需要基于它跳转到绑定手机界面；
				reject({
					'code': code,
					'msg': message
				});
				return;
			}
			// 处理成功，则只返回成功的 data 数据，不返回 code 和 msg
			resolve(res.data.data);
		}).catch((err) => {
			reject(err);
		})
	})
}
