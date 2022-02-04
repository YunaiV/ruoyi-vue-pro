/* 配置文件 */
// #ifdef MP-WEIXIN
const canIUse = wx.canIUse('editor'); // 高基础库标识，用于兼容
// #endif
module.exports = {
	// 出错占位图
	errorImg: null,
	// 过滤器函数
	filter: null,
	// 代码高亮函数
	highlight: null,
	// 文本处理函数
	onText: null,
	// 实体编码列表
	entities: {
		quot: '"',
		apos: "'",
		semi: ';',
		nbsp: '\xA0',
		ensp: '\u2002',
		emsp: '\u2003',
		ndash: '–',
		mdash: '—',
		middot: '·',
		lsquo: '‘',
		rsquo: '’',
		ldquo: '“',
		rdquo: '”',
		bull: '•',
		hellip: '…'
	},
	blankChar: makeMap(' ,\xA0,\t,\r,\n,\f'),
	boolAttrs: makeMap('allowfullscreen,autoplay,autostart,controls,ignore,loop,muted'),
	// 块级标签，将被转为 div
	blockTags: makeMap('address,article,aside,body,caption,center,cite,footer,header,html,nav,section' + (
		// #ifdef MP-WEIXIN
		canIUse ? '' :
		// #endif
		',pre')),
	// 将被移除的标签
	ignoreTags: makeMap(
		'area,base,canvas,frame,input,link,map,meta,param,script,source,style,svg,textarea,title,track,wbr'
		// #ifdef MP-WEIXIN
		+ (canIUse ? ',rp' : '')
		// #endif
		// #ifndef APP-PLUS
		+ ',iframe'
		// #endif
	),
	// 只能被 rich-text 显示的标签
	richOnlyTags: makeMap('a,colgroup,fieldset,legend,table'
		// #ifdef MP-WEIXIN
		+ (canIUse ? ',bdi,bdo,caption,rt,ruby' : '')
		// #endif
	),
	// 自闭合的标签
	selfClosingTags: makeMap(
		'area,base,br,col,circle,ellipse,embed,frame,hr,img,input,line,link,meta,param,path,polygon,rect,source,track,use,wbr'
	),
	// 信任的标签
	trustTags: makeMap(
		'a,abbr,ad,audio,b,blockquote,br,code,col,colgroup,dd,del,dl,dt,div,em,fieldset,h1,h2,h3,h4,h5,h6,hr,i,img,ins,label,legend,li,ol,p,q,source,span,strong,sub,sup,table,tbody,td,tfoot,th,thead,tr,title,ul,video'
		// #ifdef MP-WEIXIN
		+ (canIUse ? ',bdi,bdo,caption,pre,rt,ruby' : '')
		// #endif
		// #ifdef APP-PLUS
		+ ',embed,iframe'
		// #endif
	),
	// 默认的标签样式
	userAgentStyles: {
		address: 'font-style:italic',
		big: 'display:inline;font-size:1.2em',
		blockquote: 'background-color:#f6f6f6;border-left:3px solid #dbdbdb;color:#6c6c6c;padding:5px 0 5px 10px',
		caption: 'display:table-caption;text-align:center',
		center: 'text-align:center',
		cite: 'font-style:italic',
		dd: 'margin-left:40px',
		mark: 'background-color:yellow',
		pre: 'font-family:monospace;white-space:pre;overflow:scroll',
		s: 'text-decoration:line-through',
		small: 'display:inline;font-size:0.8em',
		u: 'text-decoration:underline'
	}
}

function makeMap(str) {
	var map = Object.create(null),
		list = str.split(',');
	for (var i = list.length; i--;)
		map[list[i]] = true;
	return map;
}
