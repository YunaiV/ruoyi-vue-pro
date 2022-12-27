/*
 * @Author       : LQ
 * @Description  :
 * @version      : 1.0
 * @Date         : 2021-08-20 16:44:21
 * @LastAuthor   : LQ
 * @lastTime     : 2021-08-20 17:18:20
 * @FilePath     : /u-view2.0/uview-ui/libs/config/props/picker.js
 */
export default {
    // picker
    picker: {
        show: false,
        showToolbar: true,
        title: '',
        columns: () => [],
        loading: false,
        itemHeight: 44,
        cancelText: '取消',
        confirmText: '确定',
        cancelColor: '#909193',
        confirmColor: '#3c9cff',
        visibleItemCount: 5,
        keyName: 'text',
        closeOnClickOverlay: false,
        defaultIndex: () => [],
		immediateChange: false
    }
}
