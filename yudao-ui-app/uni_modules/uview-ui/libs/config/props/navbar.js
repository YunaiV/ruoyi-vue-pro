/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:16:18
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/navbar.js
 */
import color from '../color'
export default {
    // navbar 组件
    navbar: {
        safeAreaInsetTop: true,
        placeholder: false,
        fixed: true,
        border: false,
        leftIcon: 'arrow-left',
        leftText: '',
        rightText: '',
        rightIcon: '',
        title: '',
        bgColor: '#ffffff',
        titleWidth: '400rpx',
        height: '44px',
		leftIconSize: 20,
		leftIconColor: color.mainColor,
		autoBack: false,
		titleStyle: ''
    }

}
