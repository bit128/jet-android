/**
* jet-sdk script
* ======
* @author 洪波
* @version 19.01.20
*/
var Jet = function() {
    this.apiHost = 'http://192.168.1.2:8001/';
    this.imgHost = 'http://192.168.1.2:8001/images/';
};
Jet.prototype = {
    constructor: Jet,
    deviceType : function(){
        if (window.android) {
            return 1;
        } else if (window.webkit) {
            return 2;
        }
        return 0;
    },
    uniqid: function(){
        let t = new Date().getTime();
        let r = Math.floor(16 + Math.random() * (255-16));
        return t.toString(16) + r.toString(16);
    },
    /**
     * 建立ajax异步网络请求
     * ======
     * @param url           访问地址 http://127.0.0.1/test
     * @param formData      post表单数据（若该字段存在，则表示为post请求方法）
     * @param callback      响应结果回调
     * @param resultType    响应结果格式 text（默认） | json
     */
    httpRequest: function(url, formData, callback, resultType) {
        const self = this;
        let xmlHttp = new XMLHttpRequest();
        let params = '';
        let method = 'GET';
        if (formData) {
            method = 'POST';
        }
        xmlHttp.onreadystatechange = function(){
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                if (resultType == 'json') {
                    return callback(JSON.parse(xmlHttp.responseText));
                } else {
                    return callback(xmlHttp.responseText);
                }
            }
        };
        xmlHttp.open(method, url, true);
        if (method == 'POST') {
            xmlHttp.setRequestHeader('Content-type','application/x-www-form-urlencoded');
            for (let key in formData) {
                params += '&' + key + '=' + formData[key];
            }
        }
        if (params != '') {
            xmlHttp.send(params.substring(1));
        } else {
            xmlHttp.send();
        }
    },
    /**
     * 调用桥接层功能模块
     * ====== ======
     * @param name      功能名称
     * ------ ------
     * @param params    桥接参数
     * --- 参考值 ---
     * {
     *  "trackId": 16
     *  //如果存在trackId，则回调名称为name+Callback
     *  //例如updateFile上传文件的默认回调为：updateFileCallback
     *  //所有回调的作用域为window，例如：window.updateFileCallback(result,trackId)
     * }
     * ------ ------
     * @param backup    备用逻辑（回调）
     * 如果原生层没有找到桥接方法，则执行这里的逻辑
     */
    addJSBridge: function(name, params, backup){
        if (window.android && window.android[name]) {
            if (params == undefined) {
                window.android[name]();
            } else {
                window.android[name](params);
            }
        } else if (window.webkit && window.webkit.messageHandlers[name]) {
            if (params == undefined) {
                params = "";
            }
            window.webkit.messageHandlers[name].postMessage(params);
        } else if (backup != undefined) {
            backup();
        }
    }
};