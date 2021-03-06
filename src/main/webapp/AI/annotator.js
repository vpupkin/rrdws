(function() {
  var h, o, b, p, n, i, l, a, e, d, c, r, s, j;
  var m = Array.prototype.slice,
    g = function(t, u) {
      return function() {
        return t.apply(u, arguments)
      }
    },
    k = Object.prototype.hasOwnProperty,
    q = function(w, u) {
      for (var t in u) {
        if (k.call(u, t)) {
          w[t] = u[t]
        }
      }
      function v() {
        this.constructor = w
      }
      v.prototype = u.prototype;
      w.prototype = new v;
      w.__super__ = u.prototype;
      return w
    },
    f = Array.prototype.indexOf ||
    function(v) {
      for (var u = 0, t = this.length; u < t; u++) {
        if (this[u] === v) {
          return u
        }
      }
      return -1
    };
  if (!(typeof jQuery !== "undefined" && jQuery !== null ? (j = jQuery.fn) != null ? j.jquery : void 0 : void 0)) {
    console.error("Annotator requires jQuery: have you included lib/vendor/jquery.js?")
  }
  if (!(JSON && JSON.parse && JSON.stringify)) {
    console.error("Annotator requires a JSON implementation: have you included lib/vendor/json2.js?")
  }
  h = jQuery.sub();
  h.flatten = function(u) {
    var t;
    t = function(w) {
      var x, z, y, v;
      z = [];
      for (y = 0, v = w.length; y < v; y++) {
        x = w[y];
        z = z.concat(x && h.isArray(x) ? t(x) : x)
      }
      return z
    };
    return t(u)
  };
  h.plugin = function(u, t) {
    return jQuery.fn[u] = function(w) {
      var v;
      v = Array.prototype.slice.call(arguments, 1);
      return this.each(function() {
        var x;
        x = h.data(this, u);
        if (x) {
          return w && x[w].apply(x, v)
        } else {
          x = new t(this, w);
          return h.data(this, u, x)
        }
      })
    }
  };
  h.fn.textNodes = function() {
    var t;
    t = function(v) {
      var u;
      if (v && v.nodeType !== 3) {
        u = [];
        if (v.nodeType !== 8) {
          v = v.lastChild;
          while (v) {
            u.push(t(v));
            v = v.previousSibling
          }
        }
        return u.reverse()
      } else {
        return v
      }
    };
    return this.map(function() {
      return h.flatten(t(this))
    })
  };
  h.fn.xpath = function(t) {
    var u;
    u = this.map(function() {
      var w, v, x;
      x = "";
      w = this;
      while (w && w.nodeType === 1 && w !== t) {
        v = h(w.parentNode).children(w.tagName).index(w) + 1;
        v = v > 1 ? "[" + v + "]" : "";x = "/" + w.tagName.toLowerCase() + v + x;w = w.parentNode
      }
      return x
    });
    return u.get()
  };
  h.escape = function(t) {
    return t.replace(/&(?!\w+;)/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;")
  };
  h.fn.escape = function(t) {
    if (arguments.length) {
      return this.html(h.escape(t))
    }
    return this.html()
  };
  l = ["log", "debug", "info", "warn", "exception", "assert", "dir", "dirxml", "trace", "group", "groupEnd", "groupCollapsed", "time", "timeEnd", "profile", "profileEnd", "count", "clear", "table", "error", "notifyFirebug", "firebug", "userObjects"];
  if (typeof console !== "undefined" && console !== null) {
    if (!(console.group != null)) {
      console.group = function(t) {
        return console.log("GROUP: ", t)
      }
    }
    if (!(console.groupCollapsed != null)) {
      console.groupCollapsed = console.group
    }
    for (d = 0, r = l.length; d < r; d++) {
      i = l[d];
      if (!(console[i] != null)) {
        console[i] = function() {
          return console.log("Not implemented: console." + name)
        }
      }
    }
  } else {
    this.console = {};
    for (c = 0, s = l.length; c < s; c++) {
      i = l[c];
      this.console[i] = function() {}
    }
    this.console.error = function() {
      var t;
      t = 1 <= arguments.length ? m.call(arguments, 0) : [];
      return alert("ERROR: " + (t.join(", ")))
    };
    this.console.warn = function() {
      var t;
      t = 1 <= arguments.length ? m.call(arguments, 0) : [];
      return alert("WARNING: " + (t.join(", ")))
    }
  }
  b = (function() {
    t.prototype.events = {};
    t.prototype.options = {};
    t.prototype.element = null;

    function t(v, u) {
      this.options = h.extend(true, {}, this.options, u);
      this.element = h(v);
      this.on = this.subscribe;
      this.addEvents()
    }
    t.prototype.addEvents = function() {
      var x, z, y, u, w, B, A, v;
      B = this.events;
      v = [];
      for (y in B) {
        z = B[y];
        A = y.split(" "), u = 2 <= A.length ? m.call(A, 0, w = A.length - 1) : (w = 0, []),
        x = A[w++];v.push(this.addEvent(u.join(" "), x, z))
      }
      return v
    };
    t.prototype.addEvent = function(u, w, x) {
      var y, v;
      y = g(function() {
        return this[x].apply(this, arguments)
      }, this);
      v = typeof u === "string" && u.replace(/\s+/g, "") === "";
      if (v) {
        u = this.element
      }
      if (typeof u === "string") {
        this.element.delegate(u, w, y)
      } else {
        if (this.isCustomEvent(w)) {
          this.subscribe(w, y)
        } else {
          h(u).bind(w, y)
        }
      }
      return this
    };
    t.prototype.isCustomEvent = function(v) {
      var u;
      u = "blur focus focusin focusout load resize scroll unload click dblclick\nmousedown mouseup mousemove mouseover mouseout mouseenter mouseleave\nchange select submit keydown keypress keyup error".split(/[^a-z]+/);
      v = v.split(".")[0];
      return h.inArray(v, u) === -1
    };
    t.prototype.publish = function() {
      this.element.triggerHandler.apply(this.element, arguments);
      return this
    };
    t.prototype.subscribe = function(u, w) {
      var v;
      v = function() {
        return w.apply(this, [].slice.call(arguments, 1))
      };
      v.guid = w.guid = (h.guid += 1);
      this.element.bind(u, v);
      return this
    };
    t.prototype.unsubscribe = function() {
      this.element.unbind.apply(this.element, arguments);
      return this
    };
    return t
  })();
  p = {};
  p.sniff = function(t) {
    if (t.commonAncestorContainer != null) {
      return new p.BrowserRange(t)
    } else {
      if (typeof t.start === "string") {
        return new p.SerializedRange(t)
      } else {
        if (t.start && typeof t.start === "object") {
          return new p.NormalizedRange(t)
        } else {
          console.error("Couldn't not sniff range type");
          return false
        }
      }
    }
  };
  p.BrowserRange = (function() {
    function t(u) {
      this.commonAncestorContainer = u.commonAncestorContainer;
      this.startContainer = u.startContainer;
      this.startOffset = u.startOffset;
      this.endContainer = u.endContainer;
      this.endOffset = u.endOffset
    }
    t.prototype.normalize = function(C) {
      var B, x, D, A, w, v, z, u, y;
      if (this.tainted) {
        console.error("You may only call normalize() once on a BrowserRange!");
        return false
      } else {
        this.tainted = true
      }
      v = {};
      D = {};
      y = ["start", "end"];
      for (z = 0, u = y.length; z < u; z++) {
        w = y[z];
        x = this[w + "Container"];
        A = this[w + "Offset"];
        if (x.nodeType === 1) {
          B = x.childNodes[A];
          x = B || x.childNodes[A - 1];
          while (x.nodeType !== 3) {
            x = x.firstChild
          }
          A = B ? 0 : x.nodeValue.length
        }
        v[w] = x;
        v[w + "Offset"] = A
      }
      D.start = v.startOffset > 0 ? v.start.splitText(v.startOffset) : v.start;
      if (v.start === v.end) {
        if ((v.endOffset - v.startOffset) < D.start.nodeValue.length) {
          D.start.splitText(v.endOffset - v.startOffset)
        }
        D.end = D.start
      } else {
        if (v.endOffset < v.end.nodeValue.length) {
          v.end.splitText(v.endOffset)
        }
        D.end = v.end
      }
      D.commonAncestor = this.commonAncestorContainer;
      while (D.commonAncestor.nodeType !== 1) {
        D.commonAncestor = D.commonAncestor.parentNode
      }
      return new p.NormalizedRange(D)
    };
    t.prototype.serialize = function(u, v) {
      return this.normalize(u).serialize(u, v)
    };
    return t
  })();
  p.NormalizedRange = (function() {
    function t(u) {
      this.commonAncestor = u.commonAncestor;
      this.start = u.start;
      this.end = u.end
    }
    t.prototype.normalize = function(u) {
      return this
    };
    t.prototype.limit = function(y) {
      var u, x, v, w, A, z;
      u = h.grep(this.textNodes(), function(B) {
        return B.parentNode === y || h.contains(y, B.parentNode)
      });
      if (!u.length) {
        return null
      }
      this.start = u[0];
      this.end = u[u.length - 1];
      v = h(this.start).parents();
      z = h(this.end).parents();
      for (w = 0, A = z.length; w < A; w++) {
        x = z[w];
        if (v.index(x) !== -1) {
          this.commonAncestor = x;
          break
        }
      }
      return this
    };
    t.prototype.serialize = function(w, x) {
      var v, u, y;
      u = function(D, C) {
        var B, A, F, I, H, G, E, z;
        if (x) {
          I = h(D).parents(":not(" + x + ")").eq(0)
        } else {
          I = h(D).parent()
        }
        G = I.xpath(w)[0];
        H = I.textNodes();
        A = H.slice(0, H.index(D));
        F = 0;
        for (E = 0, z = A.length; E < z; E++) {
          B = A[E];
          F += B.nodeValue.length
        }
        if (C) {
          return [G, F + D.nodeValue.length]
        } else {
          return [G, F]
        }
      };
      y = u(this.start);
      v = u(this.end, true);
      return new p.SerializedRange({
        start: y[0],
        end: v[0],
        startOffset: y[1],
        endOffset: v[1]
      })
    };
    t.prototype.text = function() {
      var u;
      return ((function() {
        var w, y, x, v;
        x = this.textNodes();
        v = [];
        for (w = 0, y = x.length; w < y; w++) {
          u = x[w];
          v.push(u.nodeValue)
        }
        return v
      }).call(this)).join("")
    };
    t.prototype.textNodes = function() {
      var u, x, v, w;
      v = h(this.commonAncestor).textNodes();
      w = [v.index(this.start), v.index(this.end)], x = w[0], u = w[1];
      return h.makeArray(v.slice(x, (u + 1) || 9000000000))
    };
    return t
  })();
  p.SerializedRange = (function() {
    function t(u) {
      this.start = u.start;
      this.startOffset = u.startOffset;
      this.end = u.end;
      this.endOffset = u.endOffset
    }
    t.prototype._nodeFromXPath = function(u) {
      var z, y, v, x, w;
      y = function(B, A) {
        if (A == null) {
          A = null
        }
        return document.evaluate(B, document, A, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue
      };
      if (!h.isXMLDoc(document.documentElement)) {
        return y(u)
      } else {
        z = document.createNSResolver(document.ownerDocument === null ? document.documentElement : document.ownerDocument.documentElement);
        x = y(u, z);
        if (!x) {
          u = ((function() {
            var B, D, C, A;
            C = u.split("/");
            A = [];
            for (B = 0, D = C.length; B < D; B++) {
              w = C[B];
              A.push(w && w.indexOf(":") === -1 ? w.replace(/^([a-z]+)/, "xhtml:$1") : w)
            }
            return A
          })()).join("/");
          v = document.lookupNamespaceURI(null);
          z = function(A) {
            if (A === "xhtml") {
              return v
            } else {
              return document.documentElement.getAttribute("xmlns:" + A)
            }
          };
          x = y(u, z)
        }
        return x
      }
    };
    t.prototype.normalize = function(G) {
      var K, D, C, I, A, F, H, E, B, z, v, u, L, J, y, x, w;
      H = h(G).xpath()[0];
      B = this.start.split("/");
      C = this.end.split("/");
      D = [];
      E = {};
      for (I = 0, y = B.length; 0 <= y ? I < y : I > y;0 <= y ? I++ : I--) {
        if (B[I] === C[I]) {
          D.push(B[I])
        } else {
          break
        }
      }
      K = H + D.join("/");
      E.commonAncestorContainer = this._nodeFromXPath(K);
      if (!E.commonAncestorContainer) {
        console.error("Error deserializing range: can't find XPath '" + K + "'. Is this the right document?");
        return null
      }
      x = ["start", "end"];
      for (v = 0, L = x.length; v < L; v++) {
        F = x[v];
        A = 0;
        w = h(this._nodeFromXPath(H + this[F])).textNodes();
        for (u = 0, J = w.length; u < J; u++) {
          z = w[u];
          if (A + z.nodeValue.length >= this[F + "Offset"]) {
            E[F + "Container"] = z;
            E[F + "Offset"] = this[F + "Offset"] - A;
            break
          } else {
            A += z.nodeValue.length
          }
        }
      }
      return new p.BrowserRange(E).normalize(G)
    };
    t.prototype.serialize = function(u, v) {
      return this.normalize(u).serialize(u, v)
    };
    t.prototype.toObject = function() {
      return {
        start: this.start,
        startOffset: this.startOffset,
        end: this.end,
        endOffset: this.endOffset
      }
    };
    return t
  })();
  a = {
    getGlobal: function() {
      return (function() {
        return this
      })()
    },
    mousePosition: function(t, v) {
      var u;
      u = h(v).offset();
      return {
        top: t.pageY - u.top,
        left: t.pageX - u.left
      }
    }
  };
  e = this.Annotator;
  o = (function() {
    q(t, b);
    t.prototype.events = {
      ".annotator-adder button click": "onAdderClick",
      ".annotator-adder button mousedown": "onAdderMousedown",
      ".annotator-hl mouseover": "onHighlightMouseover",
      ".annotator-hl mouseout": "startViewerHideTimer"
    };
    t.prototype.html = {
      hl: '<span class="annotator-hl"></span>',
      adder: '<div class="annotator-adder"><button>Annotate</button></div>',
      wrapper: '<div class="annotator-wrapper"></div>'
    };
    t.prototype.options = {};
    t.prototype.plugins = {};
    t.prototype.editor = null;
    t.prototype.viewer = null;
    t.prototype.selectedRanges = null;
    t.prototype.mouseIsDown = false;
    t.prototype.ignoreMouseup = false;
    t.prototype.viewerHideTimer = null;

    function t(w, v) {
      this.onDeleteAnnotation = g(this.onDeleteAnnotation, this);
      this.onEditAnnotation = g(this.onEditAnnotation, this);
      this.onAdderClick = g(this.onAdderClick, this);
      this.onAdderMousedown = g(this.onAdderMousedown, this);
      this.onHighlightMouseover = g(this.onHighlightMouseover, this);
      this.checkForEndSelection = g(this.checkForEndSelection, this);
      this.checkForStartSelection = g(this.checkForStartSelection, this);
      this.clearViewerHideTimer = g(this.clearViewerHideTimer, this);
      this.startViewerHideTimer = g(this.startViewerHideTimer, this);
      this.showViewer = g(this.showViewer, this);
      this.onEditorSubmit = g(this.onEditorSubmit, this);
      this.onEditorHide = g(this.onEditorHide, this);
      this.showEditor = g(this.showEditor, this);
      var u, x, y;
      t.__super__.constructor.apply(this, arguments);
      this.plugins = {};
      if (!t.supported()) {
        return this
      }
      this._setupDocumentEvents()._setupWrapper()._setupViewer()._setupEditor();
      y = this.html;
      for (u in y) {
        x = y[u];
        if (u !== "wrapper") {
          this[u] = h(x).appendTo(this.wrapper).hide()
        }
      }
    }
    t.prototype._setupWrapper = function() {
      this.wrapper = h(this.html.wrapper);
      this.element.find("script").remove();
      this.element.wrapInner(this.wrapper);
      this.wrapper = this.element.find(".annotator-wrapper");
      return this
    };
    t.prototype._setupViewer = function() {
      this.viewer = new t.Viewer();
      this.viewer.hide().on("edit", this.onEditAnnotation).on("delete", this.onDeleteAnnotation).addField({
        load: g(function(v, u) {
          h(v).escape(u.text || "");
          return this.publish("annotationViewerTextField", [v, u])
        }, this)
      }).element.appendTo(this.wrapper).bind({
        mouseover: this.clearViewerHideTimer,
        mouseout: this.startViewerHideTimer
      });
      return this
    };
    t.prototype._setupEditor = function() {
      this.editor = new t.Editor();
      this.editor.hide().on("hide", this.onEditorHide).on("save", this.onEditorSubmit).addField({
        type: "textarea",
        label: "Comments\u2026",
        load: function(v, u) {
          return h(v).find("textarea").val(u.text || "")
        },
        submit: function(v, u) {
          return u.text = h(v).find("textarea").val()
        }
      });
      this.editor.element.appendTo(this.wrapper);
      return this
    };
    t.prototype._setupDocumentEvents = function() {
      h(document).bind({
        mouseup: this.checkForEndSelection,
        mousedown: this.checkForStartSelection
      });
      return this
    };
    t.prototype.getSelectedRanges = function() {
      var x, v, u, w;
      w = a.getGlobal().getSelection();
      u = [];
      if (!w.isCollapsed) {
        u = (function() {
          var z, y;
          y = [];
          for (v = 0, z = w.rangeCount; 0 <= z ? v < z : v > z;0 <= z ? v++ : v--) {
            x = new p.BrowserRange(w.getRangeAt(v));
            y.push(x.normalize().limit(this.wrapper[0]))
          }
          return y
        }).call(this)
      }
      return h.grep(u, function(y) {
        return y
      })
    };
    t.prototype.createAnnotation = function() {
      var u;
      u = {};
      this.publish("beforeAnnotationCreated", [u]);
      return u
    };
    t.prototype.setupAnnotation = function(u, w) {
      var v, A, z, x, y, B;
      if (w == null) {
        w = true
      }
      u.ranges || (u.ranges = this.selectedRanges);
      A = (function() {
        var D, F, E, C;
        E = u.ranges;
        C = [];
        for (D = 0, F = E.length; D < F; D++) {
          z = E[D];
          x = p.sniff(z);
          C.push(x.normalize(this.wrapper[0]))
        }
        return C
      }).call(this);
      A = h.grep(A, function(C) {
        return C !== null
      });
      u.quote = [];
      u.ranges = [];
      u.highlights = [];
      for (y = 0, B = A.length; y < B; y++) {
        v = A[y];
        u.quote.push(h.trim(v.text()));
        u.ranges.push(v.serialize(this.wrapper[0], ".annotator-hl"));
        h.merge(u.highlights, this.highlightRange(v))
      }
      u.quote = u.quote.join(" / ");
      h(u.highlights).data("annotation", u);
      if (w) {
        this.publish("annotationCreated", [u])
      }
      return u
    };
    t.prototype.updateAnnotation = function(u) {
      this.publish("beforeAnnotationUpdated", [u]);
      this.publish("annotationUpdated", [u]);
      return u
    };
    t.prototype.deleteAnnotation = function(u) {
      var w, v, y, x;
      x = u.highlights;
      for (v = 0, y = x.length; v < y; v++) {
        w = x[v];
        h(w).replaceWith(w.childNodes)
      }
      this.publish("annotationDeleted", [u]);
      return u
    };
    t.prototype.loadAnnotations = function(v) {
      var w, u;
      if (v == null) {
        v = []
      }
      u = g(function(z) {
        var B, x, y, A;
        if (z == null) {
          z = []
        }
        x = z.splice(0, 10);
        for (y = 0, A = x.length; y < A; y++) {
          B = x[y];
          this.setupAnnotation(B, false)
        }
        if (z.length > 0) {
          return setTimeout((function() {
            return u(z)
          }), 100)
        } else {
          return this.publish("annotationsLoaded", [w])
        }
      }, this);
      w = v.slice();
      if (v.length) {
        u(v)
      }
      return this
    };
    t.prototype.dumpAnnotations = function() {
      if (this.plugins.Store) {
        return this.plugins.Store.dumpAnnotations()
      } else {
        return console.warn("Can't dump annotations without Store plugin.")
      }
    };
    t.prototype.highlightRange = function(v) {
      var u, w, x;
      return u = (function() {
        var z, B, A, y;
        A = v.textNodes();
        y = [];
        for (z = 0, B = A.length; z < B; z++) {
          w = A[z];
          x = this.hl.clone().show();
          y.push(h(w).wrap(x).parent().get(0))
        }
        return y
      }).call(this)
    };
    t.prototype.addPlugin = function(w, v) {
      var u, x;
      if (this.plugins[w]) {
        console.error("You cannot have more than one instance of any plugin.")
      } else {
        u = t.Plugin[w];
        if (typeof u === "function") {
          this.plugins[w] = new u(this.element[0], v);
          this.plugins[w].annotator = this;
          if (typeof(x = this.plugins[w]).pluginInit === "function") {
            x.pluginInit()
          }
        } else {
          console.error("Could not load " + w + " plugin. Have you included the appropriate <script> tag?")
        }
      }
      return this
    };
    t.prototype.showEditor = function(u, v) {
      this.editor.element.css(v);
      this.editor.load(u);
      return this
    };
    t.prototype.onEditorHide = function() {
      this.publish("annotationEditorHidden", [this.editor]);
      return this.ignoreMouseup = false
    };
    t.prototype.onEditorSubmit = function(u) {
      this.publish("annotationEditorSubmit", [this.editor, u]);
      if (u.ranges === void 0) {
        return this.setupAnnotation(u)
      } else {
        return this.updateAnnotation(u)
      }
    };
    t.prototype.showViewer = function(v, u) {
      this.viewer.element.css(u);
      this.viewer.load(v);
      return this.publish("annotationViewerShown", [this.viewer, v])
    };
    t.prototype.startViewerHideTimer = function() {
      if (!this.viewerHideTimer) {
        return this.viewerHideTimer = setTimeout(h.proxy(this.viewer.hide, this.viewer), 250)
      }
    };
    t.prototype.clearViewerHideTimer = function() {
      clearTimeout(this.viewerHideTimer);
      return this.viewerHideTimer = false
    };
    t.prototype.checkForStartSelection = function(u) {
      if (!(u && this.isAnnotator(u.target))) {
        this.startViewerHideTimer();
        return this.mouseIsDown = true
      }
    };
    t.prototype.checkForEndSelection = function(x) {
      var u, v, w, z, y;
      this.mouseIsDown = false;
      if (this.ignoreMouseup) {
        return
      }
      this.selectedRanges = this.getSelectedRanges();
      y = this.selectedRanges;
      for (w = 0, z = y.length; w < z; w++) {
        v = y[w];
        u = v.commonAncestor;
        if (this.isAnnotator(u)) {
          return
        }
      }
      if (x && this.selectedRanges.length) {
        return this.adder.css(a.mousePosition(x, this.wrapper[0])).show()
      } else {
        return this.adder.hide()
      }
    };
    t.prototype.isAnnotator = function(u) {
      return !!h(u).parents().andSelf().filter("[class^=annotator-]").not(this.wrapper).length
    };
    t.prototype.onHighlightMouseover = function(u) {
      var v;
      this.clearViewerHideTimer();
      if (this.mouseIsDown || this.viewer.isShown()) {
        return false
      }
      v = h(u.target).parents(".annotator-hl").andSelf().map(function() {
        return h(this).data("annotation")
      });
      return this.showViewer(h.makeArray(v), a.mousePosition(u, this.wrapper[0]))
    };
    t.prototype.onAdderMousedown = function(u) {
      if (u != null) {
        u.preventDefault()
      }
      return this.ignoreMouseup = true
    };
    t.prototype.onAdderClick = function(v) {
      var u;
      if (v != null) {
        v.preventDefault()
      }
      u = this.adder.position();
      this.adder.hide();
      return this.showEditor(this.createAnnotation(), u)
    };
    t.prototype.onEditAnnotation = function(u) {
      var v;
      v = this.viewer.element.position();
      this.viewer.hide();
      return this.showEditor(u, v)
    };
    t.prototype.onDeleteAnnotation = function(u) {
      this.viewer.hide();
      return this.deleteAnnotation(u)
    };
    return t
  })();
  o.Plugin = (function() {
    q(t, b);

    function t(v, u) {
      t.__super__.constructor.apply(this, arguments)
    }
    t.prototype.pluginInit = function() {};
    return t
  })();
  o.$ = h;
  o.supported = function() {
    return (function() {
      return !!this.getSelection
    })()
  };
  o.noConflict = function() {
    a.getGlobal().Annotator = e;
    return this
  };
  h.plugin("annotator", o);
  this.Annotator = o;
  o.Widget = (function() {
    q(t, b);
    t.prototype.classes = {
      hide: "annotator-hide",
      invert: {
        x: "annotator-invert-x",
        y: "annotator-invert-y"
      }
    };

    function t(v, u) {
      t.__super__.constructor.apply(this, arguments);
      this.classes = h.extend({}, o.Widget.prototype.classes, this.classes)
    }
    t.prototype.checkOrientation = function() {
      var x, y, u, w, v;
      this.resetOrientation();
      v = h(a.getGlobal());
      w = this.element.children(":first");
      y = w.offset();
      u = {
        top: v.scrollTop(),
        right: v.width() + v.scrollLeft()
      };
      x = {
        top: y.top,
        right: y.left + w.width()
      };
      if ((x.top - u.top) < 0) {
        this.invertY()
      }
      if ((x.right - u.right) > 0) {
        this.invertX()
      }
      return this
    };
    t.prototype.resetOrientation = function() {
      this.element.removeClass(this.classes.invert.x).removeClass(this.classes.invert.y);
      return this
    };
    t.prototype.invertX = function() {
      this.element.addClass(this.classes.invert.x);
      return this
    };
    t.prototype.invertY = function() {
      this.element.addClass(this.classes.invert.y);
      return this
    };
    return t
  })();
  o.Editor = (function() {
    q(t, o.Widget);
    t.prototype.events = {
      "form submit": "submit",
      ".annotator-save click": "submit",
      ".annotator-cancel click": "hide",
      ".annotator-cancel mouseover": "onCancelButtonMouseover",
      "textarea keydown": "processKeypress"
    };
    t.prototype.classes = {
      hide: "annotator-hide",
      focus: "annotator-focus"
    };
    t.prototype.html = '<div class="annotator-outer annotator-editor">\n  <form class="annotator-widget">\n    <ul class="annotator-listing"></ul>\n    <div class="annotator-controls">\n      <a href="#cancel" class="annotator-cancel">Cancel</a>\n      <a href="#save" class="annotator-save annotator-focus">Save</a>\n    </div>\n    <span class="annotator-resize"></span>\n  </form>\n</div>';
    t.prototype.options = {};

    function t(u) {
      this.onCancelButtonMouseover = g(this.onCancelButtonMouseover, this);
      this.processKeypress = g(this.processKeypress, this);
      this.submit = g(this.submit, this);
      this.load = g(this.load, this);
      this.hide = g(this.hide, this);
      this.show = g(this.show, this);
      t.__super__.constructor.call(this, h(this.html)[0], u);
      this.fields = [];
      this.annotation = {};
      this.setupDragabbles()
    }
    t.prototype.show = function(u) {
      if (u != null) {
        u.preventDefault()
      }
      this.element.removeClass(this.classes.hide);
      this.element.find(".annotator-save").addClass(this.classes.focus);
      this.element.find(":input:first").focus();
      return this.checkOrientation().publish("show")
    };
    t.prototype.hide = function(u) {
      if (u != null) {
        u.preventDefault()
      }
      this.element.addClass(this.classes.hide);
      return this.publish("hide")
    };
    t.prototype.load = function(u) {
      var w, v, y, x;
      this.annotation = u;
      this.publish("load", [this.annotation]);
      x = this.fields;
      for (v = 0, y = x.length; v < y; v++) {
        w = x[v];
        w.load(w.element, this.annotation)
      }
      return this.show()
    };
    t.prototype.submit = function(v) {
      var w, u, y, x;
      if (v != null) {
        v.preventDefault()
      }
      x = this.fields;
      for (u = 0, y = x.length; u < y; u++) {
        w = x[u];
        w.submit(w.element, this.annotation)
      }
      this.publish("save", [this.annotation]);
      return this.hide()
    };
    t.prototype.addField = function(v) {
      var w, x, u;
      x = h.extend({
        id: "annotator-field-" + (new Date()).getTime(),
        type: "input",
        label: "",
        load: function() {},
        submit: function() {}
      }, v);
      u = null;
      w = h('<li class="annotator-item" />');
      x.element = w[0];
      switch (x.type) {
      case "textarea":
        u = h("<textarea />");
        break;
      case "input":
      case "checkbox":
        u = h("<input />")
      }
      w.append(u);
      u.attr({
        id: x.id,
        placeholder: x.label
      });
      if (x.type === "checkbox") {
        u[0].type = "checkbox";
        w.addClass("annotator-checkbox");
        w.append(h("<label />", {
          "for": x.id,
          html: x.label
        }))
      }
      this.element.find("ul:first").append(w);
      this.fields.push(x);
      return x.element
    };
    t.prototype.checkOrientation = function() {
      var u, w, v;
      t.__super__.checkOrientation.apply(this, arguments);
      v = this.element.find("ul");
      u = this.element.find(".annotator-controls");
      w = function() {
        return v.children().each(function() {
          return h(this).parent().prepend(this)
        })
      };
      if (this.element.hasClass(this.classes.invert.y) && v.is(":first-child")) {
        u.insertBefore(v);
        w()
      } else {
        if (u.is(":first-child")) {
          u.insertAfter(v);
          w()
        }
      }
      return this
    };
    t.prototype.processKeypress = function(u) {
      if (u.keyCode === 27) {
        return this.hide()
      } else {
        if (u.keyCode === 13 && !u.shiftKey) {
          return this.submit()
        }
      }
    };
    t.prototype.onCancelButtonMouseover = function() {
      return this.element.find("." + this.classes.focus).removeClass(this.classes.focus)
    };
    t.prototype.setupDragabbles = function() {
      var w, D, z, u, y, B, x, v, A, C;
      u = null;
      w = this.classes;
      z = this.element;
      A = null;
      v = z.find(".annotator-resize");
      D = z.find(".annotator-controls");
      C = false;
      y = function(E) {
        if (E.target === this) {
          u = {
            element: this,
            top: E.pageY,
            left: E.pageX
          };
          A = z.find("textarea:first");
          h(window).bind({
            "mouseup.annotator-editor-resize": x,
            "mousemove.annotator-editor-resize": B
          });
          return E.preventDefault()
        }
      };
      x = function() {
        u = null;
        return h(window).unbind(".annotator-editor-resize")
      };
      B = g(function(I) {
        var J, G, F, E, H;
        if (u && C === false) {
          J = {
            top: I.pageY - u.top,
            left: I.pageX - u.left
          };
          if (u.element === v[0]) {
            E = A.outerHeight();
            H = A.outerWidth();
            G = z.hasClass(w.invert.x) ? -1 : 1;F = z.hasClass(w.invert.y) ? 1 : -1;A.height(E + (J.top * F));A.width(H + (J.left * G));
            if (A.outerHeight() !== E) {
              u.top = I.pageY
            }
            if (A.outerWidth() !== H) {
              u.left = I.pageX
            }
          } else {
            if (u.element === D[0]) {
              z.css({
                top: parseInt(z.css("top"), 10) + J.top,
                left: parseInt(z.css("left"), 10) + J.left
              });
              u.top = I.pageY;
              u.left = I.pageX
            }
          }
          C = true;
          return setTimeout(function() {
            return C = false
          }, 1000 / 60)
        }
      }, this);
      v.bind("mousedown", y);
      return D.bind("mousedown", y)
    };
    return t
  })();
  o.Viewer = (function() {
    q(t, o.Widget);
    t.prototype.events = {
      ".annotator-edit click": "onEditClick",
      ".annotator-delete click": "onDeleteClick"
    };
    t.prototype.classes = {
      hide: "annotator-hide",
      showControls: "annotator-visible"
    };
    t.prototype.html = {
      element: '<div class="annotator-outer annotator-viewer">\n  <ul class="annotator-widget annotator-listing"></ul>\n</div>',
      item: '<li class="annotator-annotation annotator-item">\n  <span class="annotator-controls">\n    <button class="annotator-edit">Edit</button>\n    <button class="annotator-delete">Delete</button>\n  </span>\n</li>'
    };

    function t(u) {
      this.onDeleteClick = g(this.onDeleteClick, this);
      this.onEditClick = g(this.onEditClick, this);
      this.load = g(this.load, this);
      this.hide = g(this.hide, this);
      this.show = g(this.show, this);
      t.__super__.constructor.call(this, h(this.html.element)[0], u);
      this.item = h(this.html.item)[0];
      this.fields = [];
      this.annotations = []
    }
    t.prototype.show = function(v) {
      var u;
      if (v != null) {
        v.preventDefault()
      }
      u = this.element.find(".annotator-controls").addClass(this.classes.showControls);
      setTimeout((g(function() {
        return u.removeClass(this.classes.showControls)
      }, this)), 500);
      this.element.removeClass(this.classes.hide);
      return this.checkOrientation().publish("show")
    };
    t.prototype.isShown = function() {
      return !this.element.hasClass(this.classes.hide)
    };
    t.prototype.hide = function(u) {
      if (u != null) {
        u.preventDefault()
      }
      this.element.addClass(this.classes.hide);
      return this.publish("hide")
    };
    t.prototype.load = function(C) {
      var z, B, G, H, F, A, E, I, D, y, w, u, J, x, v;
      this.annotations = C || [];
      D = this.element.find("ul:first").empty();
      x = this.annotations;
      for (y = 0, u = x.length; y < u; y++) {
        z = x[y];
        I = h(this.item).clone().appendTo(D).data("annotation", z);
        G = I.find(".annotator-controls");
        F = G.find(".annotator-edit");
        H = G.find(".annotator-delete");
        B = {
          showEdit: function() {
            return F.removeAttr("disabled")
          },
          hideEdit: function() {
            return F.attr("disabled", "disabled")
          },
          showDelete: function() {
            return H.removeAttr("disabled")
          },
          hideDelete: function() {
            return H.attr("disabled", "disabled")
          }
        };
        v = this.fields;
        for (w = 0, J = v.length; w < J; w++) {
          E = v[w];
          A = h(E.element).clone().appendTo(I)[0];
          E.load(A, z, B)
        }
      }
      this.publish("load", [this.annotations]);
      return this.show()
    };
    t.prototype.addField = function(u) {
      var v;
      v = h.extend({
        load: function() {}
      }, u);
      v.element = h("<div />")[0];
      this.fields.push(v);
      v.element;
      return this
    };
    t.prototype.onEditClick = function(u) {
      return this.onButtonClick(u, "edit")
    };
    t.prototype.onDeleteClick = function(u) {
      return this.onButtonClick(u, "delete")
    };
    t.prototype.onButtonClick = function(w, u) {
      var v;
      v = h(w.target).parents(".annotator-annotation");
      return this.publish(u, [v.data("annotation")])
    };
    return t
  })();
  o = o || {};
  o.Notification = (function() {
    q(t, b);
    t.prototype.events = {
      click: "hide"
    };
    t.prototype.options = {
      html: "<div class='annotator-notice'></div>",
      classes: {
        show: "annotator-notice-show",
        info: "annotator-notice-info",
        success: "annotator-notice-success",
        error: "annotator-notice-error"
      }
    };

    function t(u) {
      this.hide = g(this.hide, this);
      this.show = g(this.show, this);
      t.__super__.constructor.call(this, h(this.options.html).appendTo(document.body)[0], u)
    }
    t.prototype.show = function(v, u) {
      if (u == null) {
        u = o.Notification.INFO
      }
      h(this.element).addClass(this.options.classes.show).addClass(this.options.classes[u]).escape(v || "");
      setTimeout(this.hide, 5000);
      return this
    };
    t.prototype.hide = function() {
      h(this.element).removeClass(this.options.classes.show);
      return this
    };
    return t
  })();
  o.Notification.INFO = "show";
  o.Notification.SUCCESS = "success";
  o.Notification.ERROR = "error";
  h(function() {
    var t;
    t = new o.Notification;
    o.showNotification = t.show;
    return o.hideNotification = t.hide
  });
  o.Plugin.Tags = (function() {
    q(t, o.Plugin);

    function t() {
      this.setAnnotationTags = g(this.setAnnotationTags, this);
      this.updateField = g(this.updateField, this);
      t.__super__.constructor.apply(this, arguments)
    }
    t.prototype.field = null;
    t.prototype.input = null;
    t.prototype.pluginInit = function() {
      if (!o.supported()) {
        return
      }
      this.field = this.annotator.editor.addField({
        label: "Add some tags here\u2026",
        load: this.updateField,
        submit: this.setAnnotationTags
      });
      this.annotator.viewer.addField({
        load: this.updateViewer
      });
      if (this.annotator.plugins.Filter) {
        this.annotator.plugins.Filter.addFilter({
          label: "Tag",
          property: "tags",
          isFiltered: o.Plugin.Tags.filterCallback
        })
      }
      return this.input = h(this.field).find(":input")
    };
    t.prototype.parseTags = function(v) {
      var u;
      v = h.trim(v);
      u = [];
      if (v) {
        u = v.split(/\s+/)
      }
      return u
    };
    t.prototype.stringifyTags = function(u) {
      return u.join(" ")
    };
    t.prototype.updateField = function(w, u) {
      var v;
      v = "";
      if (u.tags) {
        v = this.stringifyTags(u.tags)
      }
      return this.input.val(v)
    };
    t.prototype.setAnnotationTags = function(v, u) {
      return u.tags = this.parseTags(this.input.val())
    };
    t.prototype.updateViewer = function(v, u) {
      v = h(v);
      if (u.tags && h.isArray(u.tags) && u.tags.length) {
        return v.addClass("annotator-tags").html(function() {
          var w;
          return w = h.map(u.tags, function(x) {
            return '<span class="annotator-tag">' + o.$.escape(x) + "</span>"
          }).join(" ")
        })
      } else {
        return v.remove()
      }
    };
    return t
  })();
  o.Plugin.Tags.filterCallback = function(z, B) {
    var y, x, w, C, v, u, t, A;
    if (B == null) {
      B = []
    }
    w = 0;
    x = [];
    if (z) {
      x = z.split(/\s+/g);
      for (v = 0, t = x.length; v < t; v++) {
        y = x[v];
        if (B.length) {
          for (u = 0, A = B.length; u < A; u++) {
            C = B[u];
            if (C.indexOf(y) !== -1) {
              w += 1
            }
          }
        }
      }
    }
    return w === x.length
  };
  n = function(u) {
    var y, t, x, v, w, z;
    v = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(.([0-9]+))?)?(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
    y = u.match(new RegExp(v));
    x = 0;
    t = new Date(y[1], 0, 1);
    if (y[3]) {
      t.setMonth(y[3] - 1)
    }
    if (y[5]) {
      t.setDate(y[5])
    }
    if (y[7]) {
      t.setHours(y[7])
    }
    if (y[8]) {
      t.setMinutes(y[8])
    }
    if (y[10]) {
      t.setSeconds(y[10])
    }
    if (y[12]) {
      t.setMilliseconds(Number("0." + y[12]) * 1000)
    }
    if (y[14]) {
      x = (Number(y[16]) * 60) + Number(y[17]);
      x *= (z = y[15] === "-") != null ? z : {
        1: -1
      }
    }
    x -= t.getTimezoneOffset();
    w = Number(t) + (x * 60 * 1000);
    t.setTime(Number(w));
    return t
  };
  o.Plugin.Auth = (function() {
    q(t, o.Plugin);
    t.prototype.options = {
      token: null,
      tokenUrl: "/auth/token",
      autoFetch: true
    };

    function t(v, u) {
      this.haveValidToken = g(this.haveValidToken, this);
      t.__super__.constructor.apply(this, arguments);
      this.element.data("annotator:auth", this);
      this.waitingForToken = [];
      if (this.options.token) {
        this.setToken(this.options.token)
      } else {
        this.requestToken()
      }
    }
    t.prototype.requestToken = function() {
      this.requestInProgress = true;
      return h.getJSON(this.options.tokenUrl, g(function(v, u, w) {
        if (u !== "success") {
          return console.error("Couldn't get auth token: " + u, w)
        } else {
          this.setToken(v);
          return this.requestInProgress = false
        }
      }, this))
    };
    t.prototype.setToken = function(v) {
      var u;
      this.token = v;
      if (this.haveValidToken()) {
        if (this.options.autoFetch) {
          this.refreshTimeout = setTimeout((g(function() {
            return this.requestToken()
          }, this)), (this.timeToExpiry() - 2) * 1000)
        }
        this.updateHeaders();
        u = [];
        while (this.waitingForToken.length > 0) {
          u.push(this.waitingForToken.pop().apply())
        }
        return u
      } else {
        console.warn("Didn't get a valid token.");
        if (this.options.autoFetch) {
          console.warn("Getting a new token in 10s.");
          return setTimeout((g(function() {
            return this.requestToken()
          }, this)), 10 * 1000)
        }
      }
    };
    t.prototype.haveValidToken = function() {
      var u;
      u = this.token && this.token.authToken && this.token.authTokenIssueTime && this.token.authTokenTTL && this.token.accountId && this.token.userId;
      return u && this.timeToExpiry() > 0
    };
    t.prototype.timeToExpiry = function() {
      var w, u, v, x;
      v = new Date().getTime() / 1000;
      u = n(this.token.authTokenIssueTime).getTime() / 1000;
      w = u + this.token.authTokenTTL;
      x = w - v;
      if (x > 0) {
        return x
      } else {
        return 0
      }
    };
    t.prototype.updateHeaders = function() {
      var u;
      u = this.element.data("annotator:headers");
      return this.element.data("annotator:headers", h.extend(u, {
        "x-annotator-auth-token": this.token.authToken,
        "x-annotator-auth-token-issue-time": this.token.authTokenIssueTime,
        "x-annotator-auth-token-ttl": this.token.authTokenTTL,
        "x-annotator-account-id": this.token.accountId,
        "x-annotator-user-id": this.token.userId
      }))
    };
    t.prototype.withToken = function(u) {
      if (!(u != null)) {
        return
      }
      if (this.haveValidToken()) {
        return u()
      } else {
        this.waitingForToken.push(u);
        if (!this.requestInProgress) {
          return this.requestToken()
        }
      }
    };
    return t
  })();
  o.Plugin.Store = (function() {
    q(t, o.Plugin);
    t.prototype.events = {
      annotationCreated: "annotationCreated",
      annotationDeleted: "annotationDeleted",
      annotationUpdated: "annotationUpdated"
    };
    t.prototype.options = {
      prefix: "/store",
      autoFetch: true,
      annotationData: {},
      loadFromSearch: false,
      urls: {
    	  /*	  
          create: "/annotations",
          read: "/annotations/:id",
          update: "/annotations/:id",
          destroy: "/annotations/:id",
          //search: "/search"
          search: "/annotations"
  */		
          create: "/annoCREATE/:id",
          read: "/annoREAD/:id",
          update: "/annoUPDATE/:id",
          destroy: "/annoDESTROY/:id",
          search: "/annoSEARCH/:id"

      }
    };

    function t(v, u) {
      this._onError = g(this._onError, this);
      this._onBeforeSend = g(this._onBeforeSend, this);
      this._onLoadAnnotationsFromSearch = g(this._onLoadAnnotationsFromSearch, this);
      this._onLoadAnnotations = g(this._onLoadAnnotations, this);
      this._getAnnotations = g(this._getAnnotations, this);
      t.__super__.constructor.apply(this, arguments);
      this.annotations = []
    }
    t.prototype.pluginInit = function() {
      var u;
      if (!o.supported()) {
        return
      }
      u = this.element.data("annotator:auth");
      if (u) {
        return u.withToken(this._getAnnotations)
      } else {
        return this._getAnnotations()
      }
    };
    t.prototype._getAnnotations = function() {
      if (this.options.loadFromSearch) {
        return this.loadAnnotationsFromSearch(this.options.loadFromSearch)
      } else {
        return this.loadAnnotations()
      }
    };
    t.prototype.annotationCreated = function(u) {
      if (f.call(this.annotations, u) < 0) {
        this.registerAnnotation(u);
        return this._apiRequest("create", u, g(function(v) {
          if (!(v.id != null)) {
            console.warn("Warning: No ID returned from server for annotation ", u)
          }
          return this.updateAnnotation(u, v)
        }, this))
      } else {
        return this.updateAnnotation(u, {})
      }
    };
    t.prototype.annotationUpdated = function(u) {
      if (f.call(this.annotations, u) >= 0) {
        return this._apiRequest("update", u, (g(function(v) {
          return this.updateAnnotation(u, v)
        }, this)))
      }
    };
    t.prototype.annotationDeleted = function(u) {
      if (f.call(this.annotations, u) >= 0) {
        return this._apiRequest("destroy", u, (g(function() {
          return this.unregisterAnnotation(u)
        }, this)))
      }
    };
    t.prototype.registerAnnotation = function(u) {
      return this.annotations.push(u)
    };
    t.prototype.unregisterAnnotation = function(u) {
      return this.annotations.splice(this.annotations.indexOf(u), 1)
    };
    t.prototype.updateAnnotation = function(u, v) {
      if (f.call(this.annotations, u) < 0) {
        console.error("Trying to update unregistered annotation!")
      } else {
        h.extend(u, v)
      }
      return h(u.highlights).data("annotation", u)
    };
    t.prototype.loadAnnotations = function() {
      return this._apiRequest("read", null, this._onLoadAnnotations)
    };
    t.prototype._onLoadAnnotations = function(u) {
      if (u == null) {
        u = []
      }
      this.annotations = u;
      return this.annotator.loadAnnotations(u.slice())
    };
    t.prototype.loadAnnotationsFromSearch = function(u) {
      return this._apiRequest("search", u, this._onLoadAnnotationsFromSearch)
    };
    t.prototype._onLoadAnnotationsFromSearch = function(u) {
      if (u == null) {
        u = {}
      }
      return this._onLoadAnnotations(u.rows || [])
    };
    t.prototype.dumpAnnotations = function() {
      var w, v, y, x, u;
      x = this.annotations;
      u = [];
      for (v = 0, y = x.length; v < y; v++) {
        w = x[v];
        u.push(JSON.parse(this._dataFor(w)))
      }
      return u
    };
    t.prototype._apiRequest = function(x, y, z) {
      var A, v, w, u;
      A = y && y.id;
      u = this._urlFor(x, A);
      v = this._apiRequestOptions(x, y, z);
      w = h.ajax(u, v);
      w._id = A;
      w._action = x;
      return w
    };
    t.prototype._apiRequestOptions = function(v, w, x) {
      var u;
      u = {
        type: this._methodFor(v),
        beforeSend: this._onBeforeSend,
        dataType: "json",
        success: x ||
        function() {},
        error: this._onError
      };
      if (v === "search") {
        u = h.extend(u, {
          data: w
        })
      } else {
        u = h.extend(u, {
          data: w && this._dataFor(w),
          contentType: "application/json; charset=utf-8"
        })
      }
      return u
    };
    t.prototype._urlFor = function(w, x) {
      var u, v;
      u = x != null ? "/" + x : "";v = this.options.prefix || "/";v += this.options.urls[w];v = v.replace(/\/:id/, u);
      return v
    };
    t.prototype._methodFor = function(v) {
      var u;
      u = {
        create: "POST",
        read: "GET",
        update: "PUT",
        destroy: "DELETE",
        search: "GET"
      };
      return u[v]
    };
    t.prototype._dataFor = function(u) {
      var v, w;
      w = u.highlights;
      delete u.highlights;
      h.extend(u, this.options.annotationData);
      v = JSON.stringify(u);
      if (w) {
        u.highlights = w
      }
      return v
    };
    t.prototype._onBeforeSend = function(y) {
      var x, v, w, u;
      x = this.element.data("annotator:headers");
      if (x) {
        u = [];
        for (v in x) {
          w = x[v];
          u.push(y.setRequestHeader(v, w))
        }
        return u
      }
    };
    t.prototype._onError = function(w) {
      var v, u;
      v = w._action;
      u = "Sorry we could not " + v + " this annotation";
      if (w._action === "search") {
        u = "Sorry we could not search the store for annotations"
      } else {
        if (w._action === "read" && !w._id) {
          u = "Sorry we could not " + v + " the annotations from the store"
        }
      }
      switch (w.status) {
      case 401:
        u = "Sorry you are not allowed to " + v + " this annotation";
        break;
      case 404:
        u = "Sorry we could not connect to the annotations store";
        break;
      case 500:
        u = "Sorry something went wrong with the annotation store"
      }
      o.showNotification(u, o.Notification.ERROR);
      return console.error("API request failed: '" + w.status + "'")
    };
    return t
  })();
  o.Plugin.Filter = (function() {
    q(t, o.Plugin);
    t.prototype.events = {
      ".annotator-filter-property input focus": "_onFilterFocus",
      ".annotator-filter-property input blur": "_onFilterBlur",
      ".annotator-filter-property input keyup": "_onFilterKeyup",
      ".annotator-filter-previous click": "_onPreviousClick",
      ".annotator-filter-next click": "_onNextClick",
      ".annotator-filter-clear click": "_onClearClick"
    };
    t.prototype.classes = {
      active: "annotator-filter-active",
      hl: {
        hide: "annotator-hl-filtered",
        active: "annotator-hl-active"
      }
    };
    t.prototype.html = {
      element: '<div class="annotator-filter">\n  <strong>Navigate:</strong>\n  <span class="annotator-filter-navigation">\n    <button class="annotator-filter-previous">Previous</button>\n    <button class="annotator-filter-next">Next</button>\n  </span>\n  <strong>Filter by:</strong>\n</div>',
      filter: '<span class="annotator-filter-property">\n  <label></label>\n  <input/>\n  <button class="annotator-filter-clear">Clear</button>\n</span>'
    };
    t.prototype.options = {
      appendTo: "body",
      filters: [],
      addAnnotationFilter: true,
      isFiltered: function(v, x) {
        var u, w, z, y;
        if (!(v && x)) {
          return false
        }
        y = v.split(/\s*/);
        for (w = 0, z = y.length; w < z; w++) {
          u = y[w];
          if (x.indexOf(u) === -1) {
            return false
          }
        }
        return true
      }
    };

    function t(v, u) {
      this._onPreviousClick = g(this._onPreviousClick, this);
      this._onNextClick = g(this._onNextClick, this);
      this._onFilterKeyup = g(this._onFilterKeyup, this);
      this._onFilterBlur = g(this._onFilterBlur, this);
      this._onFilterFocus = g(this._onFilterFocus, this);
      this.updateHighlights = g(this.updateHighlights, this);
      v = h(this.html.element).appendTo(this.options.appendTo);
      t.__super__.constructor.call(this, v, u);
      this.filter = h(this.html.filter);
      this.filters = [];
      this.current = 0
    }
    t.prototype.pluginInit = function() {
      var v, u, x, w;
      w = this.options.filters;
      for (u = 0, x = w.length; u < x; u++) {
        v = w[u];
        this.addFilter(v)
      }
      this.updateHighlights();
      this._setupListeners()._insertSpacer();
      if (this.options.addAnnotationFilter === true) {
        return this.addFilter({
          label: "Annotation",
          property: "text"
        })
      }
    };
    t.prototype._insertSpacer = function() {
      var v, u;
      u = h("html");
      v = parseInt(u.css("padding-top"), 10) || 0;
      u.css("padding-top", v + this.element.outerHeight());
      return this
    };
    t.prototype._setupListeners = function() {
      var w, u, v, x;
      u = ["annotationsLoaded", "annotationCreated", "annotationUpdated", "annotationDeleted"];
      for (v = 0, x = u.length; v < x; v++) {
        w = u[v];
        this.annotator.subscribe(w, this.updateHighlights)
      }
      return this
    };
    t.prototype.addFilter = function(u) {
      var w, v;
      v = h.extend({
        label: "",
        property: "",
        isFiltered: this.options.isFiltered
      }, u);
      if (!((function() {
        var y, A, z, x;
        z = this.filters;
        x = [];
        for (y = 0, A = z.length; y < A; y++) {
          w = z[y];
          if (w.property === v.property) {
            x.push(w)
          }
        }
        return x
      }).call(this)).length) {
        v.id = "annotator-filter-" + v.property;
        v.annotations = [];
        v.element = this.filter.clone().appendTo(this.element);
        v.element.find("label").html(v.label).attr("for", v.id);
        v.element.find("input").attr({
          id: v.id,
          placeholder: "Filter by " + v.label + "\u2026"
        });
        v.element.find("button").hide();
        v.element.data("filter", v);
        this.filters.push(v)
      }
      return this
    };
    t.prototype.updateFilter = function(x) {
      var u, z, v, y, w, B, A;
      x.annotations = [];
      this.updateHighlights();
      this.resetHighlights();
      v = h.trim(x.element.find("input").val());
      if (v) {
        z = this.highlights.map(function() {
          return h(this).data("annotation")
        });
        A = h.makeArray(z);
        for (w = 0, B = A.length; w < B; w++) {
          u = A[w];
          y = u[x.property];
          if (x.isFiltered(v, y)) {
            x.annotations.push(u)
          }
        }
        return this.filterHighlights()
      }
    };
    t.prototype.updateHighlights = function() {
      this.highlights = this.annotator.element.find(".annotator-hl:visible");
      return this.filtered = this.highlights.not(this.classes.hl.hide)
    };
    t.prototype.filterHighlights = function() {
      var v, x, y, B, z, A, C, u, w;
      v = h.grep(this.filters, function(D) {
        return !!D.annotations.length
      });
      B = ((w = v[0]) != null ? w.annotations : void 0) || [];
      if (v.length > 1) {
        y = [];
        h.each(v, function() {
          return h.merge(y, this.annotations)
        });
        C = [];
        B = [];
        h.each(y, function() {
          if (h.inArray(this, C) === -1) {
            return C.push(this)
          } else {
            return B.push(this)
          }
        })
      }
      z = this.highlights;
      for (A = 0, u = B.length; A < u; A++) {
        x = B[A];
        z = z.not(x.highlights)
      }
      z.addClass(this.classes.hl.hide);
      this.filtered = this.highlights.not(this.classes.hl.hide);
      return this
    };
    t.prototype.resetHighlights = function() {
      this.highlights.removeClass(this.classes.hl.hide);
      this.filtered = this.highlights;
      return this
    };
    t.prototype._onFilterFocus = function(v) {
      var u;
      u = h(v.target);
      u.parent().addClass(this.classes.active);
      return u.next("button").show()
    };
    t.prototype._onFilterBlur = function(v) {
      var u;
      if (!v.target.value) {
        u = h(v.target);
        u.parent().removeClass(this.classes.active);
        return u.next("button").hide()
      }
    };
    t.prototype._onFilterKeyup = function(v) {
      var u;
      u = h(v.target).parent().data("filter");
      if (u) {
        return this.updateFilter(u)
      }
    };
    t.prototype._findNextHighlight = function(z) {
      var v, w, B, A, y, x, u, C;
      if (!this.highlights.length) {
        return this
      }
      x = z ? 0 : -1;C = z ? -1 : 0;u = z ? "lt" : "gt";v = this.highlights.not("." + this.classes.hl.hide);B = v.filter("." + this.classes.hl.active);
      if (!B.length) {
        B = v.eq(x)
      }
      w = B.data("annotation");A = v.index(B[0]);y = v.filter(":" + u + "(" + A + ")").not(w.highlights).eq(C);
      if (!y.length) {
        y = v.eq(C)
      }
      return this._scrollToHighlight(y.data("annotation").highlights)
    };
    t.prototype._onNextClick = function(u) {
      return this._findNextHighlight()
    };
    t.prototype._onPreviousClick = function(u) {
      return this._findNextHighlight(true)
    };
    t.prototype._scrollToHighlight = function(u) {
      u = h(u);
      this.highlights.removeClass(this.classes.hl.active);
      u.addClass(this.classes.hl.active);
      return h("html, body").animate({
        scrollTop: u.offset().top - (this.element.height() + 20)
      }, 150)
    };
    t.prototype._onClearClick = function(u) {
      return h(u.target).prev("input").val("").keyup().blur()
    };
    return t
  })();
  o.Plugin.Markdown = (function() {
    q(t, o.Plugin);
    t.prototype.events = {
      annotationViewerTextField: "updateTextField"
    };

    function t(v, u) {
      this.updateTextField = g(this.updateTextField, this);
      if ((typeof Showdown !== "undefined" && Showdown !== null ? Showdown.converter : void 0) != null) {
        t.__super__.constructor.apply(this, arguments);
        this.converter = new Showdown.converter()
      } else {
        console.error("To use the Markdown plugin, you must include Showdown into the page first.")
      }
    }
    t.prototype.updateTextField = function(v, u) {
      var w;
      w = o.$.escape(u.text || "");
      return h(v).html(this.convert(w))
    };
    t.prototype.convert = function(u) {
      return this.converter.makeHtml(u)
    };
    return t
  })();
  o.Plugin.Unsupported = (function() {
    q(t, o.Plugin);

    function t() {
      t.__super__.constructor.apply(this, arguments)
    }
    t.prototype.options = {
      message: "Sorry your current browser does not support the Annotator"
    };
    t.prototype.pluginInit = function() {
      if (!o.supported()) {
        return h(g(function() {
          o.showNotification(this.options.message);
          if ((window.XMLHttpRequest === void 0) && (ActiveXObject !== void 0)) {
            return h("html").addClass("ie6")
          }
        }, this))
      }
    };
    return t
  })();
  o.Plugin.Permissions = (function() {
    q(t, o.Plugin);
    t.prototype.events = {
      beforeAnnotationCreated: "addFieldsToAnnotation"
    };
    t.prototype.options = {
      showViewPermissionsCheckbox: true,
      showEditPermissionsCheckbox: true,
      userId: function(u) {
        return u
      },
      userString: function(u) {
        return u
      },
      userAuthorize: function(u, v) {
        return this.userId(u) === v
      },
      user: "",
      permissions: {
        read: [],
        update: [],
        "delete": [],
        admin: []
      }
    };

    function t(v, u) {
      this.updateViewer = g(this.updateViewer, this);
      this.updateAnnotationPermissions = g(this.updateAnnotationPermissions, this);
      this.updatePermissionsField = g(this.updatePermissionsField, this);
      this.addFieldsToAnnotation = g(this.addFieldsToAnnotation, this);
      t.__super__.constructor.apply(this, arguments);
      if (this.options.user) {
        this.setUser(this.options.user);
        delete this.options.user
      }
    }
    t.prototype.pluginInit = function() {
      var u, v;
      if (!o.supported()) {
        return
      }
      v = this;
      u = function(x, w) {
        return function(z, y) {
          return v[x].call(v, w, z, y)
        }
      };
      if (this.options.showViewPermissionsCheckbox === true) {
        this.annotator.editor.addField({
          type: "checkbox",
          label: "Allow anyone to <strong>view</strong> this annotation",
          load: u("updatePermissionsField", "read"),
          submit: u("updateAnnotationPermissions", "read")
        })
      }
      if (this.options.showEditPermissionsCheckbox === true) {
        this.annotator.editor.addField({
          type: "checkbox",
          label: "Allow anyone to <strong>edit</strong> this annotation",
          load: u("updatePermissionsField", "update"),
          submit: u("updateAnnotationPermissions", "update")
        })
      }
      this.annotator.viewer.addField({
        load: this.updateViewer
      });
      if (this.annotator.plugins.Filter) {
        return this.annotator.plugins.Filter.addFilter({
          label: "User",
          property: "user",
          isFiltered: g(function(y, x) {
            var w, z, B, A;
            x = this.options.userString(x);
            if (!(y && x)) {
              return false
            }
            A = y.split(/\s*/);
            for (z = 0, B = A.length; z < B; z++) {
              w = A[z];
              if (x.indexOf(w) === -1) {
                return false
              }
            }
            return true
          }, this)
        })
      }
    };
    t.prototype.setUser = function(u) {
      return this.user = u
    };
    t.prototype.addFieldsToAnnotation = function(u) {
      if (u) {
        u.permissions = this.options.permissions;
        if (this.user) {
          return u.user = this.user
        }
      }
    };
    t.prototype.authorize = function(y, u, v) {
      var w, z, x, A;
      if (v === void 0) {
        v = this.user
      }
      if (u.permissions) {
        z = u.permissions[y] || [];
        if (z.length === 0) {
          return true
        }
        for (x = 0, A = z.length; x < A; x++) {
          w = z[x];
          if (this.options.userAuthorize.call(this.options, v, w)) {
            return true
          }
        }
        return false
      } else {
        if (u.user) {
          return v && this.options.userId(v) === u.user
        }
      }
      return true
    };
    t.prototype.updatePermissionsField = function(w, y, u) {
      var x, v;
      y = h(y).show();
      v = y.find("input").removeAttr("disabled");
      if (!this.authorize("admin", u)) {
        y.hide()
      }
      if (this.authorize(w, u || {}, null)) {
        v.attr("checked", "checked");
        x = {
          permissions: this.options.permissions
        };
        if (this.authorize(w, x, null)) {
          return v.attr("disabled", "disabled")
        }
      } else {
        return v.removeAttr("checked")
      }
    };
    t.prototype.updateAnnotationPermissions = function(w, x, u) {
      var y, v;
      if (!u.permissions) {
        u.permissions = this.options.permissions
      }
      y = w + "-permissions";
      if (h(x).find("input").is(":checked")) {
        h.data(u, y, u.permissions[w]);
        return u.permissions[w] = []
      } else {
        v = h.data(u, y);
        if (v) {
          return u.permissions[w] = v
        }
      }
    };
    t.prototype.updateViewer = function(x, u, w) {
      var v, y;
      x = h(x);
      y = this.options.userString(u.user);
      if (u.user && y && typeof y === "string") {
        v = o.$.escape(this.options.userString(u.user));
        x.html(v).addClass("annotator-user")
      } else {
        x.remove()
      }
      if (u.permissions) {
        if (!this.authorize("update", u)) {
          w.hideEdit()
        }
        if (!this.authorize("delete", u)) {
          return w.hideDelete()
        }
      } else {
        if (u.user && !this.authorize(null, u)) {
          w.hideEdit();
          return w.hideDelete()
        }
      }
    };
    return t
  })();
  o.prototype.setupPlugins = function(w, E) {
    var B, D, u, t, y, v, A, C, z, x;
    if (w == null) {
      w = {}
    }
    if (E == null) {
      E = {}
    }
    z = a.getGlobal();
    y = {
      Tags: {},
      Filter: {
        filters: [{
          label: "User",
          property: "user"},
        {
          label: "Tags",
          property: "tags"}]
      },
      Unsupported: {}
    };
    if (z.Showdown) {
      y.Markdown = {}
    }
    A = w.userId, C = w.userName, B = w.accountId, D = w.authToken;
    if (A && C && B && D) {
      v = z.location.href.split(/#|\?/).shift() || "";
      h.extend(y, {
        Store: {
          prefix: w.storeUri || "http://rrd.llabor.co.cc/rrdsaas/F/h_t_t_p_://annotateit.org/api",
          annotationData: {
            uri: v
          },
          loadFromSearch: {
            uri: v,
            all_fields: 1
          }
        },
        Permissions: {
          user: {
            id: w.userId,
            name: w.userName
          },
          permissions: {
            read: [w.userId],
            update: [w.userId],
            "delete": [w.userId],
            admin: [w.userId]
          },
          userId: function(F) {
            if (F != null ? F.id : void 0) {
              return F.id
            } else {
              return ""
            }
          },
          userString: function(F) {
            if (F != null ? F.name : void 0) {
              return F.name
            } else {
              return ""
            }
          }
        }
      });
      this.element.data({
        "annotator:headers": {
          "X-Annotator-User-Id": w.userId,
          "X-Annotator-Account-Id": w.accountId,
          "X-Annotator-Auth-Token": w.authToken
        }
      })
    }
    h.extend(y, E);
    x = [];
    for (u in y) {
      if (!k.call(y, u)) {
        continue
      }
      t = y[u];
      if (t !== null && t !== false) {
        x.push(this.addPlugin(u, t))
      }
    }
    return x
  }
}).call(this);