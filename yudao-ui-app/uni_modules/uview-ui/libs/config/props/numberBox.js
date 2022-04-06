/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:11:46
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/numberBox.js
 */
export default {
    // 步进器组件
    numberBox: {
        name: '',
        value: 0,
        min: 1,
        max: Number.MAX_SAFE_INTEGER,
        step: 1,
        integer: false,
        disabled: false,
        disabledInput: false,
        asyncChange: false,
        inputWidth: 35,
        showMinus: true,
        showPlus: true,
        decimalLength: null,
        longPress: true,
        color: '#323233',
        buttonSize: 30,
        bgColor: '#EBECEE',
        cursorSpacing: 100,
        disableMinus: false,
        disablePlus: false,
        iconStyle: ''
    }
}
