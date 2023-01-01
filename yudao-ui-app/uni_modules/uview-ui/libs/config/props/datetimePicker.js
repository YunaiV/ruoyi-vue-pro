/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 16:57:48
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/datetimePicker.js
 */
export default {
    // datetimePicker 组件
    datetimePicker: {
        show: false,
        showToolbar: true,
        value: '',
        title: '',
        mode: 'datetime',
        maxDate: new Date(new Date().getFullYear() + 10, 0, 1).getTime(),
        minDate: new Date(new Date().getFullYear() - 10, 0, 1).getTime(),
        minHour: 0,
        maxHour: 23,
        minMinute: 0,
        maxMinute: 59,
        filter: null,
        formatter: null,
        loading: false,
        itemHeight: 44,
        cancelText: '取消',
        confirmText: '确认',
        cancelColor: '#909193',
        confirmColor: '#3c9cff',
        visibleItemCount: 5,
        closeOnClickOverlay: false,
        defaultIndex: () => []
    }
}
