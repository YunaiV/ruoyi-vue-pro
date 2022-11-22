const getters = {
  accessToken: state => state.user.accessToken,
  userInfo: state => state.user.userInfo,
  hasLogin: state => !!state.user.accessToken
}
export default getters
