/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:45:47
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/loadingIcon.js
 */
import config from '../config'

const {
    color
} = config
export default {
    // loading-icon加载中图标组件
    loadingIcon: {
        show: true,
        color: color['u-tips-color'],
        textColor: color['u-tips-color'],
        vertical: false,
        mode: 'spinner',
        size: 24,
        textSize: 15,
        text: '',
        timingFunction: 'ease-in-out',
        duration: 1200,
        inactiveColor: ''
    }
}
