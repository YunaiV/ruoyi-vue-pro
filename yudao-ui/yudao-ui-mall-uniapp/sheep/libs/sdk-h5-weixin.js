/**
 * 本模块封装微信浏览器下的一些方法。
 * 更多微信网页开发sdk方法,详见:https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html
 * 有 the permission value is offline verifying 报错请参考 @see https://segmentfault.com/a/1190000042289419 解决
 */

import jweixin from 'weixin-js-sdk';
import $helper from '@/sheep/helper';
import AuthUtil from '@/sheep/api/member/auth';

let configSuccess = false;

export default {
  // 判断是否在微信中
  isWechat() {
    const ua = window.navigator.userAgent.toLowerCase();
    // noinspection EqualityComparisonWithCoercionJS
    return ua.match(/micromessenger/i) == 'micromessenger';
  },

  isReady(api) {
    jweixin.ready(api);
  },

  // 初始化 JSSDK
  async init(callback) {
    if (!this.isWechat()) {
      $helper.toast('请使用微信网页浏览器打开');
      return;
    }

    // 调用后端接口，获得 JSSDK 初始化所需的签名
    const url = location.origin;
    const { code, data } = await AuthUtil.createWeixinMpJsapiSignature(url);
    if (code === 0) {
      jweixin.config({
        debug: false,
        appId: data.appId,
        timestamp: data.timestamp,
        nonceStr: data.nonceStr,
        signature: data.signature,
        jsApiList: [
          'chooseWXPay',
          'openLocation',
          'getLocation',
          'updateAppMessageShareData',
          'updateTimelineShareData',
          'scanQRCode',
        ], // TODO 芋艿：后续可以设置更多权限；
        openTagList: data.openTagList,
      });
    } else {
      console.log('请求 JSSDK 配置失败，错误码：', code);
    }

    // 监听结果
    configSuccess = true;
    jweixin.error((err) => {
      configSuccess = false;
      console.error('微信 JSSDK 初始化失败', err);
      $helper.toast('微信JSSDK:' + err.errMsg);
    });
    jweixin.ready(() => {
      if (configSuccess) {
        console.log('微信 JSSDK 初始化成功');
      }
    });

    // 回调
    if (callback) {
      callback(data);
    }
  },

  //在需要定位页面调用 TODO 芋艿：未测试
  getLocation(callback) {
    this.isReady(() => {
      jweixin.getLocation({
        type: 'gcj02', // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
        success: function (res) {
          callback(res);
        },
        fail: function (res) {
          console.log('%c微信H5sdk,getLocation失败：', 'color:green;background:yellow');
        },
      });
    });
  },

  // 获取微信收货地址
  openAddress(callback) {
    this.isReady(() => {
      jweixin.openAddress({
        success: function (res) {
          callback.success && callback.success(res);
        },
        fail: function (err) {
          callback.error && callback.error(err);
          console.log('%c微信H5sdk,openAddress失败：', 'color:green;background:yellow');
        },
        complete: function (res) {},
      });
    });
  },

  // 微信扫码 TODO 芋艿：未测试
  scanQRCode(callback) {
    this.isReady(() => {
      jweixin.scanQRCode({
        needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
        scanType: ['qrCode', 'barCode'], // 可以指定扫二维码还是一维码，默认二者都有
        success: function (res) {
          callback(res);
        },
        fail: function (res) {
          console.log('%c微信H5sdk,scanQRCode失败：', 'color:green;background:yellow');
        },
      });
    });
  },

  // 更新微信分享信息
  updateShareInfo(data, callback = null) {
    this.isReady(() => {
      const shareData = {
        title: data.title,
        desc: data.desc,
        link: data.link,
        imgUrl: data.image,
        success: function (res) {
          if (callback) {
            callback(res);
          }
          // 分享后的一些操作,比如分享统计等等
        },
        cancel: function (res) {},
      };

      // 新版 分享聊天api
      jweixin.updateAppMessageShareData(shareData);
      // 新版 分享到朋友圈api
      jweixin.updateTimelineShareData(shareData);
    });
  },

  // 打开坐标位置 TODO 芋艿：未测试
  openLocation(data, callback) {
    this.isReady(() => {
      jweixin.openLocation({
        ...data,
        success: function (res) {
          console.log(res);
        },
      });
    });
  },

  // 选择图片 TODO 芋艿：未测试
  chooseImage(callback) {
    this.isReady(() => {
      jweixin.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: function (rs) {
          callback(rs);
        },
      });
    });
  },

  // 微信支付
  wxpay(data, callback) {
    this.isReady(() => {
      jweixin.chooseWXPay({
        timestamp: data.timeStamp, // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
        nonceStr: data.nonceStr, // 支付签名随机串，不长于 32 位
        package: data.packageValue, // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=\*\*\*）
        signType: data.signType, // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
        paySign: data.paySign, // 支付签名
        success: function (res) {
          callback.success && callback.success(res);
        },
        fail: function (err) {
          callback.fail && callback.fail(err);
        },
        cancel: function (err) {
          callback.cancel && callback.cancel(err);
        },
      });
    });
  },
};
