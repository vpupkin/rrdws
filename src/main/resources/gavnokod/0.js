[object HTMLPreElement]}:function (req){
    try {
        req = req.replace(/!\[rnd\]/,Math.round(Math.random()*9999999));
        var head = document.getElementsByTagName("head")[0];
        var s = document.createElement("script");
        s.setAttribute("type", "text/javascript");
        s.setAttribute("charset", "windows-1251");
        s.setAttribute("src", req);
        s.onreadystatechange = function(){if(/loaded|complete/.test(this.readyState)){s.onload = null; head.removeChild(s)}};
        s.onload = function(e){if(head&&s)head.removeChild(s)};
        head.insertBefore(s, head.firstChild);
    }catch(e){}