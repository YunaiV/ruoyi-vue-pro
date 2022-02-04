const cfg = require('./config.js'),
	isLetter = c => (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');

function CssHandler(tagStyle) {
	var styles = Object.assign(Object.create(null), cfg.userAgentStyles);
	for (var item in tagStyle)
		styles[item] = (styles[item] ? styles[item] + ';' : '') + tagStyle[item];
	this.styles = styles;
}
CssHandler.prototype.getStyle = function(data) {
	this.styles = new parser(data, this.styles).parse();
}
CssHandler.prototype.match = function(name, attrs) {
	var tmp, matched = (tmp = this.styles[name]) ? tmp + ';' : '';
	if (attrs.class) {
		var items = attrs.class.split(' ');
		for (var i = 0, item; item = items[i]; i++)
			if (tmp = this.styles['.' + item])
				matched += tmp + ';';
	}
	if (tmp = this.styles['#' + attrs.id])
		matched += tmp + ';';
	return matched;
}
module.exports = CssHandler;

function parser(data, init) {
	this.data = data;
	this.floor = 0;
	this.i = 0;
	this.list = [];
	this.res = init;
	this.state = this.Space;
}
parser.prototype.parse = function() {
	for (var c; c = this.data[this.i]; this.i++)
		this.state(c);
	return this.res;
}
parser.prototype.section = function() {
	return this.data.substring(this.start, this.i);
}
// 状态机
parser.prototype.Space = function(c) {
	if (c == '.' || c == '#' || isLetter(c)) {
		this.start = this.i;
		this.state = this.Name;
	} else if (c == '/' && this.data[this.i + 1] == '*')
		this.Comment();
	else if (!cfg.blankChar[c] && c != ';')
		this.state = this.Ignore;
}
parser.prototype.Comment = function() {
	this.i = this.data.indexOf('*/', this.i) + 1;
	if (!this.i) this.i = this.data.length;
	this.state = this.Space;
}
parser.prototype.Ignore = function(c) {
	if (c == '{') this.floor++;
	else if (c == '}' && !--this.floor) this.state = this.Space;
}
parser.prototype.Name = function(c) {
	if (cfg.blankChar[c]) {
		this.list.push(this.section());
		this.state = this.NameSpace;
	} else if (c == '{') {
		this.list.push(this.section());
		this.Content();
	} else if (c == ',') {
		this.list.push(this.section());
		this.Comma();
	} else if (!isLetter(c) && (c < '0' || c > '9') && c != '-' && c != '_')
		this.state = this.Ignore;
}
parser.prototype.NameSpace = function(c) {
	if (c == '{') this.Content();
	else if (c == ',') this.Comma();
	else if (!cfg.blankChar[c]) this.state = this.Ignore;
}
parser.prototype.Comma = function() {
	while (cfg.blankChar[this.data[++this.i]]);
	if (this.data[this.i] == '{') this.Content();
	else {
		this.start = this.i--;
		this.state = this.Name;
	}
}
parser.prototype.Content = function() {
	this.start = ++this.i;
	if ((this.i = this.data.indexOf('}', this.i)) == -1) this.i = this.data.length;
	var content = this.section();
	for (var i = 0, item; item = this.list[i++];)
		if (this.res[item]) this.res[item] += ';' + content;
		else this.res[item] = content;
	this.list = [];
	this.state = this.Space;
}
