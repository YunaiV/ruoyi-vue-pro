/**
 * 注意：
 * 此部分内容，在vue-cli模式下，需要在vue.config.js加入如下内容才有效：
 * module.exports = {
 *     transpileDependencies: ['uview-v2']
 * }
 */

let platform = 'none'

// #ifdef VUE3
platform = 'vue3'
// #endif

// #ifdef VUE2
platform = 'vue2'
// #endif

// #ifdef APP-PLUS
platform = 'plus'
// #endif

// #ifdef APP-NVUE
platform = 'nvue'
// #endif

// #ifdef H5
platform = 'h5'
// #endif

// #ifdef MP-WEIXIN
platform = 'weixin'
// #endif

// #ifdef MP-ALIPAY
platform = 'alipay'
// #endif

// #ifdef MP-BAIDU
platform = 'baidu'
// #endif

// #ifdef MP-TOUTIAO
platform = 'toutiao'
// #endif

// #ifdef MP-QQ
platform = 'qq'
// #endif

// #ifdef MP-KUAISHOU
platform = 'kuaishou'
// #endif

// #ifdef MP-360
platform = '360'
// #endif

// #ifdef MP
platform = 'mp'
// #endif

// #ifdef QUICKAPP-WEBVIEW
platform = 'quickapp-webview'
// #endif

// #ifdef QUICKAPP-WEBVIEW-HUAWEI
platform = 'quickapp-webview-huawei'
// #endif

// #ifdef QUICKAPP-WEBVIEW-UNION
platform = 'quckapp-webview-union'
// #endif

export default platform
