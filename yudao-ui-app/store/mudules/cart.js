import { getCartDetail } from '@/api/cart'

const cart = {
  state: {
    cartCount: 0
  },
  mutations: {
    //记录购物车商品数量
    SET_CART_COUNT(state, data) {
      const arr = data.length || []
      state.cartNumber = arr.length
    }
  },
  actions: {
    //获取购物车数据
    CartProductDetail({ state, commit }) {
      return getCartDetail()
        .then(res => {
          commit('SET_CART_COUNT', res.data)
          return Promise.resolve(res)
        })
        .catch(err => {
          return Promise.reject(err)
        })
    }
  }
}
export default cart
