/*!
 * elFinder - file manager for web
 * Version 2.0 rc1 (2012-04-10)
 * http://elfinder.org
 * 
 * Copyright 2009-2012, Studio 42
 * Licensed under a 3 clauses BSD license
 */ (function (a) {
    window.elFinder = function (b, c) {
        this.time("load");
        var d = this,
            b = a(b),
            e = a("<div/>").append(b.contents()),
            f = b.attr("style"),
            g = b.attr("id") || "",
            h = "elfinder-" + (g || Math.random().toString().substr(2, 7)),
            i = "mousedown." + h,
            j = "keydown." + h,
            k = "keypress." + h,
            l = !0,
            m = !0,
            n = ["enable", "disable", "load", "open", "reload", "select", "add", "remove", "change", "dblclick", "getfile", "lockfiles", "unlockfiles", "dragstart", "dragstop"],
            o = {}, p = "",
            q = {
                path: "",
                url: "",
                tmbUrl: "",
                disabled: [],
                separator: "/",
                archives: [],
                extract: [],
                copyOverwrite: !0,
                tmb: !1
            }, r = {}, s = [],
            t = {}, u = {}, v = [],
            w = [],
            x = [],
            y = new d.command(d),
            z = "auto",
            A = 400,
            B = a(document.createElement("audio")).hide().appendTo("body")[0],
            C, D = function (b) {
                if (b.init) r = {};
                else for (var c in r) r.hasOwnProperty(c) && r[c].mime != "directory" && r[c].phash == p && a.inArray(c, w) === -1 && delete r[c];
                p = b.cwd.hash, E(b.files), r[p] || E([b.cwd]), d.lastDir(p)
            }, E = function (a) {
                var b = a.length,
                    c;
                while (b--) c = a[b], c.name && c.hash && c.mime && (r[c.hash] = c)
            }, F = function (b) {
                var c = b.keyCode,
                    e = !! b.ctrlKey || !! b.metaKey;
                l && (a.each(u, function (a, f) {
                    f.type == b.type && f.keyCode == c && f.shiftKey == b.shiftKey && f.ctrlKey == e && f.altKey == b.altKey && (b.preventDefault(), b.stopPropagation(), f.callback(b, d), d.debug("shortcut-exec", a + " : " + f.description))
                }), c == 9 && b.preventDefault())
            }, G = new Date,
            H, I;
        this.api = null, this.newAPI = !1, this.oldAPI = !1, this.OS = navigator.userAgent.indexOf("Mac") !== -1 ? "mac" : navigator.userAgent.indexOf("Win") !== -1 ? "win" : "other", this.options = a.extend(!0, {}, this._options, c || {}), c.ui && (this.options.ui = c.ui), c.commands && (this.options.commands = c.commands), c.uiOptions && c.uiOptions.toolbar && (this.options.uiOptions.toolbar = c.uiOptions.toolbar), a.extend(this.options.contextmenu, c.contextmenu), this.requestType = /^(get|post)$/i.test(this.options.requestType) ? this.options.requestType.toLowerCase() : "get", this.customData = a.isPlainObject(this.options.customData) ? this.options.customData : {}, this.id = g, this.uploadURL = c.urlUpload || c.url, this.namespace = h, this.lang = this.i18[this.options.lang] && this.i18[this.options.lang].messages ? this.options.lang : "en", I = this.lang == "en" ? this.i18.en : a.extend(!0, {}, this.i18.en, this.i18[this.lang]), this.direction = I.direction, this.messages = I.messages, this.dateFormat = this.options.dateFormat || I.dateFormat, this.fancyFormat = this.options.fancyDateFormat || I.fancyDateFormat, this.today = (new Date(G.getFullYear(), G.getMonth(), G.getDate())).getTime() / 1e3, this.yesterday = this.today - 86400, H = this.options.UTCDate ? "UTC" : "", this.getHours = "get" + H + "Hours", this.getMinutes = "get" + H + "Minutes", this.getSeconds = "get" + H + "Seconds", this.getDate = "get" + H + "Date", this.getDay = "get" + H + "Day", this.getMonth = "get" + H + "Month", this.getFullYear = "get" + H + "FullYear", this.cssClass = "ui-helper-reset ui-helper-clearfix ui-widget ui-widget-content ui-corner-all elfinder elfinder-" + (this.direction == "rtl" ? "rtl" : "ltr") + " " + this.options.cssClass, this.storage = function () {
            try {
                return "localStorage" in window && window.localStorage !== null ? d.localStorage : d.cookie
            } catch (a) {
                return d.cookie
            }
        }(), this.notifyDelay = this.options.notifyDelay > 0 ? parseInt(this.options.notifyDelay) : 500, this.draggable = {
            appendTo: "body",
            addClasses: !0,
            delay: 30,
            revert: !0,
            refreshPositions: !0,
            cursor: "move",
            cursorAt: {
                left: 50,
                top: 47
            },
            drag: function (a, b) {
                b.helper.toggleClass("elfinder-drag-helper-plus", a.shiftKey || a.ctrlKey || a.metaKey)
            },
            stop: function () {
                d.trigger("focus").trigger("dragstop")
            },
            helper: function (b, c) {
                var e = this.id ? a(this) : a(this).parents("[id]:first"),
                    f = a('<div class="elfinder-drag-helper"><span class="elfinder-drag-helper-icon-plus"/></div>'),
                    g = function (a) {
                        return '<div class="elfinder-cwd-icon ' + d.mime2class(a) + ' ui-corner-all"/>'
                    }, h, i;
                return d.trigger("dragstart", {
                    target: e[0],
                    originalEvent: b
                }), h = e.is("." + d.res("class", "cwdfile")) ? d.selected() : [d.navId2Hash(e.attr("id"))], f.append(g(r[h[0]].mime)).data("files", h), (i = h.length) > 1 && f.append(g(r[h[i - 1]].mime) + '<span class="elfinder-drag-num">' + i + "</span>"), f
            }
        }, this.droppable = {
            tolerance: "pointer",
            accept: ".elfinder-cwd-file-wrapper,.elfinder-navbar-dir,.elfinder-cwd-file",
            hoverClass: this.res("class", "adroppable"),
            drop: function (b, c) {
                var e = a(this),
                    f = a.map(c.helper.data("files") || [], function (a) {
                        return a || null
                    }),
                    g = [],
                    h = "class",
                    i, j, k, l;
                e.is("." + d.res(h, "cwd")) ? j = p : e.is("." + d.res(h, "cwdfile")) ? j = e.attr("id") : e.is("." + d.res(h, "navdir")) && (j = d.navId2Hash(e.attr("id"))), i = f.length;
                while (i--) l = f[i], l != j && r[l].phash != j && g.push(l);
                g.length && (c.helper.hide(), d.clipboard(g, !(b.ctrlKey || b.shiftKey || b.metaKey)), d.exec("paste", j).always(function () {
                    d.clipboard([])
                }), d.trigger("drop", {
                    files: f
                }))
            }
        }, this.enabled = function () {
            return b.is(":visible") && l
        }, this.visible = function () {
            return b.is(":visible")
        }, this.root = function (a) {
            var b = r[a || p],
                c;
            while (b && b.phash) b = r[b.phash];
            if (b) return b.hash;
            while (c in r && r.hasOwnProperty(c)) {
                b = r[c];
                if (!b.phash && !b.mime == "directory" && b.read) return b.hash
            }
            return ""
        }, this.cwd = function () {
            return r[p] || {}
        }, this.option = function (a) {
            return q[a] || ""
        }, this.file = function (a) {
            return r[a]
        }, this.files = function () {
            return a.extend(!0, {}, r)
        }, this.parents = function (a) {
            var b = [],
                c;
            while (c = this.file(a)) b.unshift(c.hash), a = c.phash;
            return b
        }, this.path2array = function (a) {
            var b, c = [];
            while (a && (b = r[a]) && b.hash) c.unshift(b.name), a = b.phash;
            return c
        }, this.path = function (a) {
            return r[a] && r[a].path ? r[a].path : this.path2array(a).join(q.separator)
        }, this.url = function (b) {
            var c = r[b];
            if (!c || !c.read) return "";
            if (c.url) return c.url;
            if (q.url) return q.url + a.map(this.path2array(b), function (a) {
                return encodeURIComponent(a)
            }).slice(1).join("/");
            var d = a.extend({}, this.customData, {
                cmd: "file",
                target: c.hash
            });
            return this.oldAPI && (d.cmd = "open", d.current = c.phash), this.options.url + (this.options.url.indexOf("?") === -1 ? "?" : "&") + a.param(d, !0)
        }, this.tmb = function (b) {
            var c = r[b],
                d = c && c.tmb && c.tmb != 1 ? q.tmbUrl + c.tmb : "";
            return d && (a.browser.opera || a.browser.msie) && (d += "?_=" + (new Date).getTime()), d
        }, this.selected = function () {
            return s.slice(0)
        }, this.selectedFiles = function () {
            return a.map(s, function (a) {
                return r[a] || null
            })
        }, this.fileByName = function (a, b) {
            var c;
            for (c in r) if (r.hasOwnProperty(c) && r[c].phash == b && r[c].name == a) return r[c]
        }, this.validResponse = function (a, b) {
            return b.error || this.rules[this.rules[a] ? a : "defaults"](b)
        }, this.request = function (b) {
            var c = this,
                d = this.options,
                e = a.Deferred(),
                f = a.extend({}, d.customData, {
                    mimes: d.onlyMimes
                }, b.data || b),
                g = f.cmd,
                h = !b.preventDefault && !b.preventFail,
                i = !b.preventDefault && !b.preventDone,
                j = a.extend({}, b.notify),
                k = !! b.raw,
                l = b.syncOnFail,
                m, b = a.extend({
                    url: d.url,
                    async: !0,
                    type: this.requestType,
                    dataType: "json",
                    cache: !1,
                    data: f
                }, b.options || {}),
                n = function (b) {
                    b.warning && c.error(b.warning), g == "open" && D(a.extend(!0, {}, b)), b.removed && b.removed.length && c.remove(b), b.added && b.added.length && c.add(b), b.changed && b.changed.length && c.change(b), c.trigger(g, b), b.sync && c.sync()
                }, o = function (a, b) {
                    var c;
                    switch (b) {
                        case "abort":
                            c = a.quiet ? "" : ["errConnect", "errAbort"];
                            break;
                        case "timeout":
                            c = ["errConnect", "errTimeout"];
                            break;
                        case "parsererror":
                            c = ["errResponse", "errDataNotJSON"];
                            break;
                        default:
                            a.status == 403 ? c = ["errConnect", "errAccess"] : a.status == 404 ? c = ["errConnect", "errNotFound"] : c = "errConnect"
                    }
                    e.reject(c, a, b)
                }, p = function (b) {
                    if (k) return e.resolve(b);
                    if (!b) return e.reject(["errResponse", "errDataEmpty"], r);
                    if (!a.isPlainObject(b)) return e.reject(["errResponse", "errDataNotJSON"], r);
                    if (b.error) return e.reject(b.error, r);
                    if (!c.validResponse(g, b)) return e.reject("errResponse", r);
                    b = c.normalize(b), c.api || (c.api = b.api || 1, c.newAPI = c.api >= 2, c.oldAPI = !c.newAPI), b.options && (q = a.extend({}, q, b.options)), e.resolve(b), b.debug && c.debug("backend-debug", b.debug)
                }, r, s;
            i && e.done(n), e.fail(function (a) {
                a && (h ? c.error(a) : c.debug("error", c.i18n(a)))
            });
            if (!g) return e.reject("errCmdReq");
            l && e.fail(function (a) {
                a && c.sync()
            }), j.type && j.cnt && (m = setTimeout(function () {
                c.notify(j), e.always(function () {
                    j.cnt = -(parseInt(j.cnt) || 0), c.notify(j)
                })
            }, c.notifyDelay), e.always(function () {
                clearTimeout(m)
            }));
            if (g == "open") while (s = x.pop())!s.isRejected() && !s.isResolved() && (s.quiet = !0, s.abort());
            return delete b.preventFail, r = this.transport.send(b).fail(o).done(p), g == "open" && (x.unshift(r), e.always(function () {
                var b = a.inArray(r, x);
                b !== -1 && x.splice(b, 1)
            })), e
        }, this.diff = function (b) {
            var c = {}, d = [],
                e = [],
                f = [],
                g = function (a) {
                    var b = f.length;
                    while (b--) if (f[b].hash == a) return !0
                };
            return a.each(b, function (a, b) {
                c[b.hash] = b
            }), a.each(r, function (a, b) {
                !c[a] && e.push(a)
            }), a.each(c, function (b, c) {
                var e = r[b];
                e ? a.each(c, function (a) {
                    if (c[a] != e[a]) return f.push(c), !1
                }) : d.push(c)
            }), a.each(e, function (b, d) {
                var h = r[d],
                    i = h.phash;
                i && h.mime == "directory" && a.inArray(i, e) === -1 && c[i] && !g(i) && f.push(c[i])
            }), {
                added: d,
                removed: e,
                changed: f
            }
        }, this.sync = function () {
            var b = this,
                c = a.Deferred().done(function () {
                    b.trigger("sync")
                }),
                d = {
                    data: {
                        cmd: "open",
                        init: 1,
                        target: p,
                        tree: this.ui.tree ? 1 : 0
                    },
                    preventDefault: !0
                }, e = {
                    data: {
                        cmd: "parents",
                        target: p
                    },
                    preventDefault: !0
                };
            return a.when(this.request(d), this.request(e)).fail(function (a) {
                c.reject(a), a && b.request({
                    data: {
                        cmd: "open",
                        target: b.lastDir(""),
                        tree: 1,
                        init: 1
                    },
                    notify: {
                        type: "open",
                        cnt: 1,
                        hideCnt: !0
                    }
                })
            }).done(function (a, d) {
                var e = b.diff(a.files.concat(d && d.tree ? d.tree : []));
                return e.removed.length && b.remove(e), e.added.length && b.add(e), e.changed.length && b.change(e), c.resolve(e)
            }), c
        }, this.upload = function (a) {
            return this.transport.upload(a, this)
        }, this.bind = function (a, b) {
            var c;
            if (typeof b == "function") {
                a = ("" + a).toLowerCase().split(/\s+/);
                for (c = 0; c < a.length; c++) t[a[c]] === void 0 && (t[a[c]] = []), t[a[c]].push(b)
            }
            return this
        }, this.unbind = function (a, b) {
            var c = t[("" + a).toLowerCase()] || [],
                d = c.indexOf(b);
            return d > -1 && c.splice(d, 1), b = null, this
        }, this.trigger = function (b, c) {
            var b = b.toLowerCase(),
                d = t[b] || [],
                e, f;
            this.debug("event-" + b, c);
            if (d.length) {
                b = a.Event(b);
                for (e = 0; e < d.length; e++) {
                    b.data = a.extend(!0, {}, c);
                    try {
                        if (d[e](b, this) === !1 || b.isDefaultPrevented()) {
                            this.debug("event-stoped", b.type);
                            break
                        }
                    } catch (g) {
                        window.console && window.console.log && window.console.log(g)
                    }
                }
            }
            return this
        }, this.shortcut = function (b) {
            var c, d, e, f, g;
            if (this.options.allowShortcuts && b.pattern && a.isFunction(b.callback)) {
                c = b.pattern.toUpperCase().split(/\s+/);
                for (f = 0; f < c.length; f++) d = c[f], g = d.split("+"), e = (e = g.pop()).length == 1 ? e > 0 ? e : e.charCodeAt(0) : a.ui.keyCode[e], e && !u[d] && (u[d] = {
                    keyCode: e,
                    altKey: a.inArray("ALT", g) != -1,
                    ctrlKey: a.inArray("CTRL", g) != -1,
                    shiftKey: a.inArray("SHIFT", g) != -1,
                    type: b.type || "keydown",
                    callback: b.callback,
                    description: b.description,
                    pattern: d
                })
            }
            return this
        }, this.shortcuts = function () {
            var b = [];
            return a.each(u, function (a, c) {
                b.push([c.pattern, d.i18n(c.description)])
            }), b
        }, this.clipboard = function (b, c) {
            var d = function () {
                return a.map(v, function (a) {
                    return a.hash
                })
            };
            return b !== void 0 && (v.length && this.trigger("unlockfiles", {
                files: d()
            }), w = [], v = a.map(b || [], function (a) {
                var b = r[a];
                return b ? (w.push(a), {
                    hash: a,
                    phash: b.phash,
                    name: b.name,
                    mime: b.mime,
                    read: b.read,
                    locked: b.locked,
                    cut: !! c
                }) : null
            }), this.trigger("changeclipboard", {
                clipboard: v.slice(0, v.length)
            }), c && this.trigger("lockfiles", {
                files: d()
            })), v.slice(0, v.length)
        }, this.isCommandEnabled = function (b) {
            return this._commands[b] ? a.inArray(b, q.disabled) === -1 : !1
        }, this.exec = function (b, c, d) {
            return this._commands[b] && this.isCommandEnabled(b) ? this._commands[b].exec(c, d) : a.Deferred().reject("No such command")
        }, this.dialog = function (c, d) {
            return a("<div/>").append(c).appendTo(b).elfinderdialog(d)
        }, this.getUI = function (a) {
            return this.ui[a] || b
        }, this.command = function (a) {
            return a === void 0 ? this._commands : this._commands[a]
        }, this.resize = function (a, c) {
            b.css("width", a).height(c).trigger("resize"), this.trigger("resize", {
                width: b.width(),
                height: b.height()
            })
        }, this.restoreSize = function () {
            this.resize(z, A)
        }, this.show = function () {
            b.show(), this.enable().trigger("show")
        }, this.hide = function () {
            this.disable().trigger("hide"), b.hide()
        }, this.destroy = function () {
            b && b[0].elfinder && (this.trigger("destroy").disable(), t = {}, u = {}, a(document).add(b).unbind("." + this.namespace), d.trigger = function () {}, b.children().remove(), b.append(e.contents()).removeClass(this.cssClass).attr("style", f), b[0].elfinder = null, C && clearInterval(C))
        }, this.setSort(this.options.sort, this.options.sortDirect);
        if (!(a.fn.selectable && a.fn.draggable && a.fn.droppable)) return alert(this.i18n("errJqui"));
        if (!b.length) return alert(this.i18n("errNode"));
        if (!this.options.url) return alert(this.i18n("errURL"));
        a.extend(a.ui.keyCode, {
            F1: 112,
            F2: 113,
            F3: 114,
            F4: 115,
            F5: 116,
            F6: 117,
            F7: 118,
            F8: 119,
            F9: 120
        }), this.dragUpload = !1, this.xhrUpload = typeof XMLHttpRequestUpload != "undefined" && typeof File != "undefined" && typeof FormData != "undefined", this.transport = {}, typeof this.options.transport == "object" && (this.transport = this.options.transport, typeof this.transport.init == "function" && this.transport.init(this)), typeof this.transport.send != "function" && (this.transport.send = function (b) {
            return a.ajax(b)
        }), this.transport.upload == "iframe" ? this.transport.upload = a.proxy(this.uploads.iframe, this) : typeof this.transport.upload == "function" ? this.dragUpload = !! this.options.dragUploadAllow : this.xhrUpload ? (this.transport.upload = a.proxy(this.uploads.xhr, this), this.dragUpload = !0) : this.transport.upload = a.proxy(this.uploads.iframe, this), this.error = function () {
            var a = arguments[0];
            return arguments.length == 1 && typeof a == "function" ? d.bind("error", a) : d.trigger("error", {
                error: a
            })
        }, a.each(["enable", "disable", "load", "open", "reload", "select", "add", "remove", "change", "dblclick", "getfile", "lockfiles", "unlockfiles", "dragstart", "dragstop", "search", "searchend", "viewchange"], function (b, c) {
            d[c] = function () {
                var b = arguments[0];
                return arguments.length == 1 && typeof b == "function" ? d.bind(c, b) : d.trigger(c, a.isPlainObject(b) ? b : {})
            }
        }), this.enable(function () {
            !l && d.visible() && d.ui.overlay.is(":hidden") && (l = !0, a("texarea:focus,input:focus,button").blur(), b.removeClass("elfinder-disabled"))
        }).disable(function () {
            m = l, l = !1, b.addClass("elfinder-disabled")
        }).open(function () {
            s = []
        }).select(function (b) {
            s = a.map(b.data.selected || b.data.value || [], function (a) {
                return r[a] ? a : null
            })
        }).error(function (b) {
            var c = {
                cssClass: "elfinder-dialog-error",
                title: d.i18n(d.i18n("error")),
                resizable: !1,
                destroyOnClose: !0,
                buttons: {}
            };
            c.buttons[d.i18n(d.i18n("btnClose"))] = function () {
                a(this).elfinderdialog("close")
            }, d.dialog('<span class="elfinder-dialog-icon elfinder-dialog-icon-error"/>' + d.i18n(b.data.error), c)
        }).bind("tree parents", function (a) {
            E(a.data.tree || [])
        }).bind("tmb", function (b) {
            a.each(b.data.images || [], function (a, b) {
                r[a] && (r[a].tmb = b)
            })
        }).add(function (a) {
            E(a.data.added || [])
        }).change(function (b) {
            a.each(b.data.changed || [], function (b, c) {
                var d = c.hash;
                r[d] = r[d] ? a.extend(r[d], c) : c
            })
        }).remove(function (b) {
            var c = b.data.removed || [],
                d = c.length,
                e = function (b) {
                    var c = r[b];
                    c && (c.mime == "directory" && c.dirs && a.each(r, function (a, c) {
                        c.phash == b && e(a)
                    }), delete r[b])
                };
            while (d--) e(c[d])
        }).bind("search", function (a) {
            E(a.data.files)
        }).bind("rm", function (b) {
            var c = B.canPlayType && B.canPlayType('audio/wav; codecs="1"');
            c && c != "" && c != "no" && a(B).html('<source src="./sounds/rm.wav" type="audio/wav">')[0].play()
        }), a.each(this.options.handlers, function (a, b) {
            d.bind(a, b)
        }), this.history = new this.history(this), typeof this.options.getFileCallback == "function" && this.commands.getfile && (this.bind("dblclick", function (a) {
            a.preventDefault(), d.exec("getfile").fail(function () {
                d.exec("open")
            })
        }), this.shortcut({
            pattern: "enter",
            description: this.i18n("cmdgetfile"),
            callback: function () {
                d.exec("getfile").fail(function () {
                    d.exec(d.OS == "mac" ? "rename" : "open")
                })
            }
        }).shortcut({
            pattern: "ctrl+enter",
            description: this.i18n(this.OS == "mac" ? "cmdrename" : "cmdopen"),
            callback: function () {
                d.exec(d.OS == "mac" ? "rename" : "open")
            }
        })), this._commands = {}, a.isArray(this.options.commands) || (this.options.commands = []), a.each(["open", "reload", "back", "forward", "up", "home", "info", "quicklook", "getfile", "help"], function (b, c) {
            a.inArray(c, d.options.commands) === -1 && d.options.commands.push(c)
        }), a.each(this.options.commands, function (b, c) {
            var e = d.commands[c];
            a.isFunction(e) && !d._commands[c] && (e.prototype = y, d._commands[c] = new e, d._commands[c].setup(c, d.options.commandsOptions[c] || {}))
        }), b.addClass(this.cssClass).bind(i, function () {
            !l && d.enable()
        }), this.ui = {
            workzone: a("<div/>").appendTo(b).elfinderworkzone(this),
            navbar: a("<div/>").appendTo(b).elfindernavbar(this, this.options.uiOptions.navbar || {}),
            contextmenu: a("<div/>").appendTo(b).elfindercontextmenu(this),
            overlay: a("<div/>").appendTo(b).elfinderoverlay({
                show: function () {
                    d.disable()
                },
                hide: function () {
                    m && d.enable()
                }
            }),
            cwd: a("<div/>").appendTo(b).elfindercwd(this),
            notify: this.dialog("", {
                cssClass: "elfinder-dialog-notify",
                position: {
                    top: "12px",
                    right: "12px"
                },
                resizable: !1,
                autoOpen: !1,
                title: "&nbsp;",
                width: 280
            }),
            statusbar: a('<div class="ui-widget-header ui-helper-clearfix ui-corner-bottom elfinder-statusbar"/>').hide().appendTo(b)
        }, a.each(this.options.ui || [], function (c, e) {
            var f = "elfinder" + e,
                g = d.options.uiOptions[e] || {};
            !d.ui[e] && a.fn[f] && (d.ui[e] = a("<" + (g.tag || "div") + "/>").appendTo(b)[f](d, g))
        }), b[0].elfinder = this, this.options.resizable && a.fn.resizable && b.resizable({
            handles: "se",
            minWidth: 300,
            minHeight: 200
        }), this.options.width && (z = this.options.width), this.options.height && (A = parseInt(this.options.height)), d.resize(z, A), a(document).bind("click." + this.namespace, function (c) {
            l && !a(c.target).closest(b).length && d.disable()
        }).bind(j + " " + k, F), this.trigger("init").request({
            data: {
                cmd: "open",
                target: d.lastDir(),
                init: 1,
                tree: this.ui.tree ? 1 : 0
            },
            preventDone: !0,
            notify: {
                type: "open",
                cnt: 1,
                hideCnt: !0
            },
            freeze: !0
        }).fail(function () {
            d.trigger("fail").disable().lastDir(""), t = {}, u = {}, a(document).add(b).unbind("." + this.namespace), d.trigger = function () {}
        }).done(function (b) {
            d.load().debug("api", d.api), b = a.extend(!0, {}, b), D(b), d.trigger("open", b)
        }), this.one("load", function () {
            b.trigger("resize"), d.options.sync > 1e3 && (C = setInterval(function () {
                d.sync()
            }, d.options.sync))
        })
    }, elFinder.prototype = {
        res: function (a, b) {
            return this.resources[a] && this.resources[a][b]
        },
        i18: {
            en: {
                translator: "",
                language: "English",
                direction: "ltr",
                dateFormat: "d.m.Y H:i",
                fancyDateFormat: "$1 H:i",
                messages: {}
            },
            months: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
            monthsShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
            days: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
            daysShort: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
        },
        kinds: {
            unknown: "Unknown",
            directory: "Folder",
            symlink: "Alias",
            "symlink-broken": "AliasBroken",
            "application/x-empty": "TextPlain",
            "application/postscript": "Postscript",
            "application/vnd.ms-office": "MsOffice",
            "application/vnd.ms-word": "MsWord",
            "application/vnd.ms-excel": "MsExcel",
            "application/vnd.ms-powerpoint": "MsPP",
            "application/pdf": "PDF",
            "application/xml": "XML",
            "application/vnd.oasis.opendocument.text": "OO",
            "application/x-shockwave-flash": "AppFlash",
            "application/flash-video": "Flash video",
            "application/x-bittorrent": "Torrent",
            "application/javascript": "JS",
            "application/rtf": "RTF",
            "application/rtfd": "RTF",
            "application/x-font-ttf": "TTF",
            "application/x-font-otf": "OTF",
            "application/x-rpm": "RPM",
            "application/x-web-config": "TextPlain",
            "application/xhtml+xml": "HTML",
            "application/docbook+xml": "DOCBOOK",
            "application/x-awk": "AWK",
            "application/x-gzip": "GZIP",
            "application/x-bzip2": "BZIP",
            "application/zip": "ZIP",
            "application/x-zip": "ZIP",
            "application/x-rar": "RAR",
            "application/x-tar": "TAR",
            "application/x-7z-compressed": "7z",
            "application/x-jar": "JAR",
            "text/plain": "TextPlain",
            "text/x-php": "PHP",
            "text/html": "HTML",
            "text/javascript": "JS",
            "text/css": "CSS",
            "text/rtf": "RTF",
            "text/rtfd": "RTF",
            "text/x-c": "C",
            "text/x-csrc": "C",
            "text/x-chdr": "CHeader",
            "text/x-c++": "CPP",
            "text/x-c++src": "CPP",
            "text/x-c++hdr": "CPPHeader",
            "text/x-shellscript": "Shell",
            "application/x-csh": "Shell",
            "text/x-python": "Python",
            "text/x-java": "Java",
            "text/x-java-source": "Java",
            "text/x-ruby": "Ruby",
            "text/x-perl": "Perl",
            "text/x-sql": "SQL",
            "text/xml": "XML",
            "text/x-comma-separated-values": "CSV",
            "image/x-ms-bmp": "BMP",
            "image/jpeg": "JPEG",
            "image/gif": "GIF",
            "image/png": "PNG",
            "image/tiff": "TIFF",
            "image/x-targa": "TGA",
            "image/vnd.adobe.photoshop": "PSD",
            "image/xbm": "XBITMAP",
            "image/pxm": "PXM",
            "audio/mpeg": "AudioMPEG",
            "audio/midi": "AudioMIDI",
            "audio/ogg": "AudioOGG",
            "audio/mp4": "AudioMPEG4",
            "audio/x-m4a": "AudioMPEG4",
            "audio/wav": "AudioWAV",
            "audio/x-mp3-playlist": "AudioPlaylist",
            "video/x-dv": "VideoDV",
            "video/mp4": "VideoMPEG4",
            "video/mpeg": "VideoMPEG",
            "video/x-msvideo": "VideoAVI",
            "video/quicktime": "VideoMOV",
            "video/x-ms-wmv": "VideoWM",
            "video/x-flv": "VideoFlash",
            "video/x-matroska": "VideoMKV",
            "video/ogg": "VideoOGG"
        },
        rules: {
            defaults: function (b) {
                return !b || b.added && !a.isArray(b.added) || b.removed && !a.isArray(b.removed) || b.changed && !a.isArray(b.changed) ? !1 : !0
            },
            open: function (b) {
                return b && b.cwd && b.files && a.isPlainObject(b.cwd) && a.isArray(b.files)
            },
            tree: function (b) {
                return b && b.tree && a.isArray(b.tree)
            },
            parents: function (b) {
                return b && b.tree && a.isArray(b.tree)
            },
            tmb: function (b) {
                return b && b.images && (a.isPlainObject(b.images) || a.isArray(b.images))
            },
            upload: function (b) {
                return b && (a.isPlainObject(b.added) || a.isArray(b.added))
            },
            search: function (b) {
                return b && b.files && a.isArray(b.files)
            }
        },
        sorts: {
            nameDirsFirst: 1,
            kindDirsFirst: 2,
            sizeDirsFirst: 3,
            dateDirsFirst: 4,
            name: 5,
            kind: 6,
            size: 7,
            date: 8
        },
        setSort: function (a, b) {
            this.sort = this.sorts[a] || 1, this.sortDirect = b == "asc" || b == "desc" ? b : "asc", this.trigger("sortchange")
        },
        commands: {},
        parseUploadData: function (b) {
            var c;
            if (!a.trim(b)) return {
                error: ["errResponse", "errDataEmpty"]
            };
            try {
                c = a.parseJSON(b)
            } catch (d) {
                return {
                    error: ["errResponse", "errDataNotJSON"]
                }
            }
            return this.validResponse("upload", c) ? (c = this.normalize(c), c.removed = a.map(c.added || [], function (a) {
                return a.hash
            }), c) : {
                error: ["errResponse"]
            }
        },
        iframeCnt: 0,
        uploads: {
            iframe: function (b, c) {
                var d = c ? c : this,
                    e = b.input,
                    f = a.Deferred().fail(function (a) {
                        a && d.error(a)
                    }).done(function (a) {
                        a.warning && d.error(a.warning), a.removed && d.remove(a), a.added && d.add(a), a.changed && d.change(a), d.trigger("upload", a), a.sync && d.sync()
                    }),
                    g = "iframe-" + d.namespace + ++d.iframeCnt,
                    h = a('<form action="' + d.uploadURL + '" method="post" enctype="multipart/form-data" encoding="multipart/form-data" target="' + g + '" style="display:none"><input type="hidden" name="cmd" value="upload" /></form>'),
                    i = a.browser.msie,
                    j = function () {
                        o && clearTimeout(o), n && clearTimeout(n), m && d.notify({
                            type: "upload",
                            cnt: -l
                        }), setTimeout(function () {
                            i && a('<iframe src="javascript:false;"/>').appendTo(h), h.remove(), k.remove()
                        }, 100)
                    }, k = a('<iframe src="' + (i ? "javascript:false;" : "about:blank") + '" name="' + g + '" style="position:absolute;left:-1000px;top:-1000px" />').bind("load", function () {
                        k.unbind("load").bind("load", function () {
                            var a = d.parseUploadData(k.contents().text());
                            j(), a.error ? f.reject(a.error) : f.resolve(a)
                        }), n = setTimeout(function () {
                            m = !0, d.notify({
                                type: "upload",
                                cnt: l
                            })
                        }, d.options.notifyDelay), d.options.iframeTimeout > 0 && (o = setTimeout(function () {
                            j(), f.reject([errors.connect, errors.timeout])
                        }, d.options.iframeTimeout)), h.submit()
                    }),
                    l, m, n, o;
                return e && a(e).is(":file") && a(e).val() ? (h.append(e), l = e.files ? e.files.length : 1, h.append('<input type="hidden" name="' + (d.newAPI ? "target" : "current") + '" value="' + d.cwd().hash + '"/>').append('<input type="hidden" name="html" value="1"/>').append(a(e).attr("name", "upload[]")), a.each(d.options.onlyMimes || [], function (a, b) {
                    h.append('<input type="hidden" name="mimes[]" value="' + b + '"/>')
                }), a.each(d.options.customData, function (a, b) {
                    h.append('<input type="hidden" name="' + a + '" value="' + b + '"/>')
                }), h.appendTo("body"), k.appendTo("body"), f) : f.reject()
            },
            xhr: function (b, c) {
                var d = c ? c : this,
                    e = a.Deferred().fail(function (a) {
                        a && d.error(a)
                    }).done(function (a) {
                        a.warning && d.error(a.warning), a.removed && d.remove(a), a.added && d.add(a), a.changed && d.change(a), d.trigger("upload", a), a.sync && d.sync()
                    }).always(function () {
                        m && clearTimeout(m), k && d.notify({
                            type: "upload",
                            cnt: -i,
                            progress: 100 * i
                        })
                    }),
                    f = new XMLHttpRequest,
                    g = new FormData,
                    h = b.input ? b.input.files : b.files,
                    i = h.length,
                    j = 5,
                    k = !1,
                    l = function () {
                        return setTimeout(function () {
                            k = !0, d.notify({
                                type: "upload",
                                cnt: i,
                                progress: j * i
                            })
                        }, d.options.notifyDelay)
                    }, m;
                if (!i) return e.reject();
                f.addEventListener("error", function () {
                    e.reject("errConnect")
                }, !1), f.addEventListener("abort", function () {
                    e.reject(["errConnect", "errAbort"])
                }, !1), f.addEventListener("load", function () {
                    var a = f.status,
                        b;
                    if (a > 500) return e.reject("errResponse");
                    if (a != 200) return e.reject("errConnect");
                    if (f.readyState != 4) return e.reject(["errConnect", "errTimeout"]);
                    if (!f.responseText) return e.reject(["errResponse", "errDataEmpty"]);
                    b = d.parseUploadData(f.responseText), b.error ? e.reject(b.error) : e.resolve(b)
                }, !1), f.upload.addEventListener("progress", function (a) {
                    var b = j,
                        c;
                    a.lengthComputable && (c = parseInt(a.loaded * 100 / a.total), c > 0 && !m && (m = l()), c - b > 4 && (j = c, k && d.notify({
                        type: "upload",
                        cnt: 0,
                        progress: (j - b) * i
                    })))
                }, !1), f.open("POST", d.uploadURL, !0), g.append("cmd", "upload"), g.append(d.newAPI ? "target" : "current", d.cwd().hash), a.each(d.options.customData, function (a, b) {
                    g.append(a, b)
                }), a.each(d.options.onlyMimes, function (a, b) {
                    g.append("mimes[" + a + "]", b)
                }), a.each(h, function (a, b) {
                    g.append("upload[]", b)
                }), f.onreadystatechange = function () {
                    f.readyState == 4 && f.status == 0 && e.reject(["errConnect", "errAbort"])
                }, f.send(g);
                if (!a.browser.safari || !b.files) m = l();
                return e
            }
        },
        one: function (b, c) {
            var d = this,
                e = a.proxy(c, function (a) {
                    return setTimeout(function () {
                        d.unbind(a.type, e)
                    }, 3), c.apply(this, arguments)
                });
            return this.bind(b, e)
        },
        localStorage: function (a, b) {
            var c = window.localStorage;
            return a = "elfinder-" + a + this.id, b !== void 0 && c.setItem(a, b), c.getItem(a) || ""
        },
        cookie: function (b, c) {
            var d, e, f, g;
            b = "elfinder-" + b + this.id;
            if (c === void 0) {
                if (document.cookie && document.cookie != "") {
                    f = document.cookie.split(";"), b += "=";
                    for (g = 0; g < f.length; g++) {
                        f[g] = a.trim(f[g]);
                        if (f[g].substring(0, b.length) == b) return decodeURIComponent(f[g].substring(b.length))
                    }
                }
                return ""
            }
            return e = a.extend({}, this.options.cookie), c === null && (c = "", e.expires = -1), typeof e.expires == "number" && (d = new Date, d.setTime(d.getTime() + e.expires * 864e5), e.expires = d), document.cookie = b + "=" + encodeURIComponent(c) + "; expires=" + e.expires.toUTCString() + (e.path ? "; path=" + e.path : "") + (e.domain ? "; domain=" + e.domain : "") + (e.secure ? "; secure" : ""), c
        },
        lastDir: function (a) {
            return this.options.rememberLastDir ? this.storage("lastdir", a) : ""
        },
        _node: a("<span/>"),
        escape: function (a) {
            return this._node.text(a).html()
        },
        normalize: function (b) {
            var c = function (a) {
                return a && a.hash && a.name && a.mime ? (a.mime == "application/x-empty" && (a.mime = "text/plain"), a) : null
            };
            return b.files && (b.files = a.map(b.files, c)), b.tree && (b.tree = a.map(b.tree, c)), b.added && (b.added = a.map(b.added, c)), b.changed && (b.changed = a.map(b.changed, c)), b.api && (b.init = !0), b
        },
        compare: function (a, b) {
            var c = this.sort,
                d = this.sortDirect == "asc",
                e = d ? a : b,
                f = d ? b : a,
                g = this.mime2kind(e.mime).toLowerCase(),
                h = this.mime2kind(f.mime).toLowerCase(),
                i = a.mime == "directory",
                j = b.mime == "directory",
                k = e.name.toLowerCase(),
                l = f.name.toLowerCase(),
                m = i ? 0 : parseInt(e.size) || 0,
                n = j ? 0 : parseInt(f.size) || 0,
                o = e.ts || e.date || "",
                p = f.ts || f.date || "";
            if (c <= 4) {
                if (i && !j) return -1;
                if (!i && j) return 1
            }
            return c != 2 && c != 6 || g == h ? c != 3 && c != 7 || m == n ? c != 4 && c != 8 || o == p ? e.name.localeCompare(f.name) : o > p ? 1 : -1 : m > n ? 1 : -1 : g.localeCompare(h)
        },
        sortFiles: function (b) {
            return b.sort(a.proxy(this.compare, this))
        },
        notify: function (b) {
            var c = b.type,
                d = this.messages["ntf" + c] ? this.i18n("ntf" + c) : this.i18n("ntfsmth"),
                e = this.ui.notify,
                f = e.children(".elfinder-notify-" + c),
                g = '<div class="elfinder-notify elfinder-notify-{type}"><span class="elfinder-dialog-icon elfinder-dialog-icon-{type}"/><span class="elfinder-notify-msg">{msg}</span> <span class="elfinder-notify-cnt"/><div class="elfinder-notify-progressbar"><div class="elfinder-notify-progress"/></div></div>',
                h = b.cnt,
                i = b.progress >= 0 && b.progress <= 100 ? b.progress : 0,
                j, k, l;
            return c ? (f.length || (f = a(g.replace(/\{type\}/g, c).replace(/\{msg\}/g, d)).appendTo(e).data("cnt", 0), i && f.data({
                progress: 0,
                total: 0
            })), j = h + parseInt(f.data("cnt")), j > 0 ? (!b.hideCnt && f.children(".elfinder-notify-cnt").text("(" + j + ")"), e.is(":hidden") && e.elfinderdialog("open"), f.data("cnt", j), i < 100 && (k = f.data("total")) >= 0 && (l = f.data("progress")) >= 0 && (k = h + parseInt(f.data("total")), l = i + l, i = parseInt(l / k), f.data({
                progress: l,
                total: k
            }), e.find(".elfinder-notify-progress").animate({
                width: (i < 100 ? i : 100) + "%"
            }, 20))) : (f.remove(), !e.children().length && e.elfinderdialog("close")), this) : this
        },
        confirm: function (b) {
            var c = !1,
                d = {
                    cssClass: "elfinder-dialog-confirm",
                    modal: !0,
                    resizable: !1,
                    title: this.i18n(b.title || "confirmReq"),
                    buttons: {},
                    close: function () {
                        !c && b.cancel.callback(), a(this).elfinderdialog("destroy")
                    }
                }, e = this.i18n("apllyAll"),
                f, g;
            return b.reject && (d.buttons[this.i18n(b.reject.label)] = function () {
                b.reject.callback( !! g && !! g.prop("checked")), c = !0, a(this).elfinderdialog("close")
            }), d.buttons[this.i18n(b.accept.label)] = function () {
                b.accept.callback( !! g && !! g.prop("checked")), c = !0, a(this).elfinderdialog("close")
            }, d.buttons[this.i18n(b.cancel.label)] = function () {
                a(this).elfinderdialog("close")
            }, b.all && (b.reject && (d.width = 370), d.create = function () {
                g = a('<input type="checkbox" />'), a(this).next().children().before(a("<label>" + e + "</label>").prepend(g))
            }, d.open = function () {
                var b = a(this).next(),
                    c = parseInt(b.children(":first").outerWidth() + b.children(":last").outerWidth());
                c > parseInt(b.width()) && a(this).closest(".elfinder-dialog").width(c + 30)
            }), this.dialog('<span class="elfinder-dialog-icon elfinder-dialog-icon-confirm"/>' + this.i18n(b.text), d)
        },
        uniqueName: function (a, b) {
            var c = 0,
                d = "",
                e, f;
            a = this.i18n(a), b = b || this.cwd().hash, (e = a.indexOf(".txt")) != -1 && (d = ".txt", a = a.substr(0, e)), f = a + d;
            if (!this.fileByName(f, b)) return f;
            while (c < 1e4) {
                f = a + " " + ++c + d;
                if (!this.fileByName(f, b)) return f
            }
            return a + Math.random() + d
        },
        i18n: function () {
            var b = this,
                c = this.messages,
                d = [],
                e = [],
                f = function (a) {
                    var c;
                    if (a.indexOf("#") === 0) if (c = b.file(a.substr(1))) return c.name;
                    return a
                }, g, h, i;
            for (g = 0; g < arguments.length; g++) {
                i = arguments[g];
                if (typeof i == "string") d.push(f(i));
                else if (a.isArray(i)) for (h = 0; h < i.length; h++) typeof i[h] == "string" && d.push(f(i[h]))
            }
            for (g = 0; g < d.length; g++) {
                if (a.inArray(g, e) !== -1) continue;
                i = d[g], i = c[i] || i, i = i.replace(/\$(\d+)/g, function (a, b) {
                    return b = g + parseInt(b), b > 0 && d[b] && e.push(b), d[b] || ""
                }), d[g] = i
            }
            return a.map(d, function (b, c) {
                return a.inArray(c, e) === -1 ? b : null
            }).join("<br>")
        },
        mime2class: function (a) {
            var b = "elfinder-cwd-icon-";
            return a = a.split("/"), b + a[0] + (a[0] != "image" && a[1] ? " " + b + a[1].replace(/(\.|\+)/g, "-") : "")
        },
        mime2kind: function (a) {
            var b = typeof a == "object" ? a.mime : a,
                c;
            a.alias ? c = "Alias" : this.kinds[b] ? c = this.kinds[b] : b.indexOf("text") === 0 ? c = "Text" : b.indexOf("image") === 0 ? c = "Image" : b.indexOf("audio") === 0 ? c = "Audio" : b.indexOf("video") === 0 ? c = "Video" : b.indexOf("application") === 0 ? c = "App" : c = b;
            return this.messages["kind" + c] ? this.i18n("kind" + c) : b;
            var b, c
        },
        formatDate: function (a, b) {
            var c = this,
                b = b || a.ts,
                d = c.i18,
                e, f, g, h, i, j, k, l, m, n, o;
            return c.options.clientFormatDate && b > 0 ? (e = new Date(b * 1e3), l = e[c.getHours](), m = l > 12 ? l - 12 : l, n = e[c.getMinutes](), o = e[c.getSeconds](), h = e[c.getDate](), i = e[c.getDay](), j = e[c.getMonth]() + 1, k = e[c.getFullYear](), f = b >= this.yesterday ? this.fancyFormat : this.dateFormat, g = f.replace(/[a-z]/gi, function (a) {
                switch (a) {
                    case "d":
                        return h > 9 ? h : "0" + h;
                    case "j":
                        return h;
                    case "D":
                        return c.i18n(d.daysShort[i]);
                    case "l":
                        return c.i18n(d.days[i]);
                    case "m":
                        return j > 9 ? j : "0" + j;
                    case "n":
                        return j;
                    case "M":
                        return c.i18n(d.monthsShort[j - 1]);
                    case "F":
                        return c.i18n(d.months[j - 1]);
                    case "Y":
                        return k;
                    case "y":
                        return ("" + k).substr(2);
                    case "H":
                        return l > 9 ? l : "0" + l;
                    case "G":
                        return l;
                    case "g":
                        return m;
                    case "h":
                        return m > 9 ? m : "0" + m;
                    case "a":
                        return l > 12 ? "pm" : "am";
                    case "A":
                        return l > 12 ? "PM" : "AM";
                    case "i":
                        return n > 9 ? n : "0" + n;
                    case "s":
                        return o > 9 ? o : "0" + o
                }
                return a
            }), b >= this.yesterday ? g.replace("$1", this.i18n(b >= this.today ? "Today" : "Yesterday")) : g) : a.date ? a.date.replace(/([a-z]+)\s/i, function (a, b) {
                return c.i18n(b) + " "
            }) : c.i18n("dateUnknown")
        },
        perms2class: function (a) {
            var b = "";
            return !a.read && !a.write ? b = "elfinder-na" : a.read ? a.write || (b = "elfinder-ro") : b = "elfinder-wo", b
        },
        formatPermissions: function (a) {
            var b = [];
            return a.read && b.push(this.i18n("read")), a.write && b.push(this.i18n("write")), b.length ? b.join(" " + this.i18n("and") + " ") : this.i18n("noaccess")
        },
        formatSize: function (a) {
            var b = 1,
                c = "b";
            return a == "unknown" ? this.i18n("unknown") : (a > 1073741824 ? (b = 1073741824, c = "GB") : a > 1048576 ? (b = 1048576, c = "MB") : a > 1024 && (b = 1024, c = "KB"), (a > 0 ? Math.round(a / b) : 0) + " " + c)
        },
        navHash2Id: function (a) {
            return "nav-" + a
        },
        navId2Hash: function (a) {
            return typeof a == "string" ? a.substr(4) : !1
        },
        log: function (a) {
            return window.console && window.console.log && window.console.log(a), this
        },
        debug: function (b, c) {
            var d = this.options.debug;
            return (d == "all" || d === !0 || a.isArray(d) && a.inArray(b, d) != -1) && window.console && window.console.log && window.console.log("elfinder debug: [" + b + "] [" + this.id + "]", c), this
        },
        time: function (a) {
            window.console && window.console.time && window.console.time(a)
        },
        timeEnd: function (a) {
            window.console && window.console.timeEnd && window.console.timeEnd(a)
        }
    }, elFinder.prototype.version = "2.0 rc1", a.fn.elfinder = function (a) {
        return a == "instance" ? this.getElFinder() : this.each(function () {
            var b = typeof a == "string" ? a : "";
            this.elfinder || new elFinder(this, typeof a == "object" ? a : {});
            switch (b) {
                case "close":
                case "hide":
                    this.elfinder.hide();
                    break;
                case "open":
                case "show":
                    this.elfinder.show();
                    break;
                case "destroy":
                    this.elfinder.destroy()
            }
        })
    }, a.fn.getElFinder = function () {
        var a;
        return this.each(function () {
            if (this.elfinder) return a = this.elfinder, !1
        }), a
    }, elFinder.prototype._options = {
        url: "",
        requestType: "get",
        transport: {},
        urlUpload: "",
        dragUploadAllow: "auto",
        iframeTimeout: 0,
        customData: {},
        handlers: {},
        lang: "en",
        cssClass: "",
        commands: ["open", "reload", "home", "up", "back", "forward", "getfile", "quicklook", "download", "rm", "duplicate", "rename", "mkdir", "mkfile", "upload", "copy", "cut", "paste", "edit", "extract", "archive", "search", "info", "view", "help", "resize", "sort"],
        commandsOptions: {
            getfile: {
                onlyURL: !0,
                multiple: !1,
                folders: !1,
                oncomplete: ""
            },
            upload: {
                ui: "uploadbutton"
            },
            quicklook: {
                autoplay: !0,
                jplayer: "extensions/jplayer"
            },
            edit: {
                mimes: [],
                editors: []
            },
            help: {
                view: ["about", "shortcuts", "help"]
            }
        },
        getFileCallback: null,
        ui: ["toolbar", "tree", "path", "stat"],
        uiOptions: {
            toolbar: [
                ["back", "forward"],
                ["mkdir", "mkfile", "upload"],
                ["open", "download", "getfile"],
                ["info"],
                ["quicklook"],
                ["copy", "cut", "paste"],
                ["rm"],
                ["duplicate", "rename", "edit", "resize"],
                ["extract", "archive"],
                ["search"],
                ["view", "sort"],
                ["help"]
            ],
            tree: {
                openRootOnLoad: !0,
                syncTree: !0
            },
            navbar: {
                minWidth: 150,
                maxWidth: 500
            }
        },
        onlyMimes: [],
        sort: "nameDirsFirst",
        sortDirect: "asc",
        clientFormatDate: !0,
        UTCDate: !1,
        dateFormat: "",
        fancyDateFormat: "",
        width: "auto",
        height: 400,
        resizable: !0,
        notifyDelay: 500,
        allowShortcuts: !0,
        rememberLastDir: !0,
        showFiles: 30,
        showThreshold: 50,
        validName: !1,
        sync: 0,
        loadTmbs: 5,
        cookie: {
            expires: 30,
            domain: "",
            path: "/",
            secure: !1
        },
        contextmenu: {
            navbar: ["open", "|", "copy", "cut", "paste", "duplicate", "|", "rm", "|", "info"],
            cwd: ["reload", "back", "|", "upload", "mkdir", "mkfile", "paste", "|", "sort", "|", "info"],
            files: ["getfile", "|", "open", "quicklook", "|", "download", "|", "copy", "cut", "paste", "duplicate", "|", "rm", "|", "edit", "rename", "resize", "|", "archive", "extract", "|", "info"]
        },
        debug: ["error", "warning", "event-destroy"]
    }, elFinder.prototype.history = function (b) {
        var c = this,
            d = !0,
            e = [],
            f, g = function () {
                e = [b.cwd().hash], f = 0, d = !0
            }, h = function (h) {
                return h && c.canForward() || !h && c.canBack() ? (d = !1, b.exec("open", e[h ? ++f : --f]).fail(g)) : a.Deferred().reject()
            };
        this.canBack = function () {
            return f > 0
        }, this.canForward = function () {
            return f < e.length - 1
        }, this.back = h, this.forward = function () {
            return h(!0)
        }, b.open(function (a) {
            var c = e.length,
                g = b.cwd().hash;
            d && (f >= 0 && c > f + 1 && e.splice(f + 1), e[e.length - 1] != g && e.push(g), f = e.length - 1), d = !0
        }).reload(g)
    }, elFinder.prototype.command = function (b) {
        this.fm = b, this.name = "", this.title = "", this.state = -1, this.alwaysEnabled = !1, this._disabled = !1, this.disableOnSearch = !1, this.updateOnSelect = !0, this._handlers = {
            enable: function () {
                this.update(void 0, this.value)
            },
            disable: function () {
                this.update(-1, this.value)
            },
            "open reload load": function (a) {
                this._disabled = !this.alwaysEnabled && !this.fm.isCommandEnabled(this.name), this.update(void 0, this.value), this.change()
            }
        }, this.handlers = {}, this.shortcuts = [], this.options = {
            ui: "button"
        }, this.setup = function (b, c) {
            var d = this,
                e = this.fm,
                f, g;
            this.name = b, this.title = e.messages["cmd" + b] ? e.i18n("cmd" + b) : b, this.options = a.extend({}, this.options, c), this.listeners = [], this.updateOnSelect && (this._handlers.select = function () {
                this.update(void 0, this.value)
            }), a.each(a.extend({}, d._handlers, d.handlers), function (b, c) {
                e.bind(b, a.proxy(c, d))
            });
            for (f = 0; f < this.shortcuts.length; f++) g = this.shortcuts[f], g.callback = a.proxy(g.callback || function () {
                this.exec()
            }, this), !g.description && (g.description = this.title), e.shortcut(g);
            this.disableOnSearch && e.bind("search searchend", function (a) {
                d._disabled = a.type == "search", d.update(void 0, d.value)
            }), this.init()
        }, this.init = function () {}, this.exec = function (b, c) {
            return a.Deferred().reject()
        }, this.disabled = function () {
            return this.state < 0
        }, this.enabled = function () {
            return this.state > -1
        }, this.active = function () {
            return this.state > 0
        }, this.getstate = function () {
            return -1
        }, this.update = function (a, b) {
            var c = this.state,
                d = this.value;
            this._disabled ? this.state = -1 : this.state = a !== void 0 ? a : this.getstate(), this.value = b, (c != this.state || d != this.value) && this.change()
        }, this.change = function (a) {
            var b, c;
            if (typeof a == "function") this.listeners.push(a);
            else for (c = 0; c < this.listeners.length; c++) {
                b = this.listeners[c];
                try {
                    b(this.state, this.value)
                } catch (d) {
                    this.fm.debug("error", d)
                }
            }
            return this
        }, this.hashes = function (c) {
            return c ? a.map(a.isArray(c) ? c : [c], function (a) {
                return b.file(a) ? a : null
            }) : b.selected()
        }, this.files = function (b) {
            var c = this.fm;
            return b ? a.map(a.isArray(b) ? b : [b], function (a) {
                return c.file(a) || null
            }) : c.selectedFiles()
        }
    }, elFinder.prototype.resources = {
        "class": {
            hover: "ui-state-hover",
            active: "ui-state-active",
            disabled: "ui-state-disabled",
            draggable: "ui-draggable",
            droppable: "ui-droppable",
            adroppable: "elfinder-droppable-active",
            cwdfile: "elfinder-cwd-file",
            cwd: "elfinder-cwd",
            tree: "elfinder-tree",
            treeroot: "elfinder-navbar-root",
            navdir: "elfinder-navbar-dir",
            navdirwrap: "elfinder-navbar-dir-wrapper",
            navarrow: "elfinder-navbar-arrow",
            navsubtree: "elfinder-navbar-subtree",
            navcollapse: "elfinder-navbar-collapsed",
            navexpand: "elfinder-navbar-expanded",
            treedir: "elfinder-tree-dir",
            placedir: "elfinder-place-dir",
            searchbtn: "elfinder-button-search"
        },
        tpl: {
            perms: '<span class="elfinder-perms"/>',
            symlink: '<span class="elfinder-symlink"/>',
            navicon: '<span class="elfinder-nav-icon"/>',
            navspinner: '<span class="elfinder-navbar-spinner"/>',
            navdir: '<div class="elfinder-navbar-wrapper"><span id="{id}" class="ui-corner-all elfinder-navbar-dir {cssclass}"><span class="elfinder-navbar-arrow"/><span class="elfinder-navbar-icon"/>{symlink}{permissions}{name}</span><div class="elfinder-navbar-subtree"/></div>'
        },
        mimes: {
            text: ["application/x-empty", "application/javascript", "application/xhtml+xml", "audio/x-mp3-playlist", "application/x-web-config", "application/docbook+xml", "application/x-php", "application/x-perl", "application/x-awk", "application/x-config", "application/x-csh", "application/xml"]
        },
        mixin: {
            make: function () {
                var b = this.fm,
                    c = this.name,
                    d = b.getUI("cwd"),
                    e = a.Deferred().fail(function (a) {
                        a && b.error(a)
                    }).always(function () {
                        k.remove(), j.remove(), b.enable()
                    }),
                    f = "tmp_" + parseInt(Math.random() * 1e5),
                    g = b.cwd().hash,
                    h = new Date,
                    i = {
                        hash: f,
                        name: b.uniqueName(this.prefix),
                        mime: this.mime,
                        read: !0,
                        write: !0,
                        date: "Today " + h.getHours() + ":" + h.getMinutes()
                    }, j = d.trigger("create." + b.namespace, i).find("#" + f),
                    k = a('<input type="text"/>').keydown(function (b) {
                        b.stopImmediatePropagation(), b.keyCode == a.ui.keyCode.ESCAPE ? e.reject() : b.keyCode == a.ui.keyCode.ENTER && k.blur()
                    }).mousedown(function (a) {
                        a.stopPropagation()
                    }).blur(function () {
                        var d = a.trim(k.val()),
                            h = k.parent();
                        if (h.length) {
                            if (!d) return e.reject("errInvName");
                            if (b.fileByName(d, g)) return e.reject(["errExists", d]);
                            h.html(b.escape(d)), b.lockfiles({
                                files: [f]
                            }), b.request({
                                data: {
                                    cmd: c,
                                    name: d,
                                    target: g
                                },
                                notify: {
                                    type: c,
                                    cnt: 1
                                },
                                preventFail: !0,
                                syncOnFail: !0
                            }).fail(function (a) {
                                e.reject(a)
                            }).done(function (a) {
                                e.resolve(a)
                            })
                        }
                    });
                return this.disabled() || !j.length ? e.reject() : (b.disable(), j.find(".elfinder-cwd-filename").empty("").append(k.val(i.name)), k.select().focus(), e)
            }
        }
    }, a.fn.dialogelfinder = function (b) {
        var c = "elfinderPosition",
            d = "elfinderDestroyOnClose";
        this.not(".elfinder").each(function () {
            var e = a(document),
                f = a('<div class="ui-widget-header dialogelfinder-drag ui-corner-top">' + (b.title || "Files") + "</div>"),
                g = a('<a href="#" class="dialogelfinder-drag-close ui-corner-all"><span class="ui-icon ui-icon-closethick"/></a>').appendTo(f).click(function (a) {
                    a.preventDefault(), h.dialogelfinder("close")
                }),
                h = a(this).addClass("dialogelfinder").css("position", "absolute").hide().appendTo("body").draggable({
                    handle: ".dialogelfinder-drag",
                    containment: "parent"
                }).elfinder(b).prepend(f),
                i = h.elfinder("instance");
            h.width(parseInt(h.width()) || 840).data(d, !! b.destroyOnClose).find(".elfinder-toolbar").removeClass("ui-corner-top"), b.position && h.data(c, b.position), b.autoOpen !== !1 && a(this).dialogelfinder("open")
        });
        if (b == "open") {
            var e = a(this),
                f = e.data(c) || {
                    top: parseInt(a(document).scrollTop() + (a(window).height() < e.height() ? 2 : (a(window).height() - e.height()) / 2)),
                    left: parseInt(a(document).scrollLeft() + (a(window).width() < e.width() ? 2 : (a(window).width() - e.width()) / 2))
                }, g = 100;
            e.is(":hidden") && (a("body").find(":visible").each(function () {
                var b = a(this),
                    c;
                this !== e[0] && b.css("position") == "absolute" && (c = parseInt(b.zIndex())) > g && (g = c + 1)
            }), e.zIndex(g).css(f).show().trigger("resize"), setTimeout(function () {
                e.trigger("resize").mousedown()
            }, 200))
        } else if (b == "close") {
            var e = a(this);
            e.is(":visible") && (e.data(d) ? e.elfinder("destroy").remove() : e.elfinder("close"))
        } else if (b == "instance") return a(this).getElFinder();
        return this
    }, elFinder && elFinder.prototype && typeof elFinder.prototype.i18 == "object" && (elFinder.prototype.i18.en = {
        translator: "Troex Nevelin &lt;troex@fury.scancode.ru&gt;",
        language: "English",
        direction: "ltr",
        dateFormat: "M d, Y h:i A",
        fancyDateFormat: "$1 h:i A",
        messages: {
            error: "Error",
            errUnknown: "Unknown error.",
            errUnknownCmd: "Unknown command.",
            errJqui: "Invalid jQuery UI configuration. Selectable, draggable and droppable components must be included.",
            errNode: "elFinder requires DOM Element to be created.",
            errURL: "Invalid elFinder configuration! URL option is not set.",
            errAccess: "Access denied.",
            errConnect: "Unable to connect to backend.",
            errAbort: "Connection aborted.",
            errTimeout: "Connection timeout.",
            errNotFound: "Backend not found.",
            errResponse: "Invalid backend response.",
            errConf: "Invalid backend configuration.",
            errJSON: "PHP JSON module not installed.",
            errNoVolumes: "Readable volumes not available.",
            errCmdParams: 'Invalid parameters for command "$1".',
            errDataNotJSON: "Data is not JSON.",
            errDataEmpty: "Data is empty.",
            errCmdReq: "Backend request requires command name.",
            errOpen: 'Unable to open "$1".',
            errNotFolder: "Object is not a folder.",
            errNotFile: "Object is not a file.",
            errRead: 'Unable to read "$1".',
            errWrite: 'Unable to write into "$1".',
            errPerm: "Permission denied.",
            errLocked: '"$1" is locked and can not be renamed, moved or removed.',
            errExists: 'File named "$1" already exists.',
            errInvName: "Invalid file name.",
            errFolderNotFound: "Folder not found.",
            errFileNotFound: "File not found.",
            errTrgFolderNotFound: 'Target folder "$1" not found.',
            errPopup: "Browser prevented opening popup window. To open file enable it in browser options.",
            errMkdir: 'Unable to create folder "$1".',
            errMkfile: 'Unable to create file "$1".',
            errRename: 'Unable to rename "$1".',
            errCopyFrom: 'Copying files from volume "$1" not allowed.',
            errCopyTo: 'Copying files to volume "$1" not allowed.',
            errUpload: "Upload error.",
            errUploadFile: 'Unable to upload "$1".',
            errUploadNoFiles: "No files found for upload.",
            errUploadTotalSize: "Data exceeds the maximum allowed size.",
            errUploadFileSize: "File exceeds maximum allowed size.",
            errUploadMime: "File type not allowed.",
            errUploadTransfer: '"$1" transfer error.',
            errNotReplace: 'Object "$1" already exists at this location and can not be replaced by object with another type.',
            errReplace: 'Unable to replace "$1".',
            errSave: 'Unable to save "$1".',
            errCopy: 'Unable to copy "$1".',
            errMove: 'Unable to move "$1".',
            errCopyInItself: 'Unable to copy "$1" into itself.',
            errRm: 'Unable to remove "$1".',
            errRmSrc: "Unable remove source file(s).",
            errExtract: 'Unable to extract files from "$1".',
            errArchive: "Unable to create archive.",
            errArcType: "Unsupported archive type.",
            errNoArchive: "File is not archive or has unsupported archive type.",
            errCmdNoSupport: "Backend does not support this command.",
            errReplByChild: "The folder “$1” can’t be replaced by an item it contains.",
            errArcSymlinks: "For security reason denied to unpack archives contains symlinks.",
            errArcMaxSize: "Archive files exceeds maximum allowed size.",
            errResize: 'Unable to resize "$1".',
            errUsupportType: "Unsupported file type.",
            errNotUTF8Content: 'File "$1" is not in UTF-8 and cannot be edited.',
            cmdarchive: "Create archive",
            cmdback: "Back",
            cmdcopy: "Copy",
            cmdcut: "Cut",
            cmddownload: "Download",
            cmdduplicate: "Duplicate",
            cmdedit: "Edit file",
            cmdextract: "Extract files from archive",
            cmdforward: "Forward",
            cmdgetfile: "Select files",
            cmdhelp: "About this software",
            cmdhome: "Home",
            cmdinfo: "Get info",
            cmdmkdir: "New folder",
            cmdmkfile: "New text file",
            cmdopen: "Open",
            cmdpaste: "Paste",
            cmdquicklook: "Preview",
            cmdreload: "Reload",
            cmdrename: "Rename",
            cmdrm: "Delete",
            cmdsearch: "Find files",
            cmdup: "Go to parent directory",
            cmdupload: "Upload files",
            cmdview: "View",
            cmdresize: "Resize & Rotate",
            cmdsort: "Sort",
            btnClose: "Close",
            btnSave: "Save",
            btnRm: "Remove",
            btnApply: "Apply",
            btnCancel: "Cancel",
            btnNo: "No",
            btnYes: "Yes",
            ntfopen: "Open folder",
            ntffile: "Open file",
            ntfreload: "Reload folder content",
            ntfmkdir: "Creating directory",
            ntfmkfile: "Creating files",
            ntfrm: "Delete files",
            ntfcopy: "Copy files",
            ntfmove: "Move files",
            ntfprepare: "Prepare to copy files",
            ntfrename: "Rename files",
            ntfupload: "Uploading files",
            ntfdownload: "Downloading files",
            ntfsave: "Save files",
            ntfarchive: "Creating archive",
            ntfextract: "Extracting files from archive",
            ntfsearch: "Searching files",
            ntfresize: "Resizing images",
            ntfsmth: "Doing something >_<",
            ntfloadimg: "Loading image",
            dateUnknown: "unknown",
            Today: "Today",
            Yesterday: "Yesterday",
            Jan: "Jan",
            Feb: "Feb",
            Mar: "Mar",
            Apr: "Apr",
            May: "May",
            Jun: "Jun",
            Jul: "Jul",
            Aug: "Aug",
            Sep: "Sep",
            Oct: "Oct",
            Nov: "Nov",
            Dec: "Dec",
            sortnameDirsFirst: "by name (folders first)",
            sortkindDirsFirst: "by kind (folders first)",
            sortsizeDirsFirst: "by size (folders first)",
            sortdateDirsFirst: "by date (folders first)",
            sortname: "by name",
            sortkind: "by kind",
            sortsize: "by size",
            sortdate: "by date",
            confirmReq: "Confirmation required",
            confirmRm: "Are you sure you want to remove files?<br/>This cannot be undone!",
            confirmRepl: "Replace old file with new one?",
            apllyAll: "Apply to all",
            name: "Name",
            size: "Size",
            perms: "Permissions",
            modify: "Modified",
            kind: "Kind",
            read: "read",
            write: "write",
            noaccess: "no access",
            and: "and",
            unknown: "unknown",
            selectall: "Select all files",
            selectfiles: "Select file(s)",
            selectffile: "Select first file",
            selectlfile: "Select last file",
            viewlist: "List view",
            viewicons: "Icons view",
            places: "Places",
            calc: "Calculate",
            path: "Path",
            aliasfor: "Alias for",
            locked: "Locked",
            dim: "Dimensions",
            files: "Files",
            folders: "Folders",
            items: "Items",
            yes: "yes",
            no: "no",
            link: "Link",
            searcresult: "Search results",
            selected: "selected items",
            about: "About",
            shortcuts: "Shortcuts",
            help: "Help",
            webfm: "Web file manager",
            ver: "Version",
            protocol: "protocol version",
            homepage: "Project home",
            docs: "Documentation",
            github: "Fork us on Github",
            twitter: "Follow us on twitter",
            facebook: "Join us on facebook",
            team: "Team",
            chiefdev: "chief developer",
            developer: "developer",
            contributor: "contributor",
            maintainer: "maintainer",
            translator: "translator",
            icons: "Icons",
            dontforget: "and don't forget to take your towel",
            shortcutsof: "Shortcuts disabled",
            dropFiles: "Drop files here",
            or: "or",
            selectForUpload: "Select files to upload",
            moveFiles: "Move files",
            copyFiles: "Copy files",
            rmFromPlaces: "Remove from places",
            aspectRatio: "Aspect ratio",
            scale: "Scale",
            width: "Width",
            height: "Height",
            resize: "Resize",
            crop: "Crop",
            rotate: "Rotate",
            "rotate-cw": "Rotate 90 degrees CW",
            "rotate-ccw": "Rotate 90 degrees CCW",
            degree: "°",
            kindUnknown: "Unknown",
            kindFolder: "Folder",
            kindAlias: "Alias",
            kindAliasBroken: "Broken alias",
            kindApp: "Application",
            kindPostscript: "Postscript document",
            kindMsOffice: "Microsoft Office document",
            kindMsWord: "Microsoft Word document",
            kindMsExcel: "Microsoft Excel document",
            kindMsPP: "Microsoft Powerpoint presentation",
            kindOO: "Open Office document",
            kindAppFlash: "Flash application",
            kindPDF: "Portable Document Format (PDF)",
            kindTorrent: "Bittorrent file",
            kind7z: "7z archive",
            kindTAR: "TAR archive",
            kindGZIP: "GZIP archive",
            kindBZIP: "BZIP archive",
            kindZIP: "ZIP archive",
            kindRAR: "RAR archive",
            kindJAR: "Java JAR file",
            kindTTF: "True Type font",
            kindOTF: "Open Type font",
            kindRPM: "RPM package",
            kindText: "Text document",
            kindTextPlain: "Plain text",
            kindPHP: "PHP source",
            kindCSS: "Cascading style sheet",
            kindHTML: "HTML document",
            kindJS: "Javascript source",
            kindRTF: "Rich Text Format",
            kindC: "C source",
            kindCHeader: "C header source",
            kindCPP: "C++ source",
            kindCPPHeader: "C++ header source",
            kindShell: "Unix shell script",
            kindPython: "Python source",
            kindJava: "Java source",
            kindRuby: "Ruby source",
            kindPerl: "Perl script",
            kindSQL: "SQL source",
            kindXML: "XML document",
            kindAWK: "AWK source",
            kindCSV: "Comma separated values",
            kindDOCBOOK: "Docbook XML document",
            kindImage: "Image",
            kindBMP: "BMP image",
            kindJPEG: "JPEG image",
            kindGIF: "GIF Image",
            kindPNG: "PNG Image",
            kindTIFF: "TIFF image",
            kindTGA: "TGA image",
            kindPSD: "Adobe Photoshop image",
            kindXBITMAP: "X bitmap image",
            kindPXM: "Pixelmator image",
            kindAudio: "Audio media",
            kindAudioMPEG: "MPEG audio",
            kindAudioMPEG4: "MPEG-4 audio",
            kindAudioMIDI: "MIDI audio",
            kindAudioOGG: "Ogg Vorbis audio",
            kindAudioWAV: "WAV audio",
            AudioPlaylist: "MP3 playlist",
            kindVideo: "Video media",
            kindVideoDV: "DV movie",
            kindVideoMPEG: "MPEG movie",
            kindVideoMPEG4: "MPEG-4 movie",
            kindVideoAVI: "AVI movie",
            kindVideoMOV: "Quick Time movie",
            kindVideoWM: "Windows Media movie",
            kindVideoFlash: "Flash movie",
            kindVideoMKV: "Matroska movie",
            kindVideoOGG: "Ogg movie"
        }
    }), a.fn.elfinderbutton = function (b) {
        return this.each(function () {
            var c = "class",
                d = b.fm,
                e = d.res(c, "disabled"),
                f = d.res(c, "active"),
                g = d.res(c, "hover"),
                h = "elfinder-button-menu-item",
                i = "elfinder-button-menu-item-selected",
                j, k = a(this).addClass("ui-state-default elfinder-button").attr("title", b.title).append('<span class="elfinder-button-icon elfinder-button-icon-' + b.name + '"/>').hover(function (a) {
                    !k.is("." + e) && k[a.type == "mouseleave" ? "removeClass" : "addClass"](g)
                }).click(function (a) {
                    k.is("." + e) || (j && b.variants.length > 1 ? (j.is(":hidden") && b.fm.getUI().click(), a.stopPropagation(), j.slideToggle(100)) : b.exec())
                }),
                l = function () {
                    j.hide()
                };
            a.isArray(b.variants) && (k.addClass("elfinder-menubutton"), j = a('<div class="ui-widget ui-widget-content elfinder-button-menu ui-corner-all"/>').hide().appendTo(k).zIndex(10 + k.zIndex()).delegate("." + h, "hover", function () {
                a(this).toggleClass(g)
            }).delegate("." + h, "click", function (c) {
                c.preventDefault(), c.stopPropagation(), k.removeClass(g), b.exec(b.fm.selected(), a(this).data("value"))
            }), b.fm.bind("disable select", l).getUI().click(l), b.change(function () {
                j.html(""), a.each(b.variants, function (c, d) {
                    j.append(a('<div class="' + h + '">' + d[1] + "</div>").data("value", d[0]).addClass(d[0] == b.value ? i : ""))
                })
            })), b.change(function () {
                b.disabled() ? k.removeClass(f + " " + g).addClass(e) : (k.removeClass(e), k[b.active() ? "addClass" : "removeClass"](f))
            }).change()
        })
    }, a.fn.elfindercontextmenu = function (b) {
        return this.each(function () {
            var c = a(this).addClass("ui-helper-reset ui-widget ui-state-default ui-corner-all elfinder-contextmenu elfinder-contextmenu-" + b.direction).hide().appendTo("body").delegate(".elfinder-contextmenu-item", "hover", function () {
                a(this).toggleClass("ui-state-hover")
            }),
                d = b.direction == "ltr" ? "left" : "right",
                e = a.extend({}, b.options.contextmenu),
                f = '<div class="elfinder-contextmenu-item"><span class="elfinder-button-icon {icon} elfinder-contextmenu-icon"/><span>{label}</span></div>',
                g = function (b, c, d) {
                    return a(f.replace("{icon}", c ? "elfinder-button-icon-" + c : "").replace("{label}", b)).click(function (a) {
                        a.stopPropagation(), a.stopPropagation(), d()
                    })
                }, h = function (e, f) {
                    var g = a(window),
                        h = c.outerWidth(),
                        i = c.outerHeight(),
                        j = g.width(),
                        k = g.height(),
                        l = g.scrollTop(),
                        m = g.scrollLeft(),
                        n = {
                            top: (f + i < k ? f : f - i > 0 ? f - i : f) + l,
                            left: (e + h < j ? e : e - h) + m,
                            "z-index": 100 + b.getUI("workzone").zIndex()
                        };
                    c.css(n).show(), n = {
                        "z-index": n["z-index"] + 10
                    }, n[d] = parseInt(c.width()), c.find(".elfinder-contextmenu-sub").css(n)
                }, i = function () {
                    c.hide().empty()
                }, j = function (d, f) {
                    var h = !1;
                    a.each(e[d] || [], function (d, e) {
                        var j, k, l;
                        if (e == "|" && h) {
                            c.append('<div class="elfinder-contextmenu-separator"/>'), h = !1;
                            return
                        }
                        j = b.command(e);
                        if (j && j.getstate(f) != -1) {
                            if (j.variants) {
                                if (!j.variants.length) return;
                                k = g(j.title, j.name, function () {}), l = a('<div class="ui-corner-all elfinder-contextmenu-sub"/>').appendTo(k.append('<span class="elfinder-contextmenu-arrow"/>')), k.addClass("elfinder-contextmenu-group").hover(function () {
                                    l.toggle()
                                }), a.each(j.variants, function (b, c) {
                                    l.append(a('<div class="elfinder-contextmenu-item"><span>' + c[1] + "</span></div>").click(function (a) {
                                        a.stopPropagation(), i(), j.exec(f, c[0])
                                    }))
                                })
                            } else k = g(j.title, j.name, function () {
                                i(), j.exec(f)
                            });
                            c.append(k), h = !0
                        }
                    })
                }, k = function (b) {
                    a.each(b, function (a, b) {
                        var d;
                        b.label && typeof b.callback == "function" && (d = g(b.label, b.icon, function () {
                            i(), b.callback()
                        }), c.append(d))
                    })
                };
            b.one("load", function () {
                b.bind("contextmenu", function (a) {
                    var b = a.data;
                    i(), b.type && b.targets ? j(b.type, b.targets) : b.raw && k(b.raw), c.children().length && h(b.x, b.y)
                }).one("destroy", function () {
                    c.remove()
                }).bind("disable select", i).getUI().click(i)
            })
        })
    }, a.fn.elfindercwd = function (b) {
        return this.not(".elfinder-cwd").each(function () {
            var c = b.storage("view") == "list",
                d = "undefined",
                e = "select." + b.namespace,
                f = "unselect." + b.namespace,
                g = "disable." + b.namespace,
                h = "enable." + b.namespace,
                i = "class",
                j = b.res(i, "cwdfile"),
                k = "." + j,
                l = "ui-selected",
                m = b.res(i, "disabled"),
                n = b.res(i, "draggable"),
                o = b.res(i, "droppable"),
                p = b.res(i, "hover"),
                q = b.res(i, "adroppable"),
                r = j + "-tmp",
                s = b.options.loadTmbs > 0 ? b.options.loadTmbs : 5,
                t = "",
                // show/hide columns
                u = {
                    icon: '<div id="{hash}" class="' + j + ' {permsclass} {dirclass} ui-corner-all"><div class="elfinder-cwd-file-wrapper ui-corner-all"><div class="elfinder-cwd-icon {mime} ui-corner-all" unselectable="on" {style}/>{marker}</div><div class="elfinder-cwd-filename" title="{name}">{name}</div></div>',
                    row: '<tr id="{hash}" class="' + j + ' {permsclass} {dirclass}"><td><div class="elfinder-cwd-file-wrapper"><span class="elfinder-cwd-icon {mime}"/>{marker}<span class="elfinder-cwd-filename">{name}</span></div></td><td>{perms}</td><td>{date}</td><td>{size}</td><td>{kind}</td></tr>'
                }, v = b.res("tpl", "perms"),
                w = b.res("tpl", "symlink"),
                x = {
                    permsclass: function (a) {
                        return b.perms2class(a)
                    },
                    perms: function (a) {
                        return b.formatPermissions(a)
                    },
                    dirclass: function (a) {
                        return a.mime == "directory" ? "directory" : ""
                    },
                    mime: function (a) {
                        return b.mime2class(a.mime)
                    },
                    size: function (a) {
                        return b.formatSize(a.size)
                    },
                    date: function (a) {
                        return b.formatDate(a)
                    },
                    kind: function (a) {
                        return b.mime2kind(a)
                    },
                    marker: function (a) {
                        return (a.alias || a.mime == "symlink-broken" ? w : "") + (!a.read || !a.write ? v : "")
                    }
                }, y = function (a) {
                    return a.name = b.escape(a.name), u[c ? "row" : "icon"].replace(/\{([a-z]+)\}/g, function (b, c) {
                        return x[c] ? x[c](a) : a[c] ? a[c] : ""
                    })
                }, z = !1,
                A = function (b, d) {
                    function r(a, b) {
                        return a[b + "All"]("[id]:not(." + m + "):first")
                    }
                    var g = a.ui.keyCode,
                        h = b == g.LEFT || b == g.UP,
                        i = S.find("[id]." + l),
                        j = h ? "first" : "last",
                        k, n, o, p, q;
                    if (i.length) {
                        k = i.filter(h ? ":first" : ":last"), o = r(k, h ? "prev" : "next");
                        if (!o.length) n = k;
                        else if (c || b == g.LEFT || b == g.RIGHT) n = o;
                        else {
                            p = k.position().top, q = k.position().left, n = k;
                            if (h) {
                                do n = n.prev("[id]");
                                while (n.length && !(n.position().top < p && n.position().left <= q));
                                n.is("." + m) && (n = r(n, "next"))
                            } else {
                                do n = n.next("[id]");
                                while (n.length && !(n.position().top > p && n.position().left >= q));
                                n.is("." + m) && (n = r(n, "prev")), n.length || (o = S.find("[id]:not(." + m + "):last"), o.position().top > p && (n = o))
                            }
                        }
                    } else n = S.find("[id]:not(." + m + "):" + (h ? "last" : "first"));
                    n && n.length && (d ? n = k.add(k[h ? "prevUntil" : "nextUntil"]("#" + n.attr("id"))).add(n) : i.trigger(f), n.trigger(e), F(n.filter(h ? ":first" : ":last")), E())
                }, B = function (a) {
                    S.find("#" + a).trigger(e)
                }, C = function () {
                    S.find("[id]." + l).trigger(f)
                }, D = function () {
                    return a.map(S.find("[id]." + l), function (b) {
                        return b = a(b), b.is("." + m) ? null : a(b).attr("id")
                    })
                }, E = function () {
                    b.trigger("select", {
                        selected: D()
                    })
                }, F = function (a) {
                    var b = a.position().top,
                        c = a.outerHeight(!0),
                        d = T.scrollTop(),
                        e = T.innerHeight();
                    b + c > d + e ? T.scrollTop(parseInt(b + c - e)) : b < d && T.scrollTop(b)
                }, G = [],
                H = function (a) {
                    var b = G.length;
                    while (b--) if (G[b].hash == a) return b;
                    return -1
                }, I = "scroll." + b.namespace,
                J = function () {
                    var d = [],
                        e = !1,
                        f = [],
                        g = {}, h = S.find("[id]:last"),
                        i = !h.length,
                        j = c ? S.children("table").children("tbody") : S,
                        k;
                    if (!G.length) return T.unbind(I);
                    while ((!h.length || h.position().top <= T.height() + T.scrollTop() + b.options.showThreshold) && (k = G.splice(0, b.options.showFiles)).length) d = a.map(k, function (a) {
                        return a.hash && a.name ? (a.mime == "directory" && (e = !0), a.tmb && (a.tmb === 1 ? f.push(a.hash) : g[a.hash] = a.tmb), y(a)) : null
                    }), j.append(d.join("")), h = S.find("[id]:last"), i && S.scrollTop(0);
                    M(g), f.length && N(f), e && L()
                }, K = a.extend({}, b.droppable, {
                    over: function (c, d) {
                        var e = b.cwd().hash;
                        a.each(d.helper.data("files"), function (a, c) {
                            if (b.file(c).phash == e) return S.removeClass(q), !1
                        })
                    }
                }),
                L = function () {
                    setTimeout(function () {
                        S.find(".directory:not(." + o + ",.elfinder-na,.elfinder-ro)").droppable(b.droppable)
                    }, 20)
                }, M = function (c) {
                    var d = b.option("tmbUrl"),
                        e = !0,
                        f;
                    return a.each(c, function (b, c) {
                        var g = S.find("#" + b);
                        g.length ? function (b, c) {
                            a("<img/>").load(function () {
                                b.find(".elfinder-cwd-icon").css("background", "url('" + c + "') center center no-repeat")
                            }).attr("src", c)
                        }(g, d + c) : (e = !1, (f = H(b)) != -1 && (G[f].tmb = c))
                    }), e
                }, N = function (a) {
                    var c = [];
                    if (b.oldAPI) {
                        b.request({
                            data: {
                                cmd: "tmb",
                                current: b.cwd().hash
                            },
                            preventFail: !0
                        }).done(function (a) {
                            M(a.images || []) && a.tmb && N()
                        });
                        return
                    }
                    c = c = a.splice(0, s), c.length && b.request({
                        data: {
                            cmd: "tmb",
                            targets: c
                        },
                        preventFail: !0
                    }).done(function (b) {
                        M(b.images || []) && N(a)
                    })
                }, O = function (a) {
                    var d = c ? S.find("tbody") : S,
                        e = a.length,
                        f = [],
                        g = {}, h = !1,
                        i = function (a) {
                            var c = S.find("[id]:first"),
                                d;
                            while (c.length) {
                                d = b.file(c.attr("id"));
                                if (d && b.compare(a, d) < 0) return c;
                                c = c.next("[id]")
                            }
                        }, j = function (a) {
                            var c = G.length,
                                d;
                            for (d = 0; d < c; d++) if (b.compare(a, G[d]) < 0) return d;
                            return c || -1
                        }, k, l, m, n;
                    while (e--) {
                        k = a[e], l = k.hash;
                        if (S.find("#" + l)
                            .length) continue;
                        (m = i(k)) && m.length ? m.before(y(k)) : (n = j(k)) >= 0 ? G.splice(n, 0, k) : d.append(y(k)), S.find("#" + l).length && (k.mime == "directory" ? h = !0 : k.tmb && (k.tmb === 1 ? f.push(l) : g[l] = k.tmb))
                    }
                    M(g), f.length && N(f), h && L()
                }, P = function (a) {
                    var c = a.length,
                        d, e, f;
                    while (c--) {
                        d = a[c];
                        if ((e = S.find("#" + d)).length) try {
                            e.detach()
                        } catch (g) {
                            b.debug("error", g)
                        } else(f = H(d)) != -1 && G.splice(f, 1)
                    }
                }, Q = {
                    name: b.i18n("name"),
                    perm: b.i18n("perms"),
                    mod: b.i18n("modify"),
                    size: b.i18n("size"),
                    kind: b.i18n("kind")
                }, R = function (d, e) {
                    var f = b.cwd().hash;
                    try {
                        S.children("table," + k).remove().end()
                    } catch (g) {
                        S.html("")
                    }
                    S.removeClass("elfinder-cwd-view-icons elfinder-cwd-view-list").addClass("elfinder-cwd-view-" + (c ? "list" : "icons")), T[c ? "addClass" : "removeClass"]("elfinder-cwd-wrapper-list"), c && S.html('<table><thead><tr class="ui-state-default"><td >' + Q.name + "</td><td>" + Q.perm + "</td><td>" + Q.mod + "</td><td>" + Q.size + "</td><td>" + Q.kind + "</td></tr></thead><tbody/></table>"), G = a.map(d, function (a) {
                        return e || a.phash == f ? a : null
                    }), G = b.sortFiles(G), T.bind(I, J).trigger(I), E()
                }, S = a(this).addClass("ui-helper-clearfix elfinder-cwd").attr("unselectable", "on").delegate(k, "click." + b.namespace, function (b) {
                    var c = this.id ? a(this) : a(this).parents("[id]:first"),
                        d = c.prevAll("." + l + ":first"),
                        g = c.nextAll("." + l + ":first"),
                        h = d.length,
                        i = g.length,
                        j;
                    b.stopImmediatePropagation(), b.shiftKey && (h || i) ? (j = h ? c.prevUntil("#" + d.attr("id")) : c.nextUntil("#" + g.attr("id")), j.add(c).trigger(e)) : b.ctrlKey || b.metaKey ? c.trigger(c.is("." + l) ? f : e) : (S.find("[id]." + l).trigger(f), c.trigger(e)), E()
                }).delegate(k, "dblclick." + b.namespace, function (a) {
                    b.dblclick({
                        file: this.id
                    })
                }).delegate(k, "mouseenter." + b.namespace, function (d) {
                    var e = a(this),
                        f = c ? e : e.children();
                    !e.is("." + r) && !f.is("." + n + ",." + m) && f.draggable(b.draggable)
                }).delegate(k, e, function (b) {
                    var c = a(this);
                    !z && !c.is("." + m) && c.addClass(l).children().addClass(p)
                }).delegate(k, f, function (b) {
                    !z && a(this).removeClass(l).children().removeClass(p)
                }).delegate(k, g, function () {
                    var b = a(this).removeClass(l).addClass(m),
                        d = (c ? b : b.children()).removeClass(p);
                    b.is("." + o) && b.droppable("disable"), d.is("." + n) && d.draggable("disable"), !c && d.removeClass(m)
                }).delegate(k, h, function () {
                    var b = a(this).removeClass(m),
                        d = c ? b : b.children();
                    b.is("." + o) && b.droppable("enable"), d.is("." + n) && d.draggable("enable")
                }).delegate(k, "scrolltoview", function () {
                    F(a(this))
                }).delegate(k, "hover", function (c) {
                    b.trigger("hover", {
                        hash: a(this).attr("id"),
                        type: c.type
                    })
                }).bind("contextmenu." + b.namespace, function (c) {
                    var d = a(c.target).closest("." + j);
                    d.length && (c.stopPropagation(), c.preventDefault(), d.is("." + m) || (d.is("." + l) || (S.trigger("unselectall"), d.trigger(e), E()), b.trigger("contextmenu", {
                        type: "files",
                        targets: b.selected(),
                        x: c.clientX,
                        y: c.clientY
                    })))
                }).selectable({
                    filter: k,
                    stop: E,
                    selected: function (b, c) {
                        a(c.selected).trigger(e)
                    },
                    unselected: function (b, c) {
                        a(c.unselected).trigger(f)
                    }
                }).droppable(K).bind("create." + b.namespace, function (b, d) {
                    var e = c ? S.find("tbody") : S;
                    S.trigger("unselectall"), e.prepend(a(y(d)).addClass(r)), S.scrollTop(0)
                }).bind("unselectall", function () {
                    S.find("[id]." + l + "").trigger(f), E()
                }).bind("selectfile", function (a, b) {
                    S.find("#" + b).trigger(e), E()
                }),
                T = a('<div class="elfinder-cwd-wrapper"/>').bind("contextmenu", function (a) {
                    a.preventDefault(), b.trigger("contextmenu", {
                        type: "cwd",
                        targets: [b.cwd().hash],
                        x: a.clientX,
                        y: a.clientY
                    })
                }),
                U = function () {
                    var b = 0;
                    T.siblings(".elfinder-panel:visible").each(function () {
                        b += a(this).outerHeight(!0)
                    }), T.height(W.height() - b)
                }, V = a(this).parent().resize(U),
                W = V.children(".elfinder-workzone").append(T.append(this));
            b.dragUpload && (T[0].addEventListener("dragenter", function (a) {
                a.preventDefault(), a.stopPropagation(), T.addClass(q)
            }, !1), T[0].addEventListener("dragleave", function (a) {
                a.preventDefault(), a.stopPropagation(), a.target == S[0] && T.removeClass(q)
            }, !1), T[0].addEventListener("dragover", function (a) {
                a.preventDefault(), a.stopPropagation()
            }, !1), T[0].addEventListener("drop", function (a) {
                a.preventDefault(), T.removeClass(q), a.dataTransfer && a.dataTransfer.files && a.dataTransfer.files.length && b.exec("upload", {
                    files: a.dataTransfer.files
                })
            }, !1)), b.bind("open search", function (a) {
                R(a.data.files, a.type == "search")
            }).bind("searchend sortchange", function () {
                t && R(b.files())
            }).bind("searchstart", function (a) {
                t = a.data.query
            }).bind("searchend", function () {
                t = ""
            }).bind("viewchange", function () {
                var d = b.selected(),
                    e = b.storage("view") == "list";
                e != c && (c = e, R(b.files()), a.each(d, function (a, b) {
                    B(b)
                }), E()), U()
            }).add(function (c) {
                var d = b.cwd().hash,
                    e = t ? a.map(c.data.added || [], function (a) {
                        return a.name.indexOf(t) === -1 ? null : a
                    }) : a.map(c.data.added || [], function (a) {
                        return a.phash == d ? a : null
                    });
                O(e)
            }).change(function (c) {
                var d = b.cwd().hash,
                    e = b.selected(),
                    f;
                t ? a.each(c.data.changed || [], function (b, c) {
                    P([c.hash]), c.name.indexOf(t) !== -1 && (O([c]), a.inArray(c.hash, e) !== -1 && B(c.hash))
                }) : a.each(a.map(c.data.changed || [], function (a) {
                    return a.phash == d ? a : null
                }), function (b, c) {
                    P([c.hash]), O([c]), a.inArray(c.hash, e) !== -1 && B(c.hash)
                }), E()
            }).remove(function (a) {
                P(a.data.removed || []), E()
            }).bind("open add search searchend", function () {
                S.css("height", "auto"), S.outerHeight(!0) < T.height() && S.height(T.height() - (S.outerHeight(!0) - S.height()) - 2)
            }).dragstart(function (b) {
                var c = a(b.data.target),
                    d = b.data.originalEvent;
                c.is(k) && (c.is("." + l) || (!(d.ctrlKey || d.metaKey || d.shiftKey) && C(), c.trigger(e), E()), S.droppable("disable")), S.selectable("disable").removeClass(m), z = !0
            }).dragstop(function () {
                S.selectable("enable"), z = !1
            }).bind("lockfiles unlockfiles", function (a) {
                var b = a.type == "lockfiles" ? g : h,
                    c = a.data.files || [],
                    d = c.length;
                while (d--) S.find("#" + c[d]).trigger(b);
                E()
            }).bind("mkdir mkfile duplicate upload rename archive extract", function (c) {
                var d = b.cwd().hash,
                    e;
                S.trigger("unselectall"), a.each(c.data.added || [], function (a, b) {
                    b && b.phash == d && B(b.hash)
                }), E()
            }).shortcut({
                pattern: "ctrl+a",
                description: "selectall",
                callback: function () {
                    var c = [],
                        d;
                    S.find("[id]:not(." + l + ")").trigger(e), G.length ? (d = b.cwd().hash, b.select({
                        selected: a.map(b.files(), function (a) {
                            return a.phash == d ? a.hash : null
                        })
                    })) : E()
                }
            }).shortcut({
                pattern: "left right up down shift+left shift+right shift+up shift+down",
                description: "selectfiles",
                type: "keydown",
                callback: function (a) {
                    A(a.keyCode, a.shiftKey)
                }
            }).shortcut({
                pattern: "home",
                description: "selectffile",
                callback: function (a) {
                    C(), F(S.find("[id]:first").trigger(e)), E()
                }
            }).shortcut({
                pattern: "end",
                description: "selectlfile",
                callback: function (a) {
                    C(), F(S.find("[id]:last").trigger(e)), E()
                }
            })
        }), this
    }, a.fn.elfinderdialog = function (b) {
        var c;
        return typeof b == "string" && (c = this.closest(".ui-dialog")).length && (b == "open" && c.is(":hidden") ? c.fadeIn(120, function () {
            c.trigger("open")
        }) : b == "close" && c.is(":visible") ? c.hide().trigger("close") : b == "destroy" ? c.hide().remove() : b == "toTop" && c.trigger("totop")), b = a.extend({}, a.fn.elfinderdialog.defaults, b), this.filter(":not(.ui-dialog-content)").each(function () {
            var c = a(this).addClass("ui-dialog-content ui-widget-content"),
                d = c.parent(),
                e = "elfinder-dialog-active",
                f = "elfinder-dialog",
                g = "elfinder-dialog-notify",
                h = "ui-state-hover",
                i = parseInt(Math.random() * 1e6),
                j = d.children(".elfinder-overlay"),
                k = a('<div class="ui-dialog-buttonset"/>'),
                l = a('<div class=" ui-helper-clearfix ui-dialog-buttonpane ui-widget-content"/>').append(k),
                m = a('<div class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-draggable std42-dialog  ' + f + " " + b.cssClass + '"/>').hide().append(c).appendTo(d).draggable({
                    handle: ".ui-dialog-titlebar",
                    containment: a("body")
                }).css({
                    width: b.width,
                    height: b.height
                }).mousedown(function (b) {
                    b.stopPropagation(), a(document).mousedown(), m.is("." + e) || (d.find("." + f + ":visible").removeClass(e), m.addClass(e).zIndex(n() + 1))
                }).bind("open", function () {
                    b.modal && j.elfinderoverlay("show"), m.trigger("totop"), typeof b.open == "function" && a.proxy(b.open, c[0])(), m.is("." + g) || d.find("." + f + ":visible").not("." + g).each(function () {
                        var b = a(this),
                            c = parseInt(b.css("top")),
                            d = parseInt(b.css("left")),
                            e = parseInt(m.css("top")),
                            f = parseInt(m.css("left"));
                        b[0] != m[0] && (c == e || d == f) && m.css({
                            top: c + 10 + "px",
                            left: d + 10 + "px"
                        })
                    })
                }).bind("close", function () {
                    var e = d.find(".elfinder-dialog:visible"),
                        f = n();
                    b.modal && j.elfinderoverlay("hide"), e.length ? e.each(function () {
                        var b = a(this);
                        if (b.zIndex() >= f) return b.trigger("totop"), !1
                    }) : setTimeout(function () {
                        d.mousedown().click()
                    }, 10), typeof b.close == "function" ? a.proxy(b.close, c[0])() : b.destroyOnClose && m.hide().remove()
                }).bind("totop", function () {
                    a(this).mousedown().find(".ui-button:first").focus().end().find(":text:first").focus()
                }),
                n = function () {
                    var b = d.zIndex() + 10;
                    return d.find("." + f + ":visible").each(function () {
                        var c;
                        this != m[0] && (c = a(this).zIndex(), c > b && (b = c))
                    }), b
                }, o;
            b.position || (o = parseInt((d.height() - m.outerHeight()) / 2 - 42), b.position = {
                top: (o > 0 ? o : 0) + "px",
                left: parseInt((d.width() - m.outerWidth()) / 2) + "px"
            }), m.css(b.position), b.closeOnEscape && a(document).bind("keyup." + i, function (b) {
                b.keyCode == a.ui.keyCode.ESCAPE && m.is("." + e) && (c.elfinderdialog("close"), a(document).unbind("keyup." + i))
            }), m.prepend(a('<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">' + b.title + "</div>").prepend(a('<a href="#" class="ui-dialog-titlebar-close ui-corner-all"><span class="ui-icon ui-icon-closethick"/></a>').mousedown(function (a) {
                a.preventDefault(), c.elfinderdialog("close")
            }))), a.each(b.buttons, function (b, d) {
                var e = a('<button type="button" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"><span class="ui-button-text">' + b + "</span></button>").click(a.proxy(d, c[0])).hover(function (b) {
                    a(this)[b.type == "mouseenter" ? "focus" : "blur"]()
                }).focus(function () {
                    a(this).addClass(h)
                }).blur(function () {
                    a(this).removeClass(h)
                }).keydown(function (b) {
                    var c;
                    b.keyCode == a.ui.keyCode.ENTER ? a(this).click() : b.keyCode == a.ui.keyCode.TAB && (c = a(this).next(".ui-button"), c.length ? c.focus() : a(this).parent().children(".ui-button:first").focus())
                });
                k.append(e)
            }), k.children().length && m.append(l), b.resizable && a.fn.resizable && m.resizable({
                minWidth: b.minWidth,
                minHeight: b.minHeight,
                alsoResize: this
            }), typeof b.create == "function" && a.proxy(b.create, this)(), b.autoOpen && c.elfinderdialog("open")
        }), this
    }, a.fn.elfinderdialog.defaults = {
        cssClass: "",
        title: "",
        modal: !1,
        resizable: !0,
        autoOpen: !0,
        closeOnEscape: !0,
        destroyOnClose: !1,
        buttons: {},
        position: null,
        width: 320,
        height: "auto",
        minWidth: 200,
        minHeight: 110
    }, a.fn.elfindernavbar = function (b, c) {
        return this.not(".elfinder-navbar").each(function () {
            var d = a(this).addClass("ui-state-default elfinder-navbar"),
                e = d.parent().resize(function () {
                    d.height(f.height() - g)
                }),
                f = e.children(".elfinder-workzone").append(d),
                g = d.outerHeight() - d.height(),
                h = b.direction == "ltr",
                i;
            a.fn.resizable && (i = d.resizable({
                handles: h ? "e" : "w",
                minWidth: c.minWidth || 150,
                maxWidth: c.maxWidth || 500
            }).bind("resize scroll", function () {
                i.css({
                    top: parseInt(d.scrollTop()) + "px",
                    left: parseInt(h ? d.width() + d.scrollLeft() - i.width() - 2 : d.scrollLeft() + 2)
                })
            }).find(".ui-resizable-handle").zIndex(d.zIndex() + 10), h || d.resize(function () {
                d.css("left", null).css("right", 0)
            }), b.one("open", function () {
                setTimeout(function () {
                    d.trigger("resize")
                }, 150)
            }))
        }), this
    }, a.fn.elfinderoverlay = function (b) {
        this.filter(":not(.elfinder-overlay)").each(function () {
            b = a.extend({}, b), a(this).addClass("ui-widget-overlay elfinder-overlay").hide().mousedown(function (a) {
                a.preventDefault(), a.stopPropagation()
            }).data({
                cnt: 0,
                show: typeof b.show == "function" ? b.show : function () {},
                hide: typeof b.hide == "function" ? b.hide : function () {}
            })
        });
        if (b == "show") {
            var c = this.eq(0),
                d = c.data("cnt") + 1,
                e = c.data("show");
            c.data("cnt", d), c.is(":hidden") && (c.zIndex(c.parent().zIndex() + 1), c.show(), e())
        }
        if (b == "hide") {
            var c = this.eq(0),
                d = c.data("cnt") - 1,
                f = c.data("hide");
            c.data("cnt", d), d == 0 && c.is(":visible") && (c.hide(), f())
        }
        return this
    }, a.fn.elfinderpanel = function (b) {
        return this.each(function () {
            var c = a(this).addClass("elfinder-panel ui-state-default ui-corner-all"),
                d = "margin-" + (b.direction == "ltr" ? "left" : "right");
            b.one("load", function (a) {
                var e = b.getUI("navbar");
                c.css(d, parseInt(e.outerWidth(!0))), e.bind("resize", function () {
                    c.is(":visible") && c.css(d, parseInt(e.outerWidth(!0)))
                })
            })
        })
    }, a.fn.elfinderpath = function (b) {
        return this.each(function () {
            var c = a(this).addClass("elfinder-path").html("&nbsp;").delegate("a", "click", function (c) {
                var d = a(this).attr("href").substr(1);
                c.preventDefault(), d != b.cwd().hash && b.exec("open", d)
            }).prependTo(b.getUI("statusbar").show());
            b.bind("open searchend", function () {
                var d = [];
                a.each(b.parents(b.cwd().hash), function (a, c) {
                    d.push('<a href="#' + c + '">' + b.escape(b.file(c).name) + "</a>")
                }), c.html(d.join(b.option("separator")))
            }).bind("search", function () {
                c.html(b.i18n("searcresult"))
            })
        })
    }, a.fn.elfinderplaces = function (b, c) {
        return this.each(function () {
            var d = [],
                e = "class",
                f = b.res(e, "navdir"),
                g = b.res(e, "navcollapse"),
                h = b.res(e, "navexpand"),
                i = b.res(e, "hover"),
                j = b.res(e, "treeroot"),
                k = b.res("tpl", "navdir"),
                l = b.res("tpl", "perms"),
                m = a(b.res("tpl", "navspinner")),
                n = function (a) {
                    return a.substr(6)
                }, o = function (a) {
                    return "place-" + a
                }, p = function () {
                    b.storage("places", d.join(","))
                }, q = function (c) {
                    return a(k.replace(/\{id\}/, o(c.hash)).replace(/\{name\}/, b.escape(c.name)).replace(/\{cssclass\}/, b.perms2class(c)).replace(/\{permissions\}/, !c.read || !c.write ? l : "").replace(/\{symlink\}/, ""))
                }, r = function (c) {
                    var e = q(c);
                    w.children().length && a.each(w.children(), function () {
                        var b = a(this);
                        if (c.name.localeCompare(b.children("." + f).text()) < 0) return !e.insertBefore(b)
                    }), d.push(c.hash), !e.parent().length && w.append(e), v.addClass(g), e.draggable({
                        appendTo: "body",
                        revert: !1,
                        helper: function () {
                            var c = a(this);
                            return c.children().removeClass("ui-state-hover"), a('<div class="elfinder-place-drag elfinder-' + b.direction + '"/>').append(c.clone()).data("hash", n(c.children(":first").attr("id")))
                        },
                        start: function () {
                            a(this).hide()
                        },
                        stop: function (b, c) {
                            var d = x.offset().top,
                                e = x.offset().left,
                                f = x.width(),
                                g = x.height(),
                                h = b.clientX,
                                i = b.clientY;
                            h > e && h < e + f && i > d && i < i + g ? a(this).show() : (s(c.helper.data("hash")), p())
                        }
                    })
                }, s = function (b) {
                    var c = a.inArray(b, d);
                    c !== -1 && (d.splice(c, 1), w.find("#" + o(b)).parent().remove(), !w.children().length && v.removeClass(g + " " + h))
                }, t = function () {
                    w.empty(), v.removeClass(g + " " + h)
                }, u = q({
                    hash: "root-" + b.namespace,
                    name: b.i18n(c.name, "places"),
                    read: !0,
                    write: !0
                }),
                v = u.children("." + f).addClass(j).click(function () {
                    v.is("." + g) && (x.toggleClass(h), w.slideToggle(), b.storage("placesState", x.is("." + h) ? 1 : 0))
                }),
                w = u.children("." + b.res(e, "navsubtree")),
                x = a(this).addClass(b.res(e, "tree") + " elfinder-places ui-corner-all").hide().append(u).appendTo(b.getUI("navbar")).delegate("." + f, "hover", function () {
                    a(this).toggleClass("ui-state-hover")
                }).delegate("." + f, "click", function (c) {
                    b.exec("open", a(this).attr("id").substr(6))
                }).delegate("." + f + ":not(." + j + ")", "contextmenu", function (c) {
                    var d = a(this).attr("id").substr(6);
                    c.preventDefault(), b.trigger("contextmenu", {
                        raw: [{
                            label: b.i18n("rmFromPlaces"),
                            icon: "rm",
                            callback: function () {
                                s(d), p()
                            }
                        }],
                        x: c.clientX,
                        y: c.clientY
                    })
                }).droppable({
                    tolerance: "pointer",
                    accept: ".elfinder-cwd-file-wrapper,.elfinder-tree-dir,.elfinder-cwd-file",
                    hoverClass: b.res("class", "adroppable"),
                    drop: function (c, e) {
                        var f = !0;
                        a.each(e.helper.data("files"), function (c, e) {
                            var g = b.file(e);
                            g && g.mime == "directory" && a.inArray(g.hash, d) === -1 ? r(g) : f = !1
                        }), p(), f && e.helper.hide()
                    }
                });
            b.one("load", function () {
                if (b.oldAPI) return;
                x.show().parent().show(), d = a.map(b.storage("places").split(","), function (a) {
                    return a || null
                }), d.length && (v.prepend(m), b.request({
                    data: {
                        cmd: "info",
                        targets: d
                    },
                    preventDefault: !0
                }).done(function (c) {
                    d = [], a.each(c.files, function (a, b) {
                        b.mime == "directory" && r(b)
                    }), p(), b.storage("placesState") > 0 && v.click()
                }).always(function () {
                    m.remove()
                })), b.remove(function (b) {
                    a.each(b.data.removed, function (a, b) {
                        s(b)
                    }), p()
                }).change(function (b) {
                    a.each(b.data.changed, function (b, c) {
                        a.inArray(c.hash, d) !== -1 && (s(c.hash), c.mime == "directory" && r(c))
                    }), p()
                }).bind("sync", function () {
                    d.length && (v.prepend(m), b.request({
                        data: {
                            cmd: "info",
                            targets: d
                        },
                        preventDefault: !0
                    }).done(function (b) {
                        a.each(b.files || [], function (b, c) {
                            a.inArray(c.hash, d) === -1 && s(c.hash)
                        }), p()
                    }).always(function () {
                        m.remove()
                    }))
                })
            })
        })
    }, a.fn.elfindersearchbutton = function (b) {
        return this.each(function () {
            var c = !1,
                d = a(this).hide().addClass("ui-widget-content elfinder-button " + b.fm.res("class", "searchbtn") + ""),
                e = function () {
                    b.exec(a.trim(g.val())).done(function () {
                        c = !0, g.focus()
                    })
                }, f = function () {
                    g.val(""), c && (c = !1, b.fm.trigger("searchend"))
                }, g = a('<input type="text" size="42"/>').appendTo(d).keypress(function (a) {
                    a.stopPropagation()
                }).keydown(function (a) {
                    a.stopPropagation(), a.keyCode == 13 && e(), a.keyCode == 27 && (a.preventDefault(), f())
                });
            a('<span class="ui-icon ui-icon-search" title="' + b.title + '"/>').appendTo(d).click(e), a('<span class="ui-icon ui-icon-close"/>').appendTo(d).click(f), setTimeout(function () {
                d.parent().
                detach(), b.fm.getUI("toolbar").prepend(d.show());
                if (a.browser.msie) {
                    var c = d.children(b.fm.direction == "ltr" ? ".ui-icon-close" : ".ui-icon-search");
                    c.css({
                        right: "",
                        left: parseInt(d.width()) - c.outerWidth(!0)
                    })
                }
            }, 200), b.fm.error(function () {
                g.unbind("keydown")
            }).select(function () {
                g.blur()
            }).bind("searchend", function () {
                g.val("")
            }).viewchange(f).shortcut({
                pattern: "ctrl+f f3",
                description: b.title,
                callback: function () {
                    g.select().focus()
                }
            })
        })
    }, a.fn.elfindersortbutton = function (b) {
        return this.each(function () {
            var c = "class",
                d = b.fm,
                e = d.res(c, "disabled"),
                f = d.res(c, "active"),
                g = d.res(c, "hover"),
                h = "elfinder-button-menu-item",
                i = "elfinder-button-menu-item-selected",
                j, k = a(this).addClass("ui-state-default elfinder-button elfiner-button-" + b.name).attr("title", b.title).append('<span class="elfinder-button-icon elfinder-button-icon-' + b.name + '"/>').hover(function (a) {
                    !k.is("." + e) && k.toggleClass(g)
                }).click(function (a) {
                    k.is("." + e) || (j && b.variants.length > 1 ? (j.is(":hidden") && b.fm.getUI().click(), a.stopPropagation(), j.slideToggle(100)) : b.exec())
                }),
                l = function () {
                    j.hide()
                };
            a.isArray(b.variants) && (k.addClass("elfinder-menubutton"), j = a('<div class="ui-widget ui-widget-content elfinder-button-menu ui-corner-all"/>').hide().appendTo(k).zIndex(10 + k.zIndex()).delegate("." + h, "hover", function () {
                a(this).toggleClass(g)
            }).delegate("." + h, "click", function (c) {
                c.preventDefault(), c.stopPropagation(), k.removeClass(g), b.exec(b.fm.selected(), a(this).data("value"))
            }), b.fm.bind("disable select", l).getUI().click(l), b.change(function () {
                j.html(""), a.each(b.variants, function (c, d) {
                    j.append(a('<div class="' + h + " " + (d[0] == b.value ? i : "") + " elfinder-menu-item-sort-" + b.fm.sortDirect + '"><span class="elfinder-menu-item-sort-dir"/>' + d[1] + "</div>").data("value", d[0]))
                })
            })), b.change(function () {
                b.disabled() ? k.removeClass(f + " " + g).addClass(e) : (k.removeClass(e), k[b.active() ? "addClass" : "removeClass"](f))
            }).change()
        })
    }, a.fn.elfinderstat = function (b) {
        return this.each(function () {
            var c = a(this).addClass("elfinder-stat-size"),
                d = a('<div class="elfinder-stat-selected"/>'),
                e = b.i18n("size").toLowerCase(),
                f = b.i18n("items").toLowerCase(),
                g = b.i18n("selected"),
                h = function (d, g) {
                    var h = 0,
                        i = 0;
                    a.each(d, function (a, b) {
                        if (!g || b.phash == g) h++, i += parseInt(b.size) || 0
                    }), c.html(f + ": " + h + ", " + e + ": " + b.formatSize(i))
                };
            b.getUI("statusbar").prepend(c).append(d).show(), b.bind("open reload add remove change searchend", function () {
                h(b.files(), b.cwd().hash)
            }).search(function (a) {
                h(a.data.files)
            }).select(function () {
                var c = 0,
                    f = 0,
                    h = b.selectedFiles();
                if (h.length == 1) {
                    c = h[0].size, d.html(b.escape(h[0].name) + (c > 0 ? ", " + b.formatSize(c) : ""));
                    return
                }
                a.each(h, function (a, b) {
                    f++, c += parseInt(b.size) || 0
                }), d.html(f ? g + ": " + f + ", " + e + ": " + b.formatSize(c) : "&nbsp;")
            })
        })
    }, a.fn.elfindertoolbar = function (b, c) {
        return this.not(".elfinder-toolbar").each(function () {
            var d = b._commands,
                e = a(this).addClass("ui-helper-clearfix ui-widget-header ui-corner-top elfinder-toolbar"),
                f = c || [],
                g = f.length,
                h, i, j, k;
            e.prev().length && e.parent().prepend(this);
            while (g--) if (f[g]) {
                j = a('<div class="ui-widget-content ui-corner-all elfinder-buttonset"/>'), h = f[g].length;
                while (h--) if (i = d[f[g][h]]) k = "elfinder" + i.options.ui, a.fn[k] && j.prepend(a("<div/>")[k](i));
                j.children().length && e.prepend(j), j.children(":not(:last),:not(:first):not(:last)").after('<span class="ui-widget-content elfinder-toolbar-button-separator"/>')
            }
            e.children().length && e.show()
        }), this
    }, a.fn.elfindertree = function (b, c) {
        var d = b.res("class", "tree");
        return this.not("." + d).each(function () {
            var e = "class",
                f = b.res(e, "treeroot"),
                g = c.openRootOnLoad,
                h = b.res(e, "navsubtree"),
                i = b.res(e, "treedir"),
                j = b.res(e, "navcollapse"),
                k = b.res(e, "navexpand"),
                l = "elfinder-subtree-loaded",
                m = b.res(e, "navarrow"),
                n = b.res(e, "active"),
                o = b.res(e, "adroppable"),
                p = b.res(e, "hover"),
                q = b.res(e, "disabled"),
                r = b.res(e, "draggable"),
                s = b.res(e, "droppable"),
                t = a.extend({}, b.droppable, {
                    hoverClass: p + " " + o,
                    over: function () {
                        var b = a(this);
                        b.is("." + j + ":not(." + k + ")") && setTimeout(function () {
                            b.is("." + o) && b.children("." + m).click()
                        }, 500)
                    }
                }),
                u = a(b.res("tpl", "navspinner")),
                v = b.res("tpl", "navdir"),
                w = b.res("tpl", "perms"),
                x = b.res("tpl", "symlink"),
                y = {
                    id: function (a) {
                        return b.navHash2Id(a.hash)
                    },
                    cssclass: function (a) {
                        return (a.phash ? "" : f) + " " + i + " " + b.perms2class(a) + " " + (a.dirs && !a.link ? j : "")
                    },
                    permissions: function (a) {
                        return !a.read || !a.write ? w : ""
                    },
                    symlink: function (a) {
                        return a.alias ? x : ""
                    }
                }, z = function (a) {
                    return a.name = b.escape(a.name), v.replace(/(?:\{([a-z]+)\})/ig, function (b, c) {
                        return a[c] || (y[c] ? y[c](a) : "")
                    })
                }, A = function (b) {
                    return a.map(b || [], function (a) {
                        return a.mime == "directory" ? a : null
                    })
                }, B = function (a) {
                    return a ? H.find("#" + b.navHash2Id(a)).next("." + h) : H
                }, C = function (c, d) {
                    var e = c.children(":first"),
                        f;
                    while (e.length) {
                        if ((f = b.file(b.navId2Hash(e.children("[id]").attr("id")))) && d.name.localeCompare(f.name) < 0) return e;
                        e = e.next()
                    }
                    return a("")
                }, D = function (a) {
                    var c = a.length,
                        d = [],
                        e, f, g, h, i;
                    for (e = 0; e < c; e++) {
                        f = a[e];
                        if (H.find("#" + b.navHash2Id(f.hash)).length) continue;
                        (h = B(f.phash)).length ? (g = z(f), f.phash && (i = C(h, f)).length ? i.before(g) : h.append(g)) : d.push(f)
                    }
                    if (d.length && d.length < c) return D(d);
                    F()
                }, E = function () {
                    var a = b.cwd().hash,
                        d = H.find("#" + b.navHash2Id(a)),
                        e;
                    g && (e = H.find("#" + b.navHash2Id(b.root())), e.is("." + l) && e.addClass(k).next("." + h).show(), g = !1), d.is("." + n) || (H.find("." + i + "." + n).removeClass(n), d.addClass(n)), c.syncTree && (d.length ? d.parentsUntil("." + f).filter("." + h).show().prev("." + i).addClass(k) : b.newAPI && b.request({
                        data: {
                            cmd: "parents",
                            target: a
                        },
                        preventFail: !0
                    }).done(function (c) {
                        var d = A(c.tree);
                        D(d), G(d, l), a == b.cwd().hash && E()
                    }))
                }, F = function () {
                    H.find("." + i + ":not(." + s + ",.elfinder-ro,.elfinder-na)").droppable(t)
                }, G = function (c, d) {
                    var e = d == l ? "." + j + ":not(." + l + ")" : ":not(." + j + ")";
                    a.each(c, function (c, f) {
                        H.find("#" + b.navHash2Id(f.phash) + e).filter(function () {
                            return a(this).next("." + h).children().length > 0
                        }).addClass(d)
                    })
                }, H = a(this).addClass(d).delegate("." + i, "hover", function (c) {
                    var d = a(this),
                        e = c.type == "mouseenter";
                    d.is("." + o + " ,." + q) || (e && !d.is("." + f + ",." + r + ",.elfinder-na,.elfinder-wo") && d.draggable(b.draggable), d.toggleClass(p, e))
                }).delegate("." + i, "dropover dropout drop", function (b) {
                    a(this)[b.type == "dropover" ? "addClass" : "removeClass"](o + " " + p)
                }).delegate("." + i, "click", function (c) {
                    var d = a(this),
                        e = b.navId2Hash(d.attr("id")),
                        f = b.file(e);
                    b.trigger("searchend"), e != b.cwd().hash && !d.is("." + q) ? b.exec("open", f.thash || e) : d.is("." + j) && d.children("." + m).click()
                }).delegate("." + i + "." + j + " ." + m, "click", function (c) {
                    var d = a(this),
                        e = d.parent("." + i),
                        f = e.next("." + h);
                    c.stopPropagation(), e.is("." + l) ? (e.toggleClass(k), f.slideToggle()) : (u.insertBefore(d), e.removeClass(j), b.request({
                        cmd: "tree",
                        target: b.navId2Hash(e.attr("id"))
                    }).done(function (a) {
                        D(A(a.tree)), f.children().length && (e.addClass(j + " " + k), f.slideDown()), E()
                    }).always(function (a) {
                        u.remove(), e.addClass(l)
                    }))
                }).delegate("." + i, "contextmenu", function (c) {
                    c.preventDefault(), b.trigger("contextmenu", {
                        type: "navbar",
                        targets: [b.navId2Hash(a(this).attr("id"))],
                        x: c.clientX,
                        y: c.clientY
                    })
                });
            H.parent().find(".elfinder-navbar").append(H).show(), b.open(function (a) {
                var b = a.data,
                    c = A(b.files);
                b.init && H.empty(), c.length && (D(c), G(c, l)), E()
            }).add(function (a) {
                var b = A(a.data.added);
                b.length && (D(b), G(b, j))
            }).change(function (c) {
                var d = A(c.data.changed),
                    e = d.length,
                    f, g, j, m, n, o, p, q, r;
                while (e--) {
                    f = d[e];
                    if ((g = H.find("#" + b.navHash2Id(f.hash))).length) {
                        if (f.phash) {
                            m = g.closest("." + h), n = B(f.phash), o = g.parent().next(), p = C(n, f);
                            if (!n.length) continue;
                            if (n[0] !== m[0] || o.get(0) !== p.get(0)) p.length ? p.before(g) : n.append(g)
                        }
                        q = g.is("." + k), r = g.is("." + l), j = a(z(f)), g.replaceWith(j.children("." + i)), f.dirs && (q || r) && (g = H.find("#" + b.navHash2Id(f.hash))) && g.next("." + h).children().length && (q && g.addClass(k), r && g.addClass(l))
                    }
                }
                E(), F()
            }).remove(function (a) {
                var c = a.data.removed,
                    d = c.length,
                    e, f;
                while (d--)(e = H.find("#" + b.navHash2Id(c[d]))).length && (f = e.closest("." + h), e.parent().detach(), f.children().length || f.hide().prev("." + i).removeClass(j + " " + k + " " + l))
            }).bind("search searchend", function (a) {
                H.find("#" + b.navHash2Id(b.cwd().hash))[a.type == "search" ? "removeClass" : "addClass"](n)
            }).bind("lockfiles unlockfiles", function (c) {
                var d = c.type == "lockfiles",
                    e = d ? "disable" : "enable",
                    f = a.map(c.data.files || [], function (a) {
                        var c = b.file(a);
                        return c && c.mime == "directory" ? a : null
                    });
                a.each(f, function (a, c) {
                    var f = H.find("#" + b.navHash2Id(c));
                    f.length && (f.is("." + r) && f.draggable(e), f.is("." + s) && f.droppable(n), f[d ? "addClass" : "removeClass"](q))
                })
            })
        }), this
    }, a.fn.elfinderuploadbutton = function (b) {
        return this.each(function () {
            var c = a(this).elfinderbutton(b).unbind("click"),
                d = a("<form/>").appendTo(c),
                e = a('<input type="file" multiple="true"/>').change(function () {
                    var c = a(this);
                    c.val() && (b.exec({
                        input: c.remove()[0]
                    }), e.clone(!0).appendTo(d))
                });
            d.append(e.clone(!0)), b.change(function () {
                d[b.disabled() ? "hide" : "show"]()
            }).change()
        })
    }, a.fn.elfinderviewbutton = function (b) {
        return this.each(function () {
            var c = a(this).elfinderbutton(b),
                d = c.children(".elfinder-button-icon");
            b.change(function () {
                var a = b.value == "icons";
                d.toggleClass("elfinder-button-icon-view-list", a), c.attr("title", b.fm.i18n(a ? "viewlist" : "viewicons"))
            })
        })
    }, a.fn.elfinderworkzone = function (b) {
        var c = "elfinder-workzone";
        return this.not("." + c).each(function () {
            var b = a(this).addClass(c),
                d = b.outerHeight(!0) - b.height(),
                e = b.parent();
            e.add(window).bind("resize", function () {
                var f = e.height();
                e.children(":visible:not(." + c + ")").each(function () {
                    var b = a(this);
                    b.css("position") != "absolute" && (f -= b.outerHeight(!0))
                }), b.height(f - d)
            })
        }), this
    }, elFinder.prototype.commands.archive = function () {
        var b = this,
            c = b.fm,
            d = [];
        this.variants = [], this.disableOnSearch = !0, c.bind("open reload", function () {
            b.variants = [], a.each(d = c.option("archivers").create || [], function (a, d) {
                b.variants.push([d, c.mime2kind(d)])
            }), b.change()
        }), this.getstate = function () {
            return !this._disabled && d.length && c.selected().length && c.cwd().write ? 0 : -1
        }, this.exec = function (b, e) {
            var f = this.files(b),
                g = f.length,
                h = e || d[0],
                i = c.cwd(),
                j = ["errArchive", "errPerm"],
                k = a.Deferred().fail(function (a) {
                    a && c.error(a)
                }),
                l;
            if (!(this.enabled() && g && d.length && a.inArray(h, d) !== -1)) return k.reject();
            if (!i.write) return k.reject(j);
            for (l = 0; l < g; l++) if (!f[l].read) return k.reject(j);
            return c.request({
                data: {
                    cmd: "archive",
                    targets: this.hashes(b),
                    type: h
                },
                notify: {
                    type: "archive",
                    cnt: 1
                },
                syncOnFail: !0
            })
        }
    }, elFinder.prototype.commands.back = function () {
        this.alwaysEnabled = !0, this.updateOnSelect = !1, this.shortcuts = [{
            pattern: "ctrl+left backspace"
        }], this.getstate = function () {
            return this.fm.history.canBack() ? 0 : -1
        }, this.exec = function () {
            return this.fm.history.back()
        }
    }, elFinder.prototype.commands.copy = function () {
        this.shortcuts = [{
            pattern: "ctrl+c ctrl+insert"
        }], this.getstate = function (b) {
            var b = this.files(b),
                c = b.length;
            return c && a.map(b, function (a) {
                return a.phash && a.read ? a : null
            }).length == c ? 0 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                d = a.Deferred().fail(function (a) {
                    c.error(a)
                });
            return a.each(this.files(b), function (a, b) {
                if (!b.read || !b.phash) return !d.reject(["errCopy", b.name, "errPerm"])
            }), d.isRejected() ? d : d.resolve(c.clipboard(this.hashes(b)))
        }
    }, elFinder.prototype.commands.cut = function () {
        this.shortcuts = [{
            pattern: "ctrl+x shift+insert"
        }], this.getstate = function (b) {
            var b = this.files(b),
                c = b.length;
            return c && a.map(b, function (a) {
                return a.phash && a.read && !a.locked ? a : null
            }).length == c ? 0 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                d = a.Deferred().fail(function (a) {
                    c.error(a)
                });
            return a.each(this.files(b), function (a, b) {
                if (!b.read || !b.phash) return !d.reject(["errCopy", b.name, "errPerm"]);
                if (b.locked) return !d.reject(["errLocked", b.name])
            }), d.isRejected() ? d : d.resolve(c.clipboard(this.hashes(b), !0))
        }
    }, elFinder.prototype.commands.download = function () {
        var b = this,
            c = this.fm,
            d = function (c) {
                return a.map(b.files(c), function (a) {
                    return a.mime == "directory" ? null : a
                })
            };
        this.shortcuts = [{
            pattern: "shift+enter"
        }], this.getstate = function () {
            var b = this.fm.selected(),
                c = b.length;
            return !this._disabled && c && (!a.browser.msie || c == 1) && c == d(b).length ? 0 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                e = c.options.url,
                f = d(b),
                g = a.Deferred(),
                h = "",
                i = "",
                j, k;
            if (this.disabled()) return g.reject();
            if (c.oldAPI) return c.error("errCmdNoSupport"), g.reject();
            a.each(c.options.customData || {}, function (a, b) {
                i += "&" + a + "=" + b
            }), e += e.indexOf("?") === -1 ? "?" : "&";
            for (j = 0; j < f.length; j++) h += '<iframe class="downloader" id="downloader-' + f[j].hash + '" style="display:none" src="' + e + "cmd=file&target=" + f[j].hash + "&download=1" + i + '"/>';
            return a(h).appendTo("body").ready(function () {
                setTimeout(function () {
                    a(h).each(function () {
                        a("#" + a(this).attr("id")).remove()
                    })
                }, a.browser.mozilla ? 2e4 + 1e4 * j : 1e3)
            }), c.trigger("download", {
                files: f
            }), g.resolve(b)
        }
    }, elFinder.prototype.commands.duplicate = function () {
        var b = this.fm;
        this.getstate = function (c) {
            var c = this.files(c),
                d = c.length;
            return !this._disabled && d && b.cwd().write && a.map(c, function (a) {
                return a.phash && a.read ? a : null
            }).length == d ? 0 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                d = this.files(b),
                e = d.length,
                f = a.Deferred().fail(function (a) {
                    a && c.error(a)
                }),
                g = [];
            return !e || this._disabled ? f.reject() : (a.each(d, function (a, b) {
                if (!b.read || !c.file(b.phash).write) return !f.reject(["errCopy", b.name, "errPerm"])
            }), f.isRejected() ? f : c.request({
                data: {
                    cmd: "duplicate",
                    targets: this.hashes(b)
                },
                notify: {
                    type: "copy",
                    cnt: e
                }
            }))
        }
    }, elFinder.prototype.commands.edit = function () {
        var b = this,
            c = this.fm,
            d = c.res("mimes", "text") || [],
            e = function (c) {
                return a.map(c, function (c) {
                    return (c.mime.indexOf("text/") === 0 || a.inArray(c.mime, d) !== -1) && c.mime.indexOf("text/rtf") && (!b.onlyMimes.length || a.inArray(c.mime, b.onlyMimes) !== -1) && c.read && c.write ? c : null
                })
            }, f = function (d, e, f) {
                var g = a.Deferred(),
                    h = a('<textarea class="elfinder-file-edit" rows="20" id="' + d + '-ta">' + c.escape(f) + "</textarea>"),
                    i = function () {
                        h.editor && h.editor.save(h[0], h.editor.instance), g.resolve(h.getContent()), h.elfinderdialog("close")
                    }, j = function () {
                        g.reject(), h.elfinderdialog("close")
                    }, k = {
                        title: e.name,
                        width: b.options.dialogWidth || 450,
                        buttons: {},
                        close: function () {
                            h.editor && h.editor.close(h[0], h.editor.instance), a(this).elfinderdialog("destroy")
                        },
                        open: function () {
                            c.disable(), h.focus(), h[0].setSelectionRange && h[0].setSelectionRange(0, 0), h.editor && h.editor.load(h[0])
                        }
                    };
                return h.getContent = function () {
                    return h.val()
                }, a.each(b.options.editors || [], function (b, c) {
                    if (a.inArray(e.mime, c.mimes || []) !== -1 && typeof c.load == "function" && typeof c.save == "function") return h.editor = {
                        load: c.load,
                        save: c.save,
                        close: typeof c.close == "function" ? c.close : function () {},
                        instance: null
                    }, !1
                }), h.editor || h.keydown(function (a) {
                    var b = a.keyCode,
                        c, d;
                    a.stopPropagation(), b == 9 && (a.preventDefault(), this.setSelectionRange && (c = this.value, d = this.selectionStart, this.value = c.substr(0, d) + "	" + c.substr(this.selectionEnd), d += 1, this.setSelectionRange(d, d)));
                    if (a.ctrlKey || a.metaKey) {
                        if (b == 81 || b == 87) a.preventDefault(), j();
                        b == 83 && (a.preventDefault(), i())
                    }
                }), k.buttons[c.i18n("Save")] = i, k.buttons[c.i18n("Cancel")] = j, c.dialog(h, k).attr("id", d), g.promise()
            }, g = function (b) {
                var d = b.hash,
                    e = c.options,
                    g = a.Deferred(),
                    h = {
                        cmd: "file",
                        target: d
                    }, i = c.url(d) || c.options.url,
                    j = "edit-" + c.namespace + "-" + b.hash,
                    k = c.getUI().find("#" + j),
                    l;
                return k.length ? (k.elfinderdialog("toTop"), g.resolve()) : !b.read || !b.write ? (l = ["errOpen", b.name, "errPerm"], c.error(l), g.reject(l)) : (c.request({
                    data: {
                        cmd: "get",
                        target: d
                    },
                    notify: {
                        type: "openfile",
                        cnt: 1
                    },
                    syncOnFail: !0
                }).done(function (a) {
                    f(j, b, a.content).done(function (a) {
                        c.request({
                            options: {
                                type: "post"
                            },
                            data: {
                                cmd: "put",
                                target: d,
                                content: a
                            },
                            notify: {
                                type: "save",
                                cnt: 1
                            },
                            syncOnFail: !0
                        }).fail(function (a) {
                            g.reject(a)
                        }).done(function (a) {
                            a.changed && a.changed.length && c.change(a), g.resolve(a)
                        })
                    })
                }).fail(function (a) {
                    g.reject(a)
                }), g.promise())
            };
        this.shortcuts = [{
            pattern: "ctrl+e"
        }], this.init = function () {
            this.onlyMimes = this.options.mimes || []
        }, this.getstate = function (a) {
            var a = this.files(a),
                b = a.length;
            return !this._disabled && b && e(a).length == b ? 0 : -1
        }, this.exec = function (b) {
            var c = e(this.files(b)),
                d = [],
                f;
            if (this.disabled()) return a.Deferred().reject();
            while (f = c.shift()) d.push(g(f));
            return d.length ? a.when.apply(null, d) : a.Deferred().reject()
        }
    }, elFinder.prototype.commands.extract = function () {
        var b = this,
            c = b.fm,
            d = [],
            e = function (b) {
                return a.map(b, function (b) {
                    return b.read && a.inArray(b.mime, d) !== -1 ? b : null
                })
            };
        this.disableOnSearch = !0, c.bind("open reload", function () {
            d = c.option("archivers").extract || [], b.change()
        }), this.getstate = function (a) {
            var a = this.files(a),
                b = a.length;
            return !this._disabled && b && e(a).length == b ? 0 : -1
        }, this.exec = function (b) {
            var e = this.files(b),
                f = a.Deferred(),
                g = e.length,
                h = g,
                i, j, k;
            if (!(this.enabled() && g && d.length)) return f.reject();
            for (i = 0; i < g; i++) {
                j = e[i];
                if (!j.read || !c.file(j.phash).write) return k = ["errExtract", j.name, "errPerm"], c.error(k), f.reject(k);
                if (a.inArray(j.mime, d) === -1) return k = ["errExtract", j.name, "errNoArchive"], c.error(k), f.reject(k);
                c.request({
                    data: {
                        cmd: "extract",
                        target: j.hash
                    },
                    notify: {
                        type: "extract",
                        cnt: 1
                    },
                    syncOnFail: !0
                }).fail(function (a) {
                    f.isRejected() || f.reject(a)
                }).done(function () {
                    h--, h == 0 && f.resolve()
                })
            }
            return f
        }
    }, elFinder.prototype.commands.forward = function () {
        this.alwaysEnabled = !0, this.updateOnSelect = !0, this.shortcuts = [{
            pattern: "ctrl+right"
        }], this.getstate = function () {
            return this.fm.history.canForward() ? 0 : -1
        }, this.exec = function () {
            return this.fm.history.forward()
        }
    }, elFinder.prototype.commands.getfile = function () {
        var b = this,
            c = this.fm,
            d = function (c) {
                var d = b.options;
                return c = a.map(c, function (a) {
                    return a.mime != "directory" || d.folders ? a : null
                }), d.multiple || c.length == 1 ? c : []
            };
        this.alwaysEnabled = !0, this.callback = c.options.getFileCallback, this._disabled = typeof this.callback == "function", this.getstate = function (a) {
            var a = this.files(a),
                b = a.length;
            return this.callback && b && d(a).length == b ? 0 : -1
        }, this.exec = function (c) {
            var d = this.fm,
                e = this.options,
                f = this.files(c),
                g = f.length,
                h = d.option("url"),
                i = d.option("tmbUrl"),
                j = a.Deferred().done(function (a) {
                    d.trigger("getfile", {
                        files: a
                    }), b.callback(a, d), e.oncomplete == "close" ? d.hide() : e.oncomplete == "destroy" && d.destroy()
                }),
                k = function (b) {
                    return e.onlyURL ? e.multiple ? a.map(f, function (a) {
                        return a.url
                    }) : f[0].url : e.multiple ? f : f[0]
                }, l = [],
                m, n, o;
            if (this.getstate() == -1) return j.reject();
            for (m = 0; m < g; m++) {
                n = f[m];
                if (n.mime == "directory" && !e.folders) return j.reject();
                n.baseUrl = h, n.url = d.url(n.hash), n.path = d.path(n.hash), n.tmb && n.tmb != 1 && (n.tmb = i + n.tmb), !n.width && !n.height && (n.dim ? (o = n.dim.split("x"), n.width = o[0], n.height = o[1]) : n.mime.indexOf("image") !== -1 && l.push(d.request({
                    data: {
                        cmd: "dim",
                        target: n.hash
                    },
                    preventDefault: !0
                }).done(a.proxy(function (a) {
                    a.dim && (o = a.dim.split("x"), this.width = o[0], this.height = o[1]), this.dim = a.dim
                }, f[m]))))
            }
            return l.length ? (a.when.apply(null, l).always(function () {
                j.resolve(k(f))
            }), j) : j.resolve(k(f))
        }
    }, elFinder.prototype.commands.help = function () {
        var b = this.fm,
            c = this,
            d = '<div class="elfinder-help-link"> <a href="{url}">{link}</a></div>',
            e = '<div class="elfinder-help-team"><div>{author}</div>{work}</div>',
            f = /\{url\}/,
            g = /\{link\}/,
            h = /\{author\}/,
            i = /\{work\}/,
            j = "replace",
            k = "ui-priority-primary",
            l = "ui-priority-secondary",
            m = "elfinder-help-license",
            n = '<li class="ui-state-default ui-corner-top"><a href="#{id}">{title}</a></li>',
            o = ['<div class="ui-tabs ui-widget ui-widget-content ui-corner-all elfinder-help">', '<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">'],
            p = '<div class="elfinder-help-shortcut"><div class="elfinder-help-shortcut-pattern">{pattern}</div> {descrip}</div>',
            q = '<div class="elfinder-help-separator"/>',
            r = function () {
                o.push('<div id="about" class="ui-tabs-panel ui-widget-content ui-corner-bottom"><div class="elfinder-help-logo"/>'), o.push("<h3>elFinder</h3>"), o.push('<div class="' + k + '">' + b.i18n("webfm") + "</div>"), o.push('<div class="' + l + '">' + b.i18n("ver") + ": " + b.version + ", " + b.i18n("protocol") + ": " + b.api + "</div>"), o.push('<div class="' + l + '">jQuery/jQuery UI: ' + a().jquery + "/" + a.ui.version + "</div>"), o.push(q), o.push(d[j](f, "http://elfinder.org/")[j](g, b.i18n("homepage"))), o.push(d[j](f, "https://github.com/Studio-42/elFinder/wiki")[j](g, b.i18n("docs"))), o.push(d[j](f, "https://github.com/Studio-42/elFinder")[j](g, b.i18n("github"))), o.push(d[j](f, "http://twitter.com/elrte_elfinder")[j](g, b.i18n("twitter"))), o.push(q), o.push('<div class="' + k + '">' + b.i18n("team") + "</div>"), o.push(e[j](h, 'Dmitry "dio" Levashov &lt;dio@std42.ru&gt;')[j](i, b.i18n("chiefdev"))), o.push(e[j](h, "Troex Nevelin &lt;troex@fury.scancode.ru&gt;")[j](i, b.i18n("maintainer"))), o.push(e[j](h, "Alexey Sukhotin &lt;strogg@yandex.ru&gt;")[j](i, b.i18n("contributor"))), o.push(e[j](h, "Naoki Sawada &lt;hypweb@gmail.com&gt;")[j](i, b.i18n("contributor"))), b.i18[b.lang].translator && o.push(e[j](h, b.i18[b.lang].translator)[j](i, b.i18n("translator") + " (" + b.i18[b.lang].language + ")")), o.push(q), o.push('<div class="' + m + '">' + b.i18n("icons") + ': <a href="http://pixelmixer.ru/" target="_blank">Pixelmixer</a>, <a href="http://p.yusukekamiyamane.com" target="_blank">Fugue</a></div>'), o.push(q), o.push('<div class="' + m + '">Licence: BSD Licence</div>'), o.push('<div class="' + m + '">Copyright © 2009-2011, Studio 42</div>'), o.push('<div class="' + m + '">„ …' + b.i18n("dontforget") + " ”</div>"), o.push("</div>")
            }, s = function () {
                var c = b.shortcuts();
                o.push('<div id="shortcuts" class="ui-tabs-panel ui-widget-content ui-corner-bottom">'), c.length ? (o.push('<div class="ui-widget-content elfinder-help-shortcuts">'), a.each(c, function (a, b) {
                    o.push(p.replace(/\{pattern\}/, b[0]).replace(/\{descrip\}/, b[1]))
                }), o.push("</div>")) : o.push('<div class="elfinder-help-disabled">' + b.i18n("shortcutsof") + "</div>"), o.push("</div>")
            }, t = function () {
                o.push('<div id="help" class="ui-tabs-panel ui-widget-content ui-corner-bottom">'), o.push('<a href="http://elfinder.org/forum/" target="_blank" class="elfinder-dont-panic"><span>DON\'T PANIC</span></a>'), o.push("</div>")
            }, u;
        this.alwaysEnabled = !0, this.updateOnSelect = !1, this.state = 0, this.shortcuts = [{
            pattern: "f1",
            description: this.title
        }], setTimeout(function () {
            var d = c.options.view || ["about", "shortcuts", "help"];
            a.each(d, function (a, c) {
                o.push(n[j](/\{id\}/, c)[j](/\{title\}/, b.i18n(c)))
            }), o.push("</ul>"), a.inArray("about", d) !== -1 && r(), a.inArray("shortcuts", d) !== -1 && s(), a.inArray("help", d) !== -1 && t(), o.push("</div>"), u = a(o.join("")), u.find(".ui-tabs-nav li").hover(function () {
                a(this).toggleClass("ui-state-hover")
            }).children().click(function (b) {
                var c = a(this);
                b.preventDefault(), b.stopPropagation(), c.is(".ui-tabs-selected") || (c.parent().addClass("ui-tabs-selected ui-state-active").siblings().removeClass("ui-tabs-selected").removeClass("ui-state-active"), u.find(".ui-tabs-panel").hide().filter(c.attr("href")).show())
            }).filter(":first").click()
        }, 200), this.getstate = function () {
            return 0
        }, this.exec = function () {
            this.dialog || (this.dialog = this.fm.dialog(u, {
                title: this.title,
                width: 530,
                autoOpen: !1,
                destroyOnClose: !1
            })), this.dialog.elfinderdialog("open").find(".ui-tabs-nav li a:first").click()
        }
    }, elFinder.prototype.commands.home = function () {
        this.title = "Home", this.alwaysEnabled = !0, this.updateOnSelect = !1, this.shortcuts = [{
            pattern: "ctrl+home ctrl+shift+up",
            description: "Home"
        }], this.getstate = function () {
            var a = this.fm.root(),
                b = this.fm.cwd().hash;
            return a && b && a != b ? 0 : -1
        }, this.exec = function () {
            return this.fm.exec("open", this.fm.root())
        }
    }, elFinder.prototype.commands.info = function () {
        var b = "msg",
            c = this.fm,
            d = "elfinder-info-spinner",
            e = {
                calc: c.i18n("calc"),
                size: c.i18n("size"),
                unknown: c.i18n("unknown"),
                path: c.i18n("path"),
                aliasfor: c.i18n("aliasfor"),
                modify: c.i18n("modify"),
                perms: c.i18n("perms"),
                locked: c.i18n("locked"),
                dim: c.i18n("dim"),
                kind: c.i18n("kind"),
                files: c.i18n("files"),
                folders: c.i18n("folders"),
                items: c.i18n("items"),
                yes: c.i18n("yes"),
                no: c.i18n("no"),
                link: c.i18n("link")
            };
        this.tpl = {
            main: '<div class="ui-helper-clearfix elfinder-info-title"><span class="elfinder-cwd-icon {class} ui-corner-all"/>{title}</div><table class="elfinder-info-tb">{content}</table>',
            itemTitle: '<strong>{name}</strong><span class="elfinder-info-kind">{kind}</span>',
            groupTitle: "<strong>{items}: {num}</strong>",
            row: "<tr><td>{label} : </td><td>{value}</td></tr>",
            spinner: '<span>{text}</span> <span class="' + d + '"/>'
        }, this.alwaysEnabled = !0, this.updateOnSelect = !1, this.shortcuts = [{
            pattern: "ctrl+i"
        }], this.init = function () {
            a.each(e, function (a, b) {
                e[a] = c.i18n(b)
            })
        }, this.getstate = function () {
            return 0
        }, this.exec = function (b) {
            var c = this,
                f = this.fm,
                g = this.tpl,
                h = g.row,
                i = this.files(b),
                j = i.length,
                k = [],
                l = g.main,
                m = "{label}",
                n = "{value}",
                o = {
                    title: this.title,
                    width: "auto",
                    close: function () {
                        a(this).elfinderdialog("destroy")
                    }
                }, p = [],
                q = function (a) {
                    s.find("." + d).parent().text(a)
                }, r = f.namespace + "-info-" + a.map(i, function (a) {
                    return a.hash
                }).join("-"),
                s = f.getUI().find("#" + r),
                t, u, v, w, x;
            if (!j) return a.Deferred().reject();
            if (s.length) return s.elfinderdialog("toTop"), a.Deferred().resolve();
            j == 1 ? (v = i[0], l = l.replace("{class}", f.mime2class(v.mime)), w = g.itemTitle.replace("{name}", v.name).replace("{kind}", f.mime2kind(v)), v.tmb && (u = f.option("tmbUrl") + v.tmb), v.read ? v.mime != "directory" || v.alias ? t = f.formatSize(v.size) : (t = g.spinner.replace("{text}", e.calc), p.push(v.hash)) : t = e.unknown, k.push(h.replace(m, e.size).replace(n, t)), v.alias && k.push(h.replace(m, e.aliasfor).replace(n, v.alias)), k.push(h.replace(m, e.path).replace(n, f.escape(f.path(v.hash)))), v.read && k.push(h.replace(m, e.link).replace(n, '<a href="' + f.url(v.hash) + '" target="_blank">' + v.name + "</a>")), v.dim ? k.push(h.replace(m, e.dim).replace(n, v.dim)) : v.mime.indexOf("image") !== -1 && (v.width && v.height ? k.push(h.replace(m, e.dim).replace(n, v.width + "x" + v.height)) : (k.push(h.replace(m, e.dim).replace(n, g.spinner.replace("{text}", e.calc))), f.request({
                data: {
                    cmd: "dim",
                    target: v.hash
                },
                preventDefault: !0
            }).fail(function () {
                q(e.unknown)
            }).done(function (a) {
                q(a.dim || e.unknown)
            }))), k.push(h.replace(m, e.modify).replace(n, f.formatDate(v))), k.push(h.replace(m, e.perms).replace(n, f.formatPermissions(v))), k.push(h.replace(m, e.locked).replace(n, v.locked ? e.yes : e.no))) : (l = l.replace("{class}", "elfinder-cwd-icon-group"), w = g.groupTitle.replace("{items}", e.items).replace("{num}", j), x = a.map(i, function (a) {
                return a.mime == "directory" ? 1 : null
            }).length, x ? (k.push(h.replace(m, e.kind).replace(n, x == j ? e.folders : e.folders + " " + x + ", " + e.files + " " + (j - x))), k.push(h.replace(m, e.size).replace(n, g.spinner.replace("{text}", e.calc))), p = a.map(i, function (a) {
                return a.hash
            })) : (t = 0, a.each(i, function (a, b) {
                var c = parseInt(b.size);
                c >= 0 && t >= 0 ? t += c : t = "unknown"
            }), k.push(h.replace(m, e.kind).replace(n, e.files)), k.push(h.replace(m, e.size).replace(n, f.formatSize(t))))), l = l.replace("{title}", w).replace("{content}", k.join("")), s = f.dialog(l, o), s.attr("id", r), u && a("<img/>").load(function () {
                s.find(".elfinder-cwd-icon").css("background", 'url("' + u + '") center center no-repeat')
            }).attr("src", u), p.length && f.request({
                data: {
                    cmd: "size",
                    targets: p
                },
                preventDefault: !0
            }).fail(function () {
                q(e.unknown)
            }).done(function (a) {
                var b = parseInt(a.size);
                f.log(a.size), q(b >= 0 ? f.formatSize(b) : e.unknown)
            })
        }
    }, elFinder.prototype.commands.mkdir = function () {
        this.disableOnSearch = !0, this.updateOnSelect = !1, this.mime = "directory", this.prefix = "untitled folder", this.exec = a.proxy(this.fm.res("mixin", "make"), this), this.shortcuts = [{
            pattern: "ctrl+shift+n"
        }], this.getstate = function () {
            return !this._disabled && this.fm.cwd().write ? 0 : -1
        }
    }, elFinder.prototype.commands.mkfile = function () {
        this.disableOnSearch = !0, this.updateOnSelect = !1, this.mime = "text/plain", this.prefix = "untitled file.txt", this.exec = a.proxy(this.fm.res("mixin", "make"), this), this.getstate = function () {
            return !this._disabled && this.fm.cwd().write ? 0 : -1
        }
    }, elFinder.prototype.commands.open = function () {
        this.alwaysEnabled = !0, this._handlers = {
            dblclick: function (a) {
                a.preventDefault(), this.exec()
            },
            "select enable disable reload": function (a) {
                this.update(a.type == "disable" ? -1 : void 0)
            }
        }, this.shortcuts = [{
            pattern: "ctrl+down numpad_enter" + (this.fm.OS != "mac" && " enter")
        }], this.getstate = function (b) {
            var b = this.files(b),
                c = b.length;
            return c == 1 ? 0 : c ? a.map(b, function (a) {
                return a.mime == "directory" ? null : a
            }).length == c ? 0 : -1 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                d = a.Deferred().fail(function (a) {
                    a && c.error(a)
                }),
                e = this.files(b),
                f = e.length,
                g, h, i, j;
            if (!f) return d.reject();
            if (f == 1 && (g = e[0]) && g.mime == "directory") return g && !g.read ? d.reject(["errOpen", g.name, "errPerm"]) : c.request({
                data: {
                    cmd: "open",
                    target: g.thash || g.hash
                },
                notify: {
                    type: "open",
                    cnt: 1,
                    hideCnt: !0
                },
                syncOnFail: !0
            });
            e = a.map(e, function (a) {
                return a.mime != "directory" ? a : null
            });
            if (f != e.length) return d.reject();
            f = e.length;
            while (f--) {
                g = e[f];
                if (!g.read) return d.reject(["errOpen", g.name, "errPerm"]);
                (h = c.url(g.hash)) || (h = c.options.url, h = h + (h.indexOf("?") === -1 ? "?" : "&") + (c.oldAPI ? "cmd=open&current=" + g.phash : "cmd=file") + "&target=" + g.hash), j = "", g.dim && (i = g.dim.split("x"), j = "width=" + (parseInt(i[0]) + 20) + ",height=" + (parseInt(i[1]) + 20));
                if (!window.open(h, "_blank", j + ",top=50,left=50,scrollbars=yes,resizable=yes")) return d.reject("errPopup")
            }
            return d.resolve(b)
        }
    }, elFinder.prototype.commands.paste = function () {
        this.disableOnSearch = !0, this.updateOnSelect = !1, this.handlers = {
            changeclipboard: function () {
                this.update()
            }
        }, this.shortcuts = [{
            pattern: "ctrl+v shift+insert"
        }], this.getstate = function (b) {
            if (this._disabled) return -1;
            if (b) {
                if (a.isArray(b)) {
                    if (b.length != 1) return -1;
                    b = this.fm.file(b[0])
                }
            } else b = this.fm.cwd();
            return this.fm.clipboard().length && b.mime == "directory" && b.write ? 0 : -1
        }, this.exec = function (b) {
            var c = this,
                d = c.fm,
                b = b ? this.files(b)[0] : d.cwd(),
                e = d.clipboard(),
                f = e.length,
                g = f ? e[0].cut : !1,
                h = g ? "errMove" : "errCopy",
                i = [],
                j = [],
                k = a.Deferred().fail(function (a) {
                    a && d.error(a)
                }),
                l = function (b) {
                    return b.length && d._commands.duplicate ? d.exec("duplicate", b) : a.Deferred().resolve()
                }, m = function (e) {
                    var f = a.Deferred(),
                        h = [],
                        i = function (b, c) {
                            var d = [],
                                e = b.length;
                            while (e--) a.inArray(b[e].name, c) !== -1 && d.unshift(e);
                            return d
                        }, j = function (a) {
                            var b = h[a],
                                c = e[b],
                                i = a == h.length - 1;
                            if (!c) return;
                            d.confirm({
                                title: d.i18n(g ? "moveFiles" : "copyFiles"),
                                text: d.i18n(["errExists", c.name, "confirmRepl"]),
                                all: !i,
                                accept: {
                                    label: "btnYes",
                                    callback: function (b) {
                                        !i && !b ? j(++a) : l(e)
                                    }
                                },
                                reject: {
                                    label: "btnNo",
                                    callback: function (b) {
                                        var c;
                                        if (b) {
                                            c = h.length;
                                            while (a < c--) e[h[c]].remove = !0
                                        } else e[h[a]].remove = !0;
                                        !i && !b ? j(++a) : l(e)
                                    }
                                },
                                cancel: {
                                    label: "btnCancel",
                                    callback: function () {
                                        f.resolve()
                                    }
                                }
                            })
                        }, k = function (a) {
                            h = i(e, a), h.length ? j(0) : l(e)
                        }, l = function (c) {
                            var c = a.map(c, function (a) {
                                return a.remove ? null : a
                            }),
                                e = c.length,
                                h = {}, i = [],
                                j;
                            if (!e) return f.resolve();
                            j = c[0].phash, c = a.map(c, function (a) {
                                return a.hash
                            }), d.request({
                                data: {
                                    cmd: "paste",
                                    dst: b.hash,
                                    targets: c,
                                    cut: g ? 1 : 0,
                                    src: j
                                },
                                notify: {
                                    type: g ? "move" : "copy",
                                    cnt: e
                                }
                            }).always(function () {
                                d.unlockfiles({
                                    files: c
                                })
                            })
                        };
                    return c._disabled || !e.length ? f.resolve() : (d.oldAPI ? l(e) : d.option("copyOverwrite") ? b.hash == d.cwd().hash ? k(a.map(d.files(), function (a) {
                        return a.phash == b.hash ? a.name : null
                    })) : d.request({
                        data: {
                            cmd: "ls",
                            target: b.hash
                        },
                        notify: {
                            type: "prepare",
                            cnt: 1,
                            hideCnt: !0
                        },
                        preventFail: !0
                    }).always(function (a) {
                        k(a.list || [])
                    }) : l(e), f)
                }, n, o;
            return !f || !b || b.mime != "directory" ? k.reject() : b.write ? (n = d.parents(b.hash), a.each(e, function (c, f) {
                if (!f.read) return !k.reject([h, e[0].name, "errPerm"]);
                if (g && f.locked) return !k.reject(["errLocked", f.name]);
                if (a.inArray(f.hash, n) !== -1) return !k.reject(["errCopyInItself", f.name]);
                o = d.parents(f.hash);
                if (a.inArray(b.hash, o) !== -1 && a.map(o, function (a) {
                    var c = d.file(a);
                    return c.phash == b.hash && c.name == f.name ? c : null
                }).length) return !k.reject(["errReplByChild", f.name]);
                f.phash == b.hash ? j.push(f.hash) : i.push({
                    hash: f.hash,
                    phash: f.phash,
                    name: f.name
                })
            }), k.isRejected() ? k : a.when(l(j), m(i)).always(function () {
                g && d.clipboard([])
            })) : k.reject([h, e[0].name, "errPerm"])
        }
    }, elFinder.prototype.commands.quicklook = function () {
        var b = this,
            c = b.fm,
            d = 0,
            e = 1,
            f = 2,
            g = d,
            h = "elfinder-quicklook-navbar-icon",
            i = "elfinder-quicklook-fullscreen",
            j = function (b) {
                a(document).trigger(a.Event("keydown", {
                    keyCode: b,
                    ctrlKey: !1,
                    shiftKey: !1,
                    altKey: !1,
                    metaKey: !1
                }))
            }, k = function (a) {
                return {
                    opacity: 0,
                    width: 20,
                    height: c.view == "list" ? 1 : 20,
                    top: a.offset().top + "px",
                    left: a.offset().left + "px"
                }
            }, l = function () {
                var b = a(window);
                return {
                    opacity: 1,
                    width: n,
                    height: o,
                    top: parseInt((b.height() - o) / 2 + b.scrollTop()),
                    left: parseInt((b.width() - n) / 2 + b.scrollLeft())
                }
            }, m = function (a) {
                var b = document.createElement(a.substr(0, a.indexOf("/"))),
                    c = b.canPlayType && b.canPlayType(a);
                return c && c !== "" && c != "no"
            }, n, o, p, q, r = a('<div class="elfinder-quicklook-title"/>'),
            s = a("<div/>"),
            t = a('<div class="elfinder-quicklook-info"/>'),
            u = a('<div class="' + h + " " + h + '-fullscreen"/>').mousedown(function (d) {
                var e = b.window,
                    f = e.is("." + i),
                    g = "scroll." + c.namespace,
                    j = a(window);
                d.stopPropagation(), f ? (e.css(e.data("position")).unbind("mousemove"), j.unbind(g).trigger(b.resize).unbind(b.resize), v.unbind("mouseenter").unbind("mousemove")) : (e.data("position", {
                    left: e.css("left"),
                    top: e.css("top"),
                    width: e.width(),
                    height: e.height()
                }).css({
                    width: "100%",
                    height: "100%"
                }), a(window).bind(g, function () {
                    e.css({
                        left: parseInt(a(window).scrollLeft()) + "px",
                        top: parseInt(a(window).scrollTop()) + "px"
                    })
                }).bind(b.resize, function (a) {
                    b.preview.trigger("changesize")
                }).trigger(g).trigger(b.resize), e.bind("mousemove", function (a) {
                    v.stop(!0, !0).show().delay(3e3).fadeOut("slow")
                }).mousemove(), v.mouseenter(function () {
                    v.stop(!0, !0).show()
                }).mousemove(function (a) {
                    a.stopPropagation()
                })), v.attr("style", "").draggable(f ? "destroy" : {}), e.toggleClass(i), a(this).toggleClass(h + "-fullscreen-off"), a.fn.resizable && p.add(e).resizable(f ? "enable" : "disable").removeClass("ui-state-disabled")
            }),
            v = a('<div class="elfinder-quicklook-navbar"/>').append(a('<div class="' + h + " " + h + '-prev"/>').mousedown(function () {
                j(37)
            })).append(u).append(a('<div class="' + h + " " + h + '-next"/>').mousedown(function () {
                j(39)
            })).append('<div class="elfinder-quicklook-navbar-separator"/>').append(a('<div class="' + h + " " + h + '-close"/>').mousedown(function () {
                b.window.trigger("close")
            }));
        this.resize = "resize." + c.namespace, this.info = a('<div class="elfinder-quicklook-info-wrapper"/>').append(s).append(t), this.preview = a('<div class="elfinder-quicklook-preview ui-helper-clearfix"/>').bind("change", function (a) {
            b.info.attr("style", "").hide(), s.removeAttr("class").attr("style", ""), t.html("")
        }).bind("update", function (c) {
            var d = b.fm,
                e = b.preview,
                f = c.file,
                g = '<div class="elfinder-quicklook-info-data">{value}</div>',
                h;
            f ? (!f.read && c.stopImmediatePropagation(), b.window.data("hash", f.hash), b.preview.unbind("changesize").trigger("change").children().remove(), r.html(d.escape(f.name)), t.html(g.replace(/\{value\}/, f.name) + g.replace(/\{value\}/, d.mime2kind(f)) + (f.mime == "directory" ? "" : g.replace(/\{value\}/, d.formatSize(f.size))) + g.replace(/\{value\}/, d.i18n("modify") + ": " + d.formatDate(f.date))), s.addClass("elfinder-cwd-icon ui-corner-all " + d.mime2class(f.mime)), f.tmb && a("<img/>").hide().appendTo(b.preview).load(function () {
                s.css("background", 'url("' + h + '") center center no-repeat'), a(this).remove()
            }).attr("src", h = d.tmb(f.hash)), b.info.delay(100).fadeIn(10)) : c.stopImmediatePropagation()
        }), this.window = a('<div class="ui-helper-reset ui-widget elfinder-quicklook" style="position:absolute"/>').click(function (a) {
            a.stopPropagation()
        }).append(a('<div class="elfinder-quicklook-titlebar"/>').append(r).append(a('<span class="ui-icon ui-icon-circle-close"/>').mousedown(function (a) {
            a.stopPropagation(), b.window.trigger("close")
        }))).append(this.preview.add(v)).append(b.info.hide()).draggable({
            handle: "div.elfinder-quicklook-titlebar"
        }).bind("open", function (a) {
            var c = b.window,
                d = b.value,
                h;
            b.closed() && d && (h = q.find("#" + d.hash)).length && (g = e, h.trigger("scrolltoview"), c.css(k(h)).show().animate(l(), 550, function () {
                g = f, b.update(1, b.value)
            }))
        }).bind("close", function (a) {
            var c = b.window,
                f = b.preview.trigger("change"),
                h = b.value,
                j = q.find("#" + c.data("hash")),
                l = function () {
                    g = d, c.hide(), f.children().remove(), b.update(0, b.value)
                };
            b.opened() && (g = e, c.is("." + i) && u.mousedown(), j.length ? c.animate(k(j), 500, l) : l())
        }), this.alwaysEnabled = !0, this.value = null, this.handlers = {
            select: function () {
                this.update(void 0, this.fm.selectedFiles()[0])
            },
            error: function () {
                b.window.is(":visible") && b.window.data("hash", "").trigger("close")
            },
            "searchshow searchhide": function () {
                this.opened() && this.window.trigger("close")
            }
        }, this.shortcuts = [{
            pattern: "space"
        }], this.support = {
            audio: {
                ogg: m('audio/ogg; codecs="vorbis"'),
                mp3: m("audio/mpeg;"),
                wav: m('audio/wav; codecs="1"'),
                m4a: m("audio/x-m4a;") || m("audio/aac;")
            },
            video: {
                ogg: m('video/ogg; codecs="theora"'),
                webm: m('video/webm; codecs="vp8, vorbis"'),
                mp4: m('video/mp4; codecs="avc1.42E01E"') || m('video/mp4; codecs="avc1.42E01E, mp4a.40.2"')
            }
        }, this.closed = function () {
            return g == d
        }, this.opened = function () {
            return g == f
        }, this.init = function () {
            var d = this.options,
                e = this.window,
                f = this.preview,
                g, h;
            n = d.width > 0 ? parseInt(d.width) : 450, o = d.height > 0 ? parseInt(d.height) : 300, c.one("load", function () {
                p = c.getUI(), q = c.getUI("cwd"), e.appendTo("body").zIndex(100 + p.zIndex()), a(document).keydown(function (a) {
                    a.keyCode == 27 && b.opened() && e.trigger("close")
                }), a.fn.resizable && e.resizable({
                    handles: "se",
                    minWidth: 350,
                    minHeight: 120,
                    resize: function () {
                        f.trigger("changesize")
                    }
                }), b.change(function () {
                    b.opened() && (b.value ? f.trigger(a.Event("update", {
                        file: b.value
                    })) : e.trigger("close"))
                }), a.each(c.commands.quicklook.plugins || [], function (a, c) {
                    typeof c == "function" && new c(b)
                }), f.bind("update", function () {
                    b.info.show()
                })
            })
        }, this.getstate = function () {
            return this.fm.selected().length == 1 ? g == f ? 1 : 0 : -1
        }, this.exec = function () {
            this.enabled() && this.window.trigger(this.opened() ? "close" : "open")
        }, this.hideinfo = function () {
            this.info.stop(!0).hide()
        }
    }, elFinder.prototype.commands.quicklook.plugins = [function (b) {
        var c = ["image/jpeg", "image/png", "image/gif"],
            d = b.preview;
        a.each(navigator.mimeTypes, function (b, d) {
            var e = d.type;
            e.indexOf("image/") === 0 && a.inArray(e, c) && c.push(e)
        }), d.bind("update", function (e) {
            var f = e.file,
                g;
            a.inArray(f.mime, c) !== -1 && (e.stopImmediatePropagation(), g = a("<img/>").hide().appendTo(d).load(function () {
                setTimeout(function () {
                    var a = (g.width() / g.height()).toFixed(2);
                    d.bind("changesize", function () {
                        var b = parseInt(d.width()),
                            c = parseInt(d.height()),
                            e, f;
                        a < (b / c).toFixed(2) ? (f = c, e = Math.floor(f * a)) : (e = b, f = Math.floor(e / a)), g.width(e).height(f).css("margin-top", f < c ? Math.floor((c - f) / 2) : 0)
                    }).trigger("changesize"), b.hideinfo(), g.fadeIn(100)
                }, 1)
            }).attr("src", b.fm.url(f.hash)))
        })
    }, function (b) {
        var c = ["text/html", "application/xhtml+xml"],
            d = b.preview,
            e = b.fm;
        d.bind("update", function (f) {
            var g = f.file,
                h;
            a.inArray(g.mime, c) !== -1 && (f.stopImmediatePropagation(), d.one("change", function () {
                !h.isResolved() && !h.isRejected() && h.reject()
            }), h = e.request({
                data: {
                    cmd: "get",
                    target: g.hash,
                    current: g.phash
                },
                preventDefault: !0
            }).done(function (c) {
                b.hideinfo(), doc = a('<iframe class="elfinder-quicklook-preview-html"/>').appendTo(d)[0].contentWindow.document, doc.open(), doc.write(c.content), doc.close()
            }))
        })
    }, function (b) {
        var c = b.fm,
            d = c.res("mimes", "text"),
            e = b.preview;
        e.bind("update", function (f) {
            var g = f.file,
                h = g.mime,
                i;
            if (h.indexOf("text/") === 0 || a.inArray(h, d) !== -1) f.stopImmediatePropagation(), e.one("change", function () {
                !i.isResolved() && !i.isRejected() && i.reject()
            }), i = c.request({
                data: {
                    cmd: "get",
                    target: g.hash
                },
                preventDefault: !0
            }).done(function (d) {
                b.hideinfo(), a('<div class="elfinder-quicklook-preview-text-wrapper"><pre class="elfinder-quicklook-preview-text">' + c.escape(d.content) + "</pre></div>").appendTo(e)
            })
        })
    }, function (b) {
        var c = b.fm,
            d = "application/pdf",
            e = b.preview,
            f = !1;
        a.browser.safari && navigator.platform.indexOf("Mac") != -1 || a.browser.msie ? f = !0 : a.each(navigator.plugins, function (b, c) {
            a.each(c, function (a, b) {
                if (b.type == d) return !(f = !0)
            })
        }), f && e.bind("update", function (f) {
            var g = f.file,
                h;
            g.mime == d && (f.stopImmediatePropagation(), e.one("change", function () {
                h.unbind("load").remove()
            }), h = a('<iframe class="elfinder-quicklook-preview-pdf"/>').hide().appendTo(e).load(function () {
                b.hideinfo(), h.show()
            }).attr("src", c.url(g.hash)))
        })
    }, function (b) {
        var c = b.fm,
            d = "application/x-shockwave-flash",
            e = b.preview,
            f = !1;
        a.each(navigator.plugins, function (b, c) {
            a.each(c, function (a, b) {
                if (b.type == d) return !(f = !0)
            })
        }), f && e.bind("update", function (f) {
            var g = f.file,
                h;
            g.mime == d && (f.stopImmediatePropagation(), b.hideinfo(), e.append(h = a('<embed class="elfinder-quicklook-preview-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" src="' + c.url(g.hash) + '" quality="high" type="application/x-shockwave-flash" />')))
        })
    }, function (b) {
        var c = b.preview,
            d = !! b.options.autoplay,
            e = {
                "audio/mpeg": "mp3",
                "audio/mpeg3": "mp3",
                "audio/mp3": "mp3",
                "audio/x-mpeg3": "mp3",
                "audio/x-mp3": "mp3",
                "audio/x-wav": "wav",
                "audio/wav": "wav",
                "audio/x-m4a": "m4a",
                "audio/aac": "m4a",
                "audio/mp4": "m4a",
                "audio/x-mp4": "m4a",
                "audio/ogg": "ogg"
            }, f;
        c.bind("update", function (g) {
            var h = g.file,
                i = e[h.mime];
            b.support.audio[i] && (g.stopImmediatePropagation(), f = a('<audio class="elfinder-quicklook-preview-audio" controls preload="auto" autobuffer><source src="' + b.fm.url(h.hash) + '" /></audio>').appendTo(c), d && f[0].play())
        }).bind("change", function () {
            f && f.parent().length && (f[0].pause(), f.remove(), f = null)
        })
    }, function (b) {
        var c = b.preview,
            d = !! b.options.autoplay,
            e = {
                "video/mp4": "mp4",
                "video/x-m4v": "mp4",
                "video/ogg": "ogg",
                "application/ogg": "ogg",
                "video/webm": "webm"
            }, f;
        c.bind("update", function (g) {
            var h = g.file,
                i = e[h.mime];
            b.support.video[i] && (g.stopImmediatePropagation(), b.hideinfo(), f = a('<video class="elfinder-quicklook-preview-video" controls preload="auto" autobuffer><source src="' + b.fm.url(h.hash) + '" /></video>').appendTo(c), d && f[0].play())
        }).bind("change", function () {
            f && f.parent().length && (f[0].pause(), f.remove(), f = null)
        })
    }, function (b) {
        var c = b.preview,
            d = [],
            e;
        a.each(navigator.plugins, function (b, c) {
            a.each(c, function (a, b) {
                (b.type.indexOf("audio/") === 0 || b.type.indexOf("video/") === 0) && d.push(b.type)
            })
        }), c.bind("update", function (f) {
            var g = f.file,
                h = g.mime,
                i;
            a.inArray(g.mime, d) !== -1 && (f.stopImmediatePropagation(), (i = h.indexOf("video/") === 0) && b.hideinfo(), e = a('<embed src="' + b.fm.url(g.hash) + '" type="' + h + '" class="elfinder-quicklook-preview-' + (i ? "video" : "audio") + '"/>').appendTo(c))
        }).bind("change", function () {
            e && e.parent().length && (e.remove(), e = null)
        })
    }], elFinder.prototype.commands.reload = function () {
        this.alwaysEnabled = !0, this.updateOnSelect = !0, this.shortcuts = [{
            pattern: "ctrl+shift+r f5"
        }], this.getstate = function () {
            return 0
        }, this.exec = function () {
            var a = this.fm,
                b = a.sync(),
                c = setTimeout(function () {
                    a.notify({
                        type: "reload",
                        cnt: 1,
                        hideCnt: !0
                    }), b.always(function () {
                        a.notify({
                            type: "reload",
                            cnt: -1
                        })
                    })
                }, a.notifyDelay);
            return b.always(function () {
                clearTimeout(c), a.trigger("reload")
            })
        }
    }, elFinder.prototype.commands.rename = function () {
        this.shortcuts = [{
            pattern: "f2" + (this.fm.OS == "mac" ? " enter" : "")
        }], this.getstate = function () {
            var a = this.fm.selectedFiles();
            return !this._disabled && a.length == 1 && a[0].phash && !a[0].locked ? 0 : -1
        }, this.exec = function () {
            var b = this.fm,
                c = b.getUI("cwd"),
                d = b.selected(),
                e = d.length,
                f = b.file(d.shift()),
                g = ".elfinder-cwd-filename",
                h = a.Deferred().fail(function (a) {
                    var d = i.parent(),
                        e = b.escape(f.name);
                    d.length ? (i.remove(), d.html(e)) : (c.find("#" + f.hash).find(g).html(e), setTimeout(function () {
                        c.find("#" + f.hash).click()
                    }, 50)), a && b.error(a)
                }).always(function () {
                    b.enable()
                }),
                i = a('<input type="text"/>').keydown(function (b) {
                    b.stopPropagation(), b.stopImmediatePropagation(), b.keyCode == a.ui.keyCode.ESCAPE ? h.reject() : b.keyCode == a.ui.keyCode.ENTER && i.blur()
                }).mousedown(function (a) {
                    a.stopPropagation()
                }).dblclick(function (a) {
                    a.stopPropagation(), a.preventDefault()
                }).blur(function () {
                    var c = a.trim(i.val()),
                        d = i.parent();
                    if (d.length) {
                        i[0].setSelectionRange && i[0].setSelectionRange(0, 0);
                        if (c == f.name) return h.reject();
                        if (!c) return h.reject("errInvName");
                        if (b.fileByName(c, f.phash)) return h.reject(["errExists", c]);
                        d.html(b.escape(c)), b.lockfiles({
                            files: [f.hash]
                        }), b.request({
                            data: {
                                cmd: "rename",
                                target: f.hash,
                                name: c
                            },
                            notify: {
                                type: "rename",
                                cnt: 1
                            }
                        }).fail(function (a) {
                            h.reject(), b.sync()
                        }).done(function (a) {
                            h.resolve(a)
                        }).always(function () {
                            b.unlockfiles({
                                files: [f.hash]
                            })
                        })
                    }
                }),
                j = c.find("#" + f.hash).find(g).empty().append(i.val(f.name)),
                k = i.val().replace(/\.((tar\.(gz|bz|bz2|z|lzo))|cpio\.gz|ps\.gz|xcf\.(gz|bz2)|[a-z0-9]{1,4})$/ig, "");
            return this.disabled() ? h.reject() : !f || e > 1 || !j.length ? h.reject("errCmdParams", this.title) : f.locked ? h.reject(["errLocked", f.name]) : (b.one("select", function () {
                i.parent().length && f && a.inArray(f.hash, b.selected()) === -1 && i.blur()
            }), i.select().focus(), i[0].setSelectionRange && i[0].setSelectionRange(0, k.length), h)
        }
    }, elFinder.prototype.commands.resize = function () {
        this.updateOnSelect = !1, this.getstate = function () {
            var a = this.fm.selectedFiles();
            return !this._disabled && a.length == 1 && a[0].read && a[0].write && a[0].mime.indexOf("image/") !== -1 ? 0 : -1
        }, this.exec = function (b) {
            var c = this.fm,
                d = this.files(b),
                e = a.Deferred(),
                f = function (b, d) {
                    var f = a('<div class="elfinder-dialog-resize"/>'),
                        g = '<input type="text" size="5"/>',
                        h = '<div class="elfinder-resize-row"/>',
                        i = '<div class="elfinder-resize-label"/>',
                        j = a('<div class="elfinder-resize-control"/>'),
                        k = a('<div class="elfinder-resize-preview"/>'),
                        l = a('<div class="elfinder-resize-spinner">' + c.i18n("ntfloadimg") + "</div>"),
                        m = a('<div class="elfinder-resize-handle"/>'),
                        n = a('<div class="elfinder-resize-handle"/>'),
                        o = a('<div class="elfinder-resize-uiresize"/>'),
                        p = a('<div class="elfinder-resize-uicrop"/>'),
                        q = '<div class="ui-widget-content ui-corner-all elfinder-buttonset"/>',
                        r = '<div class="ui-state-default elfinder-button"/>',
                        s = '<span class="ui-widget-content elfinder-toolbar-button-separator"/>',
                        t = a('<div class="elfinder-resize-rotate"/>'),
                        u = a(r).attr("title", c.i18n("rotate-cw")).append(a('<span class="elfinder-button-icon elfinder-button-icon-rotate-l"/>').click(function () {
                            S -= 90, ab.update(S)
                        })),
                        v = a(r).attr("title", c.i18n("rotate-ccw")).append(a('<span class="elfinder-button-icon elfinder-button-icon-rotate-r"/>').click(function () {
                            S += 90, ab.update(S)
                        })),
                        w = a("<span />"),
                        x = a('<div class="ui-state-default ui-corner-all elfinder-resize-reset"><span class="ui-icon ui-icon-arrowreturnthick-1-w"/></div>'),
                        y = a('<div class="elfinder-resize-type"/>').append('<input type="radio" name="type" id="type-resize" value="resize" checked="checked" /><label for="type-resize">' + c.i18n("resize") + "</label>").append('<input type="radio" name="type" id="type-crop"   value="crop"/><label for="type-crop">' + c.i18n("crop") + "</label>").append('<input type="radio" name="type" id="type-rotate" value="rotate"/><label for="type-rotate">' + c.i18n("rotate") + "</label>"),
                        z = a("input", y).change(function () {
                            var b = a("input:checked", y).val();
                            Y(), bb(!0), cb(!0), db(!0), b == "resize" ? (o.show(), t.hide(), p.hide(), bb()) : b == "crop" ? (t.hide(), o.hide(), p.show(), cb()) : b == "rotate" && (o.hide(), p.hide(), t.show(), db())
                        }),
                        A = a('<input type="checkbox" checked="checked"/>').change(function () {
                            N = !! A.prop("checked"), Z.fixHeight(), bb(!0), bb()
                        }),
                        B = a(g).change(function () {
                            var a = parseInt(B.val()),
                                b = parseInt(N ? a / J : C.val());
                            a > 0 && b > 0 && (Z.updateView(a, b), C.val(b))
                        }),
                        C = a(g).change(function () {
                            var a = parseInt(C.val()),
                                b = parseInt(N ? a * J : B.val());
                            b > 0 && a > 0 && (Z.updateView(b, a), B.val(b))
                        }),
                        D = a(g),
                        E = a(g),
                        F = a(g),
                        G = a(g),
                        H = a('<input type="text" size="3" maxlength="3" value="0" />').change(function () {
                            ab.update()
                        }),
                        I = a('<div class="elfinder-resize-rotate-slider"/>').slider({
                            min: 0,
                            max: 359,
                            value: H.val(),
                            animate: !0,
                            change: function (a, b) {
                                b.value != I.slider("value") && ab.update(b.value)
                            },
                            slide: function (a, b) {
                                ab.update(b.value, !1)
                            }
                        }),
                        J = 1,
                        K = 1,
                        L = 0,
                        M = 0,
                        N = !0,
                        O = 0,
                        P = 0,
                        Q = 0,
                        R = 0,
                        S = 0,
                        T = a("<img/>").load(function () {
                            l.remove(), L = T.width(), M = T.height(), J = L / M, Z.updateView(L, M), m.append(T.show()).show(), B.val(L), C.val(M);
                            var b = Math.min(O, P) / Math.sqrt(Math.pow(L, 2) + Math.pow(M, 2));
                            Q = L * b, R = M * b, j.find("input,select").removeAttr("disabled").filter(":text").keydown(function (b) {
                                var c = b.keyCode,
                                    d;
                                b.stopPropagation();
                                if (c >= 37 && c <= 40 || c == a.ui.keyCode.BACKSPACE || c == a.ui.keyCode.DELETE || c == 65 && (b.ctrlKey || b.metaKey) || c == 27) return;
                                c == 9 && (d = a(this).parent()[b.shiftKey ? "prev" : "next"](".elfinder-resize-row").children(":text"), d.length && d.focus());
                                if (c == 13) {
                                    eb();
                                    return
                                }(c < 48 || c > 57) && b.preventDefault()
                            }).filter(":first").focus(), bb(), x.hover(function () {
                                x.toggleClass("ui-state-hover")
                            }).click(Y)
                        }).error(function () {
                            l.text("Unable to load image").css("background", "transparent")
                        }),
                        U = a("<div/>"),
                        V = a("<img/>"),
                        W = a("<div/>"),
                        X = a("<img/>"),
                        Y = function () {
                            B.val(L), C.val(M), Z.updateView(L, M)
                        }, Z = {
                            update: function () {
                                B.val(parseInt(T.width() / K)), C.val(parseInt(T.height() / K))
                            },
                            updateView: function (a, b) {
                                a > O || b > P ? a / O > b / P ? T.width(O).height(Math.ceil(T.width() / J)) : T.height(P).width(Math.ceil(T.height() * J)) : T.width(a).height(b), K = T.width() / a, w.text("1 : " + (1 / K).toFixed(2)), Z.updateHandle()
                            },
                            updateHandle: function () {
                                m.width(T.width()).height(T.height())
                            },
                            fixWidth: function () {
                                var a, b;
                                N && (b = C.val(), b = parseInt(b * J), Z.updateView(a, b), B.val(a))
                            },
                            fixHeight: function () {
                                var a, b;
                                N && (a = B.val(), b = parseInt(a / J), Z.updateView(a, b), C.val(b))
                            }
                        }, _ = {
                            update: function () {
                                F.val(parseInt(n.width() / K)), G.val(parseInt(n.height() / K)), D.val(parseInt((n.offset().left - V.offset().left) / K)), E.val(parseInt((n.offset().top - V.offset().top) / K))
                            },
                            resize_update: function () {
                                _.update(), W.width(n.width()), W.height(n.height())
                            }
                        }, ab = {
                            mouseStartAngle: 0,
                            imageStartAngle: 0,
                            imageBeingRotated: !1,
                            update: function (b, c) {
                                typeof b == "undefined" && (S = b = parseInt(H.val())), typeof c == "undefined" && (c = !0), !c || a.browser.opera || a.browser.msie && parseInt(a.browser.version) < 9 ? X.rotate(b) : X.animate({
                                    rotate: b + "deg"
                                }), b %= 360, b < 0 && (b += 360), H.val(parseInt(b)), I.slider("value", H.val())
                            },
                            execute: function (a) {
                                if (!ab.imageBeingRotated) return;
                                var b = ab.getCenter(X),
                                    c = a.pageX - b[0],
                                    d = a.pageY - b[1],
                                    e = Math.atan2(d, c),
                                    f = e - ab.mouseStartAngle + ab.imageStartAngle;
                                return f = Math.round(parseFloat(f) * 180 / Math.PI), a.shiftKey && (f = Math.round((f + 6) / 15) * 15), X.rotate(f), f %= 360, f < 0 && (f += 360), H.val(f), I.slider("value", H.val()), !1
                            },
                            start: function (b) {
                                ab.imageBeingRotated = !0;
                                var c = ab.getCenter(X),
                                    d = b.pageX - c[0],
                                    e = b.pageY - c[1];
                                return ab.mouseStartAngle = Math.atan2(e, d), ab.imageStartAngle = parseFloat(X.rotate()) * Math.PI / 180, a(document).mousemove(ab.execute), !1
                            },
                            stop: function (b) {
                                if (!ab.imageBeingRotated) return;
                                return a(document).unbind("mousemove", ab.execute), setTimeout(function () {
                                    ab.imageBeingRotated = !1
                                }, 10), !1
                            },
                            getCenter: function (a) {
                                var b = X.rotate();
                                X.rotate(0);
                                var c = X.offset(),
                                    d = c.left + X.width() / 2,
                                    e = c.top + X.height() / 2;
                                return X.rotate(b), Array(d, e)
                            }
                        }, bb = function (b) {
                            a.fn.resizable && (b ? (m.resizable("destroy"), m.hide()) : (m.show(), m.resizable({
                                alsoResize: T,
                                aspectRatio: N,
                                resize: Z.update,
                                stop: Z.fixHeight
                            })))
                        }, cb = function (b) {
                            a.fn.draggable && a.fn.resizable && (b ? (n.resizable("destroy"), n.draggable("destroy"), U.hide()) : (V.width(T.width()).height(T.height()), W.width(T.width()).height(T.height()), n.width(V.width()).height(V.height()).offset(V.offset()).resizable({
                                containment: U,
                                resize: _.resize_update,
                                handles: "all"
                            }).draggable({
                                handle: n,
                                containment: V,
                                drag: _.update
                            }), U.show().width(T.width()).height(T.height()), _.update()))
                        }, db = function (b) {
                            a.fn.draggable && a.fn.resizable && (b ? X.hide() : X.show().width(Q).height(R).css("margin-top", (P - R) / 2 + "px").css("margin-left", (O - Q) / 2 + "px"))
                        }, eb = function () {
                            var d, g, h, i, j, k = a("input:checked", y).val();
                            B.add(C).change();
                            if (k == "resize") d = parseInt(B.val()) || 0, g = parseInt(C.val()) || 0;
                            else if (k == "crop") d = parseInt(F.val()) || 0, g = parseInt(G.val()) || 0, h = parseInt(D.val()) || 0, i = parseInt(E.val()) || 0;
                            else if (k = "rotate") {
                                d = L, g = M, j = parseInt(H.val()) || 0;
                                if (j < 0 || j > 360) return c.error("Invalid rotate degree");
                                if (j == 0 || j == 360) return c.error("Image dose not rotated")
                            }
                            if (k != "rotate") {
                                if (d <= 0 || g <= 0) return c.error("Invalid image size");
                                if (d == L && g == M) return c.error("Image size not changed")
                            }
                            f.elfinderdialog("close"), c.request({
                                data: {
                                    cmd: "resize",
                                    target: b.hash,
                                    width: d,
                                    height: g,
                                    x: h,
                                    y: i,
                                    degree: j,
                                    mode: k
                                },
                                notify: {
                                    type: "resize",
                                    cnt: 1
                                }
                            }).fail(function (a) {
                                e.reject(a)
                            }).done(function () {
                                e.resolve()
                            })
                        }, fb = {}, gb = "elfinder-resize-handle-hline",
                        hb = "elfinder-resize-handle-vline",
                        ib = "elfinder-resize-handle-point",
                        jb = c.url(b.hash);
                    X.mousedown(ab.start), a(document).mouseup(ab.stop), o.append(a(h).append(a(i).text(c.i18n("width"))).append(B).append(x)).append(a(h).append(a(i).text(c.i18n("height"))).append(C)).append(a(h).append(a("<label/>").text(c.i18n("aspectRatio")).prepend(A))).append(a(h).append(c.i18n("scale") + " ").append(w)), p.append(a(h).append(a(i).text("X")).append(D)).append(a(h).append(a(i).text("Y")).append(E)).append(a(h).append(a(i).text(c.i18n("width"))).append(F)).append(a(h).append(a(i).text(c.i18n("height"))).append(G)), t.append(a(h).append(a(i).text(c.i18n("rotate"))).append(a('<div style="float:left; width: 130px;">').append(a('<div style="float:left;">').append(H).append(a("<span/>").text(c.i18n("degree")))).append(a(q).append(u).append(a(s)).append(v))).append(I)), f.append(y), j.append(a(h)).append(o).append(p.hide()).append(t.hide()).find("input,select").attr("disabled", "disabled"), m.append('<div class="' + gb + " " + gb + '-top"/>').append('<div class="' + gb + " " + gb + '-bottom"/>').append('<div class="' + hb + " " + hb + '-left"/>').append('<div class="' + hb + " " + hb + '-right"/>').append('<div class="' + ib + " " + ib + '-e"/>').append('<div class="' + ib + " " + ib + '-se"/>').append('<div class="' + ib + " " + ib + '-s"/>'), k.append(l).append(m.hide()).append(T.hide()), n.css("position", "absolute").append('<div class="' + gb + " " + gb + '-top"/>').append('<div class="' + gb + " " + gb + '-bottom"/>').append('<div class="' + hb + " " + hb + '-left"/>').append('<div class="' + hb + " " + hb + '-right"/>').append('<div class="' + ib + " " + ib + '-n"/>').append('<div class="' + ib + " " + ib + '-e"/>').append('<div class="' + ib + " " + ib + '-s"/>').append('<div class="' + ib + " " + ib + '-w"/>').append('<div class="' + ib + " " + ib + '-ne"/>').append('<div class="' + ib + " " + ib + '-se"/>').append('<div class="' + ib + " " + ib + '-sw"/>').append('<div class="' + ib + " " + ib + '-nw"/>'), k.append(U.css("position", "absolute").hide().append(V).append(n.append(W))), k.append(X.hide()), k.css("overflow", "hidden"), f.append(k).append(j), fb[c.i18n("btnCancel")] = function () {
                        f.elfinderdialog("close")
                    }, fb[c.i18n("btnApply")] = eb, c.dialog(f, {
                        title: b.name,
                        width: 650,
                        resizable: !1,
                        destroyOnClose: !0,
                        buttons: fb,
                        open: function () {
                            k.zIndex(1 + a(this).parent().zIndex())
                        }
                    }).attr("id", d), a.browser.msie && parseInt(a.browser.version) < 9 && a(".elfinder-dialog").css("filter", ""), x.css("left", B.position().left + B.width() + 12), W.css({
                        opacity: .2,
                        "background-color": "#fff",
                        position: "absolute"
                    }), n.css("cursor", "move"), n.find(".elfinder-resize-handle-point").css({
                        "background-color": "#fff",
                        opacity: .5,
                        "border-color": "#000"
                    }), X.css("cursor", "pointer"), y.buttonset(), O = k.width() - (m.outerWidth() - m.width()), P = k.height() - (m.outerHeight() - m.height()), T.attr("src", jb + (jb.indexOf("?") === -1 ? "?" : "&") + "_=" + Math.random()), V.attr("src", T.attr("src")), X.attr("src", T.attr("src"))
                }, g, h;
            return !d.length || d[0].mime.indexOf("image/") === -1 ? e.reject() : (g = "resize-" + c.namespace + "-" + d[0].hash, h = c.getUI().find("#" + g), h.length ? (h.elfinderdialog("toTop"), e.resolve()) : (f(d[0], g), e))
        }
    },
    function (a) {
        var b = function (a, b) {
            var c = 0;
            for (c in b) if (typeof a[b[c]] != "undefined") return b[c];
            return a[b[c]] = "", b[c]
        };
        a.cssHooks.rotate = {
            get: function (b, c, d) {
                return a(b).rotate()
            },
            set: function (b, c) {
                return a(b).rotate(c), c
            }
        }, a.cssHooks.transform = {
            get: function (a, c, d) {
                var e = b(a.style, ["WebkitTransform", "MozTransform", "OTransform", "msTransform", "transform"]);
                return a.style[e]
            },
            set: function (a, c) {
                var d = b(a.style, ["WebkitTransform", "MozTransform", "OTransform", "msTransform", "transform"]);
                return a.style[d] = c, c
            }
        }, a.fn.rotate = function (b) {
            if (typeof b == "undefined") {
                if (a.browser.opera) {
                    var c = this.css("transform").match(/rotate\((.*?)\)/);
                    return c && c[1] ? Math.round(parseFloat(c[1]) * 180 / Math.PI) : 0
                }
                var c = this.css("transform").match(/rotate\((.*?)\)/);
                return c && c[1] ? parseInt(c[1]) : 0
            }
            return this.css("transform", this.css("transform").replace(/none|rotate\(.*?\)/, "") + "rotate(" + parseInt(b) + "deg)"), this
        }, a.fx.step.rotate = function (b) {
            b.state == 0 && (b.start = a(b.elem).rotate(), b.now = b.start), a(b.elem).rotate(b.now)
        };
        if (a.browser.msie && parseInt(a.browser.version) < 9) {
            var c = function (a) {
                var b = a,
                    c = b.offsetLeft,
                    d = b.offsetTop;
                while (b.offsetParent) {
                    b = b.offsetParent;
                    if (b != document.body && b.currentStyle["position"] != "static") break;
                    b != document.body && b != document.documentElement && (c -= b.scrollLeft, d -= b.scrollTop), c += b.offsetLeft, d += b.offsetTop
                }
                return {
                    x: c,
                    y: d
                }
            }, d = function (a) {
                if (a.currentStyle["position"] != "static") return;
                var b = c(a);
                a.style.position = "absolute", a.style.left = b.x + "px", a.style.top = b.y + "px"
            }, e = function (a, b) {
                var c, e = 1,
                    f = 1,
                    g = 1,
                    h = 1;
                if (typeof a.style["msTransform"] != "undefined") return !0;
                d(a), c = b.match(/rotate\((.*?)\)/);
                var i = c && c[1] ? parseInt(c[1]) : 0;
                i %= 360, i < 0 && (i = 360 + i);
                var j = i * Math.PI / 180,
                    k = Math.cos(j),
                    l = Math.sin(j);
                e *= k, f *= -l, g *= l, h *= k, a.style.filter = (a.style.filter || "").replace(/progid:DXImageTransform\.Microsoft\.Matrix\([^)]*\)/, "") + ("progid:DXImageTransform.Microsoft.Matrix(M11=" + e + ",M12=" + f + ",M21=" + g + ",M22=" + h + ",FilterType='bilinear',sizingMethod='auto expand')");
                var m = parseInt(a.style.width || a.width || 0),
                    n = parseInt(a.style.height || a.height || 0),
                    j = i * Math.PI / 180,
                    o = Math.abs(Math.cos(j)),
                    p = Math.abs(Math.sin(j)),
                    q = (m - (m * o + n * p)) / 2,
                    r = (n - (m * p + n * o)) / 2;
                return a.style.marginLeft = Math.floor(q) + "px", a.style.marginTop = Math.floor(r) + "px", !0
            }, f = a.cssHooks.transform.set;
            a.cssHooks.transform.set = function (a, b) {
                return f.apply(this, [a, b]), e(a, b), b
            }
        }
    }(jQuery), elFinder.prototype.commands.rm = function () {
        this.shortcuts = [{
            pattern: "delete ctrl+backspace"
        }], this.getstate = function (b) {
            var c = this.fm;
            return b = b || c.selected(), !this._disabled && b.length && a.map(b, function (a) {
                var b = c.file(a);
                return b && b.phash && !b.locked ? a : null
            }).length == b.length ? 0 : -1
        }, this.exec = function (b) {
            var c = this,
                d = this.fm,
                e = a.Deferred().fail(function (a) {
                    a && d.error(a)
                }),
                f = this.files(b),
                g = f.length,
                h = d.cwd().hash,
                i = !1;
            return !g || this._disabled ? e.reject() : (a.each(f, function (a, b) {
                if (!b.phash) return !e.reject(["errRm", b.name, "errPerm"]);
                if (b.locked) return !e.reject(["errLocked", b.name]);
                b.hash == h && (i = d.root(b.hash))
            }), e.isRejected() || (f = this.hashes(b), d.confirm({
                title: c.title,
                text: "confirmRm",
                accept: {
                    label: "btnRm",
                    callback: function () {
                        d.lockfiles({
                            files: f
                        }), d.request({
                            data: {
                                cmd: "rm",
                                targets: f
                            },
                            notify: {
                                type: "rm",
                                cnt: g
                            },
                            preventFail: !0
                        }).fail(function (a) {
                            e.reject(a)
                        }).done(function (a) {
                            e.done(a), i && d.exec("open", i)
                        }).always(function () {
                            d.unlockfiles({
                                files: f
                            })
                        })
                    }
                },
                cancel: {
                    label: "btnCancel",
                    callback: function () {
                        e.reject()
                    }
                }
            })), e)
        }
    }, elFinder.prototype.commands.search = function () {
        this.title = "Find files", this.options = {
            ui: "searchbutton"
        }, this.alwaysEnabled = !0, this.updateOnSelect = !1, this.getstate = function () {
            return 0
        }, this.exec = function (b) {
            var c = this.fm;
            return typeof b == "string" && b ? (c.trigger("searchstart", {
                query: b
            }), c.request({
                data: {
                    cmd: "search",
                    q: b
                },
                notify: {
                    type: "search",
                    cnt: 1,
                    hideCnt: !0
                }
            })) : (c.getUI("toolbar").find("." + c.res("class", "searchbtn") + " :text").focus(), a.Deferred().reject())
        }
    }, elFinder.prototype.commands.sort = function () {
        var b = this,
            c = ["nameDirsFirst", "kindDirsFirst", "sizeDirsFirst", "dateDirsFirst", "name", "kind", "size", "date"],
            d;
        this.options = {
            ui: "sortbutton"
        }, this.value = 1, this.variants = [];
        for (d = 0; d < c.length; d++) this.variants.push([c[d], this.fm.i18n("sort" + c[d])]);
        this.disableOnSearch = !0, this.fm.bind("load sortchange", function () {
            b.value = c[b.fm.sort - 1], b.change()
        }), this.getstate = function () {
            return 0
        }, this.exec = function (b, d) {
            var e = a.inArray(d, c) + 1 == this.fm.sort ? this.fm.sortDirect == "asc" ? "desc" : "asc" : this.fm.sortDirect;
            this.fm.setSort(d, e)
        }
    }, elFinder.prototype.commands.up = function () {
        this.alwaysEnabled = !0, this.updateOnSelect = !1, this.shortcuts = [{
            pattern: "ctrl+up"
        }], this.getstate = function () {
            return this.fm.cwd().phash ? 0 : -1
        }, this.exec = function () {
            return this.fm.cwd().phash ? this.fm.exec("open", this.fm.cwd().phash) : a.Deferred().reject()
        }
    }, elFinder.prototype.commands.upload = function () {
        var b = this.fm.res("class", "hover");
        this.disableOnSearch = !0, this.updateOnSelect = !1, this.shortcuts = [{
            pattern: "ctrl+u"
        }], this.getstate = function () {
            return !this._disabled && this.fm.cwd().write ? 0 : -1
        }, this.exec = function (c) {
            var d = this.fm,
                e = function (a) {
                    g.elfinderdialog("close"), d.upload(a).fail(function (a) {
                        f.reject(a)
                    }).done(function (a) {
                        f.resolve(a)
                    })
                }, f, g, h, i, j;
            return this.disabled() ? a.Deferred().reject() : c && (c.input || c.files) ? d.upload(c) : (f = a.Deferred(), h = a('<input type="file" multiple="true"/>').change(function () {
                e({
                    input: h[0]
                })
            }), i = a('<div class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"><span class="ui-button-text">' + d.i18n("selectForUpload") + "</span></div>").append(a("<form/>").append(h)).hover(function () {
                i.toggleClass(b)
            }), g = a('<div class="elfinder-upload-dialog-wrapper"/>').append(i), d.dragUpload && (j = a('<div class="ui-corner-all elfinder-upload-dropbox">' + d.i18n("dropFiles") + "</div>").prependTo(g).after('<div class="elfinder-upload-dialog-or">' + d.i18n("or") + "</div>")[0], j.addEventListener("dragenter", function (c) {
                c.stopPropagation(), c.preventDefault(), a(j).addClass(b)
            }, !1), j.addEventListener("dragleave", function (c) {
                c.stopPropagation(), c.preventDefault(), a(j).removeClass(b)
            }, !1), j.addEventListener("dragover", function (a) {
                a.stopPropagation(), a.preventDefault()
            }, !1), j.addEventListener("drop", function (a) {
                a.stopPropagation(), a.preventDefault(), e({
                    files: a.dataTransfer.files
                })
            }, !1)), d.dialog(g, {
                title: this.title,
                modal: !0,
                resizable: !1,
                destroyOnClose: !0
            }), f)
        }
    }, elFinder.prototype.commands.view = function () {
        this.value = this.fm.storage("view"), this.alwaysEnabled = !0, this.updateOnSelect = !1, this.options = {
            ui: "viewbutton"
        }, this.getstate = function () {
            return 0
        }, this.exec = function () {
            var a = this.fm.storage("view", this.value == "list" ? "icons" : "list");
            this.fm.viewchange(), this.update(void 0, a)
        }
    }
})(jQuery)
