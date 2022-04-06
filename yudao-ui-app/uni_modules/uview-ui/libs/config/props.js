/**
 * 此文件的作用为统一配置所有组件的props参数
 * 借此用户可以全局覆盖组件的props默认值
 * 无需在每个引入组件的页面中都配置一次
 */
import config from './config'

import actionSheet from './props/actionSheet.js'
import album from './props/album.js'
import alert from './props/alert.js'
import avatar from './props/avatar'
import avatarGroup from './props/avatarGroup'
import backtop from './props/backtop'
import badge from './props/badge'
import button from './props/button'
import calendar from './props/calendar'
import carKeyboard from './props/carKeyboard'
import cell from './props/cell'
import cellGroup from './props/cellGroup'
import checkbox from './props/checkbox'
import checkboxGroup from './props/checkboxGroup'
import circleProgress from './props/circleProgress'
import code from './props/code'
import codeInput from './props/codeInput'
import col from './props/col'
import collapse from './props/collapse'
import collapseItem from './props/collapseItem'
import columnNotice from './props/columnNotice'
import countDown from './props/countDown'
import countTo from './props/countTo'
import datetimePicker from './props/datetimePicker'
import divider from './props/divider'
import empty from './props/empty'
import form from './props/form'
import formItem from './props/formItem'
import gap from './props/gap'
import grid from './props/grid'
import gridItem from './props/gridItem'
import icon from './props/icon'
import image from './props/image'
import indexAnchor from './props/indexAnchor'
import indexList from './props/indexList'
import input from './props/input'
import keyboard from './props/keyboard'
import line from './props/line'
import lineProgress from './props/lineProgress'
import link from './props/link'
import list from './props/list'
import listItem from './props/listItem'
import loadingIcon from './props/loadingIcon'
import loadingPage from './props/loadingPage'
import loadmore from './props/loadmore'
import modal from './props/modal'
import navbar from './props/navbar'
import noNetwork from './props/noNetwork'
import noticeBar from './props/noticeBar'
import notify from './props/notify'
import numberBox from './props/numberBox'
import numberKeyboard from './props/numberKeyboard'
import overlay from './props/overlay'
import parse from './props/parse'
import picker from './props/picker'
import popup from './props/popup'
import radio from './props/radio'
import radioGroup from './props/radioGroup'
import rate from './props/rate'
import readMore from './props/readMore'
import row from './props/row'
import rowNotice from './props/rowNotice'
import scrollList from './props/scrollList'
import search from './props/search'
import section from './props/section'
import skeleton from './props/skeleton'
import slider from './props/slider'
import statusBar from './props/statusBar'
import steps from './props/steps'
import stepsItem from './props/stepsItem'
import sticky from './props/sticky'
import subsection from './props/subsection'
import swipeAction from './props/swipeAction'
import swipeActionItem from './props/swipeActionItem'
import swiper from './props/swiper'
import swipterIndicator from './props/swipterIndicator'
import _switch from './props/switch'
import tabbar from './props/tabbar'
import tabbarItem from './props/tabbarItem'
import tabs from './props/tabs'
import tag from './props/tag'
import text from './props/text'
import textarea from './props/textarea'
import toast from './props/toast'
import toolbar from './props/toolbar'
import tooltip from './props/tooltip'
import transition from './props/transition'
import upload from './props/upload'

const {
    color
} = config

export default {
    ...actionSheet,
    ...album,
    ...alert,
    ...avatar,
    ...avatarGroup,
    ...backtop,
    ...badge,
    ...button,
    ...calendar,
    ...carKeyboard,
    ...cell,
    ...cellGroup,
    ...checkbox,
    ...checkboxGroup,
    ...circleProgress,
    ...code,
    ...codeInput,
    ...col,
    ...collapse,
    ...collapseItem,
    ...columnNotice,
    ...countDown,
    ...countTo,
    ...datetimePicker,
    ...divider,
    ...empty,
    ...form,
    ...formItem,
    ...gap,
    ...grid,
    ...gridItem,
    ...icon,
    ...image,
    ...indexAnchor,
    ...indexList,
    ...input,
    ...keyboard,
    ...line,
    ...lineProgress,
    ...link,
    ...list,
    ...listItem,
    ...loadingIcon,
    ...loadingPage,
    ...loadmore,
    ...modal,
    ...navbar,
    ...noNetwork,
    ...noticeBar,
    ...notify,
    ...numberBox,
    ...numberKeyboard,
    ...overlay,
    ...parse,
    ...picker,
    ...popup,
    ...radio,
    ...radioGroup,
    ...rate,
    ...readMore,
    ...row,
    ...rowNotice,
    ...scrollList,
    ...search,
    ...section,
    ...skeleton,
    ...slider,
    ...statusBar,
    ...steps,
    ...stepsItem,
    ...sticky,
    ...subsection,
    ...swipeAction,
    ...swipeActionItem,
    ...swiper,
    ...swipterIndicator,
    ..._switch,
    ...tabbar,
    ...tabbarItem,
    ...tabs,
    ...tag,
    ...text,
    ...textarea,
    ...toast,
    ...toolbar,
    ...tooltip,
    ...transition,
    ...upload
}
