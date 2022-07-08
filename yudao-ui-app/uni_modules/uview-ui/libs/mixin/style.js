export default {
    props: {
        // flex排列方式
        flexDirection: {
            type: String,
            default: ''
        },
        // flex-direction的简写
        fd: {
            type: String,
            default: ''
        },
        // 展示类型
        display: {
            type: String,
            default: ''
        },
        // display简写
        d: {
            type: String,
            default: ''
        },
        // 主轴排列方式
        justifyContent: {
            type: String,
            default: ''
        },
        // justifyContent的简写
        jc: {
            type: String,
            default: ''
        },
        // 纵轴排列方式
        alignItems: {
            type: String,
            default: ''
        },
        // align-items的简写
        ai: {
            type: String,
            default: ''
        },
        color: {
            type: String,
            default: ''
        },
        // color简写
        c: {
            type: String,
            default: ''
        },
        // 字体大小
        fontSize: {
            type: [String, Number],
            default: 0
        },
        // font-size简写
        fs: {
            type: [String, Number],
            default: ''
        },
        margin: {
            type: [String, Number],
            default: 0
        },
        // margin简写
        m: {
            type: [String, Number],
            default: 0
        },
        // margin-top
        marginTop: {
            type: [String, Number],
            default: 0
        },
        // margin-top简写
        mt: {
            type: [String, Number],
            default: 0
        },
        // margin-right
        marginRight: {
            type: [String, Number],
            default: 0
        },
        // margin-right简写
        mr: {
            type: [String, Number],
            default: 0
        },
        // margin-bottom
        marginBottom: {
            type: [String, Number],
            default: 0
        },
        // margin-bottom简写
        mb: {
            type: [String, Number],
            default: 0
        },
        // margin-left
        marginLeft: {
            type: [String, Number],
            default: 0
        },
        // margin-left简写
        ml: {
            type: [String, Number],
            default: 0
        },
        // padding-left
        paddingLeft: {
            type: [String, Number],
            default: 0
        },
        // padding-left简写
        pl: {
            type: [String, Number],
            default: 0
        },
        // padding-top
        paddingTop: {
            type: [String, Number],
            default: 0
        },
        // padding-top简写
        pt: {
            type: [String, Number],
            default: 0
        },
        // padding-right
        paddingRight: {
            type: [String, Number],
            default: 0
        },
        // padding-right简写
        pr: {
            type: [String, Number],
            default: 0
        },
        // padding-bottom
        paddingBottom: {
            type: [String, Number],
            default: 0
        },
        // padding-bottom简写
        pb: {
            type: [String, Number],
            default: 0
        },
        // border-radius
        borderRadius: {
            type: [String, Number],
            default: 0
        },
        // border-radius简写
        radius: {
            type: [String, Number],
            default: 0
        },
        // transform
        transform: {
            type: String,
            default: ''
        },
        // 定位
        position: {
            type: String,
            default: ''
        },
        // position简写
        pos: {
            type: String,
            default: ''
        },
        // 宽度
        width: {
            type: [String, Number],
            default: null
        },
        // width简写
        w: {
            type: [String, Number],
            default: null
        },
        // 高度
        height: {
            type: [String, Number],
            default: null
        },
        // height简写
        h: {
            type: [String, Number],
            default: null
        },
        top: {
            type: [String, Number],
            default: 0
        },
        right: {
            type: [String, Number],
            default: 0
        },
        bottom: {
            type: [String, Number],
            default: 0
        },
        left: {
            type: [String, Number],
            default: 0
        }
    },
    computed: {
        viewStyle() {
            const style = {}
            const addStyle = uni.$u.addStyle(this.width || this.w) && (style.width = addStyle(this.width || this.w))(this.height || this.h) && (style.height = addStyle(this.height || this.h))(this.margin || this.m) && (style.margin = addStyle(this.margin || this.m))(this.marginTop || this.mt) && (style.marginTop = addStyle(this.marginTop || this.mt))(this.marginRight || this.mr) && (style.marginRight = addStyle(this.marginRight || this.mr))(this.marginBottom || this.mb) && (style.marginBottom = addStyle(this.marginBottom || this.mb))(this.marginLeft || this.ml) && (style.marginLeft = addStyle(this.marginLeft || this.ml))(this.padding || this.p) && (style.padding = addStyle(this.padding || this.p))(this.paddingTop || this.pt) && (style.paddingTop = addStyle(this.paddingTop || this.pt))(this.paddingRight || this.pr) && (style.paddingRight = addStyle(this.paddingRight || this.pr))(this.paddingBottom || this.pb) && (style.paddingBottom = addStyle(this.paddingBottom || this.pb))(this.paddingLeft || this.pl) && (style.paddingLeft = addStyle(this.paddingLeft || this.pl))(this.color || this.c) && (style.color = this.color || this.c)(this.fontSize || this.fs) && (style.fontSize = this.fontSize || this.fs)(this.borderRadius || this.radius) && (style.borderRadius = this.borderRadius || this.radius)(this.position || this.pos) && (this.position = this.position || this.pos)(this.flexDirection || this.fd) && (this.flexDirection = this.flexDirection || this.fd)(this.justifyContent || jc) && (this.justifyContent = this.justifyContent || jc)(this.alignItems || ai) && (this.alignItems = this.alignItems || ai)

            return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
        }
    },
    methods: {
        // 获取margin或者padding的单位，比如padding: 0 20转为padding: 0 20px
        getUnit(unit = '') {
            // 取出两端空格，分隔成数组，再对数组的每个元素添加单位，最后再合并成字符串
            return uni.$u.trim(unit).split(' ').map((item) => uni.$u.addUnit(item)).join(' ')
        }
    }
}
