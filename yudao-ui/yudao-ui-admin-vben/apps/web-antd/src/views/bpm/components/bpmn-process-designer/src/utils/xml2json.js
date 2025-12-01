function xmlStr2XmlObj(xmlStr) {
  let xmlObj = {};
  if (document.all) {
    const xmlDom = new window.ActiveXObject('Microsoft.XMLDOM');
    xmlDom.loadXML(xmlStr);
    xmlObj = xmlDom;
  } else {
    xmlObj = new DOMParser().parseFromString(xmlStr, 'text/xml');
  }
  return xmlObj;
}

function xml2json(xml) {
  try {
    let obj = {};
    if (xml.children.length > 0) {
      for (let i = 0; i < xml.children.length; i++) {
        const item = xml.children.item(i);
        const nodeName = item.nodeName;
        if (obj[nodeName] === undefined) {
          obj[nodeName] = xml2json(item);
        } else {
          if (obj[nodeName].push === undefined) {
            const old = obj[nodeName];
            obj[nodeName] = [];
            obj[nodeName].push(old);
          }
          obj[nodeName].push(xml2json(item));
        }
      }
    } else {
      obj = xml.textContent;
    }
    return obj;
  } catch (error) {
    console.warn(error.message);
  }
}

function xmlObj2json(xml) {
  const xmlObj = xmlStr2XmlObj(xml);
  console.warn(xmlObj);
  let jsonObj = {};
  if (xmlObj.childNodes.length > 0) {
    jsonObj = xml2json(xmlObj);
  }
  return jsonObj;
}

export default xmlObj2json;
