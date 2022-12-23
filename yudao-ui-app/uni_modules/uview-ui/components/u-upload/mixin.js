export default {
    watch: {
        // 监听accept的变化，判断是否符合个平台要求
        // 只有微信小程序才支持选择媒体，文件类型，所以这里做一个判断提示
        accept: {
            immediate: true,
            handler(val) {
                // #ifndef MP-WEIXIN
                if (val === 'all' || val === 'media') {
                    uni.$u.error('只有微信小程序才支持把accept配置为all、media之一')
                }
                // #endif
                // #ifndef H5 || MP-WEIXIN
                if (val === 'file') {
                    uni.$u.error('只有微信小程序和H5(HX2.9.9)才支持把accept配置为file')
                }
                // #endif
            }
        }
    }
}
