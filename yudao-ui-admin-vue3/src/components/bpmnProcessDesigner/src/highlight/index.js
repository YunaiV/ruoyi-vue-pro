const hljs = require("highlight.js/lib/core");
hljs.registerLanguage("xml", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("json", require("highlight.js/lib/languages/json"));

module.exports = hljs;
