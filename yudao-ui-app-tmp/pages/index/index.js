// index.js

const common=require('../../utils/common.js')
// 获取应用实例
const app = getApp()

Page({
  data: {
    motto: 'Hello World',
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
    canIUseGetUserProfile: false,
    canIUseOpenData: wx.canIUse('open-data.type.userAvatarUrl') && wx.canIUse('open-data.type.userNickName'), // 如需尝试获取用户信息可改为false
    holderText: 'to be auth'
  },
  // 事件处理函数
  bindViewTap() {
    wx.navigateTo({
      url: '../logs/logs'
    })
  },
  onLoad() {
    if (wx.getUserProfile) {
      this.setData({
        canIUseGetUserProfile: true
      })
    }
  },
  getUserProfile(e) {
    // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认，开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
    wx.getUserProfile({
      desc: '展示用户信息', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
      success: (res) => {
        console.log(res)
        this.setData({
          userInfo: res.userInfo,
          hasUserInfo: true
        })
      }
    })
  },
  getUserInfo(e) {
    // 不推荐使用getUserInfo获取用户信息，预计自2021年4月13日起，getUserInfo将不再弹出弹窗，并直接返回匿名的用户个人信息
    console.log(e)
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  },
  // 小程序登录 https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html
  wxLogin(e){
    let page=this;
    wx.login({
      success (res) {
        console.log("res:")
        console.log(res)
        if (res.code) {
          //发起网络请求
          console.log('发起网络请求'+common.baseurl)
          wx.request({
            url: common.baseurl+'/api/social-login2',
            method: "POST",
            data: {
              code: res.code,
              state: 'empty',
              type: 33,
              username: '15601691300',
              password: 'admin123'
            },
            header: {
              'content-type': 'application/json' // 默认值
            },
            success: function(res) {
              console.log(res.data)
              let holder="auth success, token:"+res.data.data.token
              page.setData({holderText: holder})
            },
            fail: function(data){
              console.error("请求出错");
              console.error(data)
            }
            
          })
        } else {
          console.log('登录失败！' + res.errMsg)
        }
      }
    })
  }
})
