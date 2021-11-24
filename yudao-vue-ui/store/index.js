import Vue from 'vue'
import Vuex from 'vuex'
// import {request} from '@/common/js/request'

Vue.use(Vuex)

const store = new Vuex.Store({
	state: {
		openExamine: false, // 是否开启审核状态。用于小程序、App 等审核时，关闭部分功能。TODO 芋艿：暂时没找到刷新的地方
		token: '', // 用户身份 Token
		userInfo: {}, // 用户基本信息
		timerIdent: false, // 全局 1s 定时器，只在全局开启一个，所有需要定时执行的任务监听该值即可，无需额外开启 TODO 芋艿：需要看看
		orderCount: {}, // 订单数量
	},
	getters: {
		hasLogin(state){
			return !!state.token;
		}
	},
	mutations: {
		//更新state数据
		setStateAttr(state, param){
			if(param instanceof Array){
				for(let item of param){
					state[item.key] = item.val;
				}
			}else{
				state[param.key] = param.val;
			}
		},
		//更新token
		setToken(state, data){
			const {token, tokenExpired} = data;
			state.token = token;
			uni.setStorageSync('uniIdToken', token);
			uni.setStorageSync('tokenExpired', tokenExpired);
			this.dispatch('getUserInfo'); //更新用户信息
			this.dispatch('getCartCount');//更新购物车数量
			uni.$emit('refreshCart');//刷新购物车
			this.dispatch('getOrderCount'); //更新订单数量
		},
		// 退出登录
		logout(state) {
			state.token = '';
			uni.removeStorageSync('uniIdToken');
			this.dispatch('getCartCount');//更新购物车数量
			uni.$emit('refreshCart');//刷新购物车
			this.dispatch('getOrderCount'); //更新订单数量
			setTimeout(()=>{
				state.userInfo = {};
			}, 1100)
		},
	},
	actions: {
		//更新用户信息
		async getUserInfo({state, commit}){
			const res = await request('user', 'get', {}, {
				checkAuthInvalid: false
			});
			if(res.status === 1){
				const userInfo = res.data;
				commit('setStateAttr', {
					key: 'userInfo',
					val: userInfo
				})
			}
		},
		//更新用户订单数量
		async getOrderCount({state, commit}){
			let data = {
				c0: 0,
				c1: 0,
				c2: 0,
				c3: 0
			}
			if(state.token){
				try {
					const res = await request('order', 'getOrderCount');
					data = res;
				}catch (err){
					console.error('更新用户订单数量 => ', err);
				}
			}
			commit('setStateAttr', {
				key: 'orderCount',
				val: data
			})
		}
	}
}) 


export default store
