
/*globals define*/
(function (root, factory) {
    "use strict";
    if (typeof define === 'function' && define.amd) {
        define(['crel'], factory);
    } else if (typeof module !== 'undefined' && module.exports) {
		module.exports = factory(require('../lib/crel'));
    } else {
        root.JsonHuman = factory(root.crel);
    }
}(this, function (crel) {
    "use strict";
    var toString = Object.prototype.toString,
        ARRAY = 1,
        BOOL = 2,
        INT = 3,
        FLOAT = 4,
        STRING = 5,
        OBJECT = 6,
        FUNCTION = 7,
        UNK = 99;

    function makePrefixer(prefix) {
        return function (name) {
            return prefix + "-" + name;
        };
    }

    function getType(obj) {
        var type = typeof obj;

        if (type === "boolean") {
            return BOOL;
        } else if (type === "string") {
            return STRING;
        } else if (type === "number") {
            return (obj % 1 === 0) ? INT : FLOAT;
        } else if (type === "function") {
            return FUNCTION;
        } else if (toString.call(obj) === '[object Array]') {
            return ARRAY;
        } else if (obj === Object(obj)) {
            return OBJECT;
        } else {
            return UNK;
        }
    }

    /**
     * Modified to ignore empty arrays, empty strings and string containing only whitespace
     */
    function _format(data, prefixer) {
        var result, container, key, keyNode, valNode,
            isEmpty = true,
            p = prefixer,
            accum = [],
            type = getType(data);

        switch (type) {
        case BOOL:
            result = crel("span", {"class": p("type-bool")}, "" + data);
            break;
        case STRING:
        	// EDITED
            if (data === "" || !/\S/.test(data)) {
                result = null;
               
            }else {	
                result = crel("span", {"class": p("type-string")}, "");
                result.innerHTML = data
                    .replace(/&/g, '&amp;')
                    .replace(/ /g, "&nbsp;")
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/[\r\n]/g, '<br/>')
                    .replace(/"/g, '&quot;'); // ")
            }
            // EDITED END
            break;
        case INT:
            result = crel("span",
                         {"class": p("type-int") + " " + p("type-number")},
                          "" + data);
            break;
        case FLOAT:
            result = crel("span",
                         {"class": p("type-float") + " " + p("type-number")},
                         "" + data);
            break;
        case OBJECT:
            result = crel("table", {"class": p("type-object")});
            for (key in data) {
                isEmpty = false;
                keyNode = crel("th",
                         {"class": p("key") + " " + p("object-key")},
                         "" + key);
                
                // EDITED
                var value = _format(data[key], p);
                if(value == null){
                	continue;
                }
                
                valNode = crel("td",
                         {"class": p("value") + " " + p("object-value")},
                         value);
             
                result.appendChild(crel("tr", keyNode, valNode));
            }

            if (isEmpty) {
                result = null;
             // EDITED END
            }
            break;
        case FUNCTION:
            result = crel("span", {"class": p("type-function")}, "" + data);
            break;
        case ARRAY:
            if (data.length > 0) {
                result = crel("table", {"class": p("type-array")});
                for (key = 0; key < data.length; key += 1) {
                    keyNode = crel("th",
                             {"class": p("key") + " " + p("array-key")},
                             "" + key);
                    // EDITED
                    var value = _format(data[key], p);
                    if(value == null){
                    	continue;
                    }
                    valNode = crel("td",
                             {"class": p("value") + " " + p("array-value")},
                             value);
                    result.appendChild(crel("tr", keyNode, valNode));
                }
            } else {
                result = null;
                // EDITED END
            }
            break;
        default:
            result = crel("span", {"class": p("type-unk")},
                          "" + data);
            break;
        }

        return result;
    }

    function format(data, options) {
        options = options || {};
        var result,
            prefixer = makePrefixer(options.prefix || "jh");

        result = _format(data, prefixer);
        result.className = result.className + " " + prefixer("root");

        return result;
    }

    return {
        format: format
    };
}));