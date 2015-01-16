attrs = document.evaluate(arguments[0], document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.attributes; 
arr = [];
for (var i = 0, n = attrs.length; i < n; i++) 
{ arr.push(attrs[i].nodeName); };

return arr;
