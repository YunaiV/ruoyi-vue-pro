/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 18:00:14
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/icon.js
 */
import config from '../config'

const {
    color
} = config
export default {
    // icon组件
    icon: {
        name: '',
        color: color['u-content-color'],
        size: '16px',
        bold: false,
        index: '',
        hoverClass: '',
        customPrefix: 'uicon',
        label: '',
        labelPos: 'right',
        labelSize: '15px',
        labelColor: color['u-content-color'],
        space: '3px',
        imgMode: '',
        width: '',
        height: '',
        top: 0,
        stop: false
    }
}
