export default {
    props: {
        // #ifdef APP-PLUS-NVUE
        bgColor: String,
        // #endif
        content: String,
        copyLink: {
		  type: Boolean,
		  default: uni.$u.props.parse.copyLink
        },
        domain: String,
        errorImg: {
		  type: String,
		  default: uni.$u.props.parse.errorImg
        },
        lazyLoad: {
		  type: Boolean,
		  default: uni.$u.props.parse.lazyLoad
        },
        loadingImg: {
		  type: String,
		  default: uni.$u.props.parse.loadingImg
        },
        pauseVideo: {
		  type: Boolean,
		  default: uni.$u.props.parse.pauseVideo
        },
        previewImg: {
		  type: Boolean,
		  default: uni.$u.props.parse.previewImg
        },
        scrollTable: Boolean,
        selectable: Boolean,
        setTitle: {
		  type: Boolean,
		  default: uni.$u.props.parse.setTitle
        },
        showImgMenu: {
		  type: Boolean,
		  default: uni.$u.props.parse.showImgMenu
        },
        tagStyle: Object,
        useAnchor: null
	  }
}
