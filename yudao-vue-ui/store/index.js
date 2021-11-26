import Vue from 'vue'
import Vuex from 'vuex'
// import {request} from '@/common/js/request'
import { getUserInfo } from '@/api/member/userProfile.js'

Vue.use(Vuex)

const store = new Vuex.Store({
	state: {
		openExamine: false, // 是否开启审核状态。用于小程序、App 等审核时，关闭部分功能。TODO 芋艿：暂时没找到刷新的地方
		token: '', // 用户身份 Token
		userInfo: {}, // 用户基本信息
		timerIdent: false, // 全局 1s 定时器，只在全局开启一个，所有需要定时执行的任务监听该值即可，无需额外开启 TODO 芋艿：需要看看
	},
	getters: {
		hasLogin(state){
			return !!state.token;
		}
	},
	mutations: {
		// 更新 state 的通用方法
		setStateAttr(state, param) {
			if (param instanceof Array) {
				for(let item of param){
					state[item.key] = item.val;
				}
			} else {
				state[param.key] = param.val;
			}
		},
		// 更新token
		setToken(state, data) {
			// 设置 Token
			const { token } = data;
			state.token = token;
			uni.setStorageSync('token', token);
			
			// 加载用户信息
			this.dispatch('obtainUserInfo');
		},
		// 退出登录
		logout(state) {
			// 清空 Token
			state.token = '';
			uni.removeStorageSync('token');
			// 清空用户信息 TODO 芋艿：这里 setTimeout 的原因是，上面可能还有一些 request 请求。后续得优化下
			setTimeout(()=>{
				state.userInfo = {};
			}, 1100)
		},
	},
	actions: {
		// 获得用户基本信息
		async obtainUserInfo({state, commit}) {
			const data = await getUserInfo();
			commit('setStateAttr', {
				key: 'userInfo',
				val: data
			});
		}
	}
}) 


export default store
