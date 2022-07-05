/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:45:36
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/link.js
 */
import config from '../config'

const {
    color
} = config
export default {
    // link超链接组件props参数
    link: {
        color: color['u-primary'],
        fontSize: 15,
        underLine: false,
        href: '',
        mpTips: '链接已复制，请在浏览器打开',
        lineColor: '',
        text: ''
    }
}
