import hljs from 'highlight.js/lib/core';
import jsonLanguage from 'highlight.js/lib/languages/json';
import xmlLanguage from 'highlight.js/lib/languages/xml';

hljs.registerLanguage('xml', xmlLanguage);
hljs.registerLanguage('json', jsonLanguage);

export default hljs;
