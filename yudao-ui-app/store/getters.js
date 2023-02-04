const getters = {
  accessToken: state => state.user.accessToken,
  refreshToken: state => state.user.refreshToken,
  userInfo: state => state.user.userInfo,
  hasLogin: state => !!state.user.accessToken
}
export default getters
