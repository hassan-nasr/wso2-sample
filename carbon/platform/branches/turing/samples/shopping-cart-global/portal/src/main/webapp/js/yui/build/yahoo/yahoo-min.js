/*
 Copyright (c) 2009, Yahoo! Inc. All rights reserved.
 Code licensed under the BSD License:
 http://developer.yahoo.net/yui/license.txt
 version: 2.8.0r4
 */
if (typeof YAHOO == "undefined" || !YAHOO) {
    var YAHOO = {};
}
YAHOO.namespace = function() {
    var A = arguments,E = null,C,B,D;
    for (C = 0; C < A.length; C = C + 1) {
        D = ("" + A[C]).split(".");
        E = YAHOO;
        for (B = (D[0] == "YAHOO") ? 1 : 0; B < D.length; B = B + 1) {
            E[D[B]] = E[D[B]] || {};
            E = E[D[B]];
        }
    }
    return E;
};
YAHOO.log = function(D, A, C) {
    var B = YAHOO.widget.Logger;
    if (B && B.log) {
        return B.log(D, A, C);
    } else {
        return false;
    }
};
YAHOO.register = function(A, E, D) {
    var I = YAHOO.env.modules,B,H,G,F,C;
    if (!I[A]) {
        I[A] = {versions:[],builds:[]};
    }
    B = I[A];
    H = D.version;
    G = D.build;
    F = YAHOO.env.listeners;
    B.name = A;
    B.version = H;
    B.build = G;
    B.versions.push(H);
    B.builds.push(G);
    B.mainClass = E;
    for (C = 0; C < F.length; C = C + 1) {
        F[C](B);
    }
    if (E) {
        E.VERSION = H;
        E.BUILD = G;
    } else {
        YAHOO.log("mainClass is undefined for module " + A, "warn");
    }
};
YAHOO.env = YAHOO.env || {modules:[],listeners:[]};
YAHOO.env.getVersion = function(A) {
    return YAHOO.env.modules[A] || null;
};
YAHOO.env.ua = function() {
    var D = function(H) {
        var I = 0;
        return parseFloat(H.replace(/\./g, function() {
            return(I++ == 1) ? "" : ".";
        }));
    },G = navigator,F = {ie:0,opera:0,gecko:0,webkit:0,mobile:null,air:0,caja:G.cajaVersion,secure:false,os:null},C = navigator && navigator.userAgent,E = window && window.location,B = E && E.href,A;
    F.secure = B && (B.toLowerCase().indexOf("https") === 0);
    if (C) {
        if ((/windows|win32/i).test(C)) {
            F.os = "windows";
        } else {
            if ((/macintosh/i).test(C)) {
                F.os = "macintosh";
            }
        }
        if ((/KHTML/).test(C)) {
            F.webkit = 1;
        }
        A = C.match(/AppleWebKit\/([^\s]*)/);
        if (A && A[1]) {
            F.webkit = D(A[1]);
            if (/ Mobile\//.test(C)) {
                F.mobile = "Apple";
            } else {
                A = C.match(/NokiaN[^\/]*/);
                if (A) {
                    F.mobile = A[0];
                }
            }
            A = C.match(/AdobeAIR\/([^\s]*)/);
            if (A) {
                F.air = A[0];
            }
        }
        if (!F.webkit) {
            A = C.match(/Opera[\s\/]([^\s]*)/);
            if (A && A[1]) {
                F.opera = D(A[1]);
                A = C.match(/Opera Mini[^;]*/);
                if (A) {
                    F.mobile = A[0];
                }
            } else {
                A = C.match(/MSIE\s([^;]*)/);
                if (A && A[1]) {
                    F.ie = D(A[1]);
                } else {
                    A = C.match(/Gecko\/([^\s]*)/);
                    if (A) {
                        F.gecko = 1;
                        A = C.match(/rv:([^\s\)]*)/);
                        if (A && A[1]) {
                            F.gecko = D(A[1]);
                        }
                    }
                }
            }
        }
    }
    return F;
}();
(function() {
    YAHOO.namespace("util", "widget", "example");
    if ("undefined" !== typeof YAHOO_config) {
        var B = YAHOO_config.listener,A = YAHOO.env.listeners,D = true,C;
        if (B) {
            for (C = 0; C < A.length; C++) {
                if (A[C] == B) {
                    D = false;
                    break;
                }
            }
            if (D) {
                A.push(B);
            }
        }
    }
})();
YAHOO.lang = YAHOO.lang || {};
(function() {
    var B = YAHOO.lang,A = Object.prototype,H = "[object Array]",C = "[object Function]",G = "[object Object]",E = [],F = ["toString","valueOf"],D = {isArray:function(
            I) {
        return A.toString.apply(I) === H;
    },isBoolean:function(I) {
        return typeof I === "boolean";
    },isFunction:function(I) {
        return(typeof I === "function") || A.toString.apply(I) === C;
    },isNull:function(I) {
        return I === null;
    },isNumber:function(I) {
        return typeof I === "number" && isFinite(I);
    },isObject:function(I) {
        return(I && (typeof I === "object" || B.isFunction(I))) || false;
    },isString:function(I) {
        return typeof I === "string";
    },isUndefined:function(I) {
        return typeof I === "undefined";
    },_IEEnumFix:(YAHOO.env.ua.ie) ? function(K, J) {
        var I,M,L;
        for (I = 0; I < F.length; I = I + 1) {
            M = F[I];
            L = J[M];
            if (B.isFunction(L) && L != A[M]) {
                K[M] = L;
            }
        }
    } : function() {
    },extend:function(L, M, K) {
        if (!M || !L) {
            throw new Error("extend failed, please check that " + "all dependencies are included.");
        }
        var J = function() {
        },I;
        J.prototype = M.prototype;
        L.prototype = new J();
        L.prototype.constructor = L;
        L.superclass = M.prototype;
        if (M.prototype.constructor == A.constructor) {
            M.prototype.constructor = M;
        }
        if (K) {
            for (I in K) {
                if (B.hasOwnProperty(K, I)) {
                    L.prototype[I] = K[I];
                }
            }
            B._IEEnumFix(L.prototype, K);
        }
    },augmentObject:function(M, L) {
        if (!L || !M) {
            throw new Error("Absorb failed, verify dependencies.");
        }
        var I = arguments,K,N,J = I[2];
        if (J && J !== true) {
            for (K = 2; K < I.length; K = K + 1) {
                M[I[K]] = L[I[K]];
            }
        } else {
            for (N in L) {
                if (J || !(N in M)) {
                    M[N] = L[N];
                }
            }
            B._IEEnumFix(M, L);
        }
    },augmentProto:function(L, K) {
        if (!K || !L) {
            throw new Error("Augment failed, verify dependencies.");
        }
        var I = [L.prototype,K.prototype],J;
        for (J = 2; J < arguments.length; J = J + 1) {
            I.push(arguments[J]);
        }
        B.augmentObject.apply(this, I);
    },dump:function(I, N) {
        var K,M,P = [],Q = "{...}",J = "f(){...}",O = ", ",L = " => ";
        if (!B.isObject(I)) {
            return I + "";
        } else {
            if (I instanceof Date || ("nodeType" in I && "tagName" in I)) {
                return I;
            } else {
                if (B.isFunction(I)) {
                    return J;
                }
            }
        }
        N = (B.isNumber(N)) ? N : 3;
        if (B.isArray(I)) {
            P.push("[");
            for (K = 0,M = I.length; K < M; K = K + 1) {
                if (B.isObject(I[K])) {
                    P.push((N > 0) ? B.dump(I[K], N - 1) : Q);
                } else {
                    P.push(I[K]);
                }
                P.push(O);
            }
            if (P.length > 1) {
                P.pop();
            }
            P.push("]");
        } else {
            P.push("{");
            for (K in I) {
                if (B.hasOwnProperty(I, K)) {
                    P.push(K + L);
                    if (B.isObject(I[K])) {
                        P.push((N > 0) ? B.dump(I[K], N - 1) : Q);
                    } else {
                        P.push(I[K]);
                    }
                    P.push(O);
                }
            }
            if (P.length > 1) {
                P.pop();
            }
            P.push("}");
        }
        return P.join("");
    },substitute:function(Y, J, R) {
        var N,M,L,U,V,X,T = [],K,O = "dump",S = " ",I = "{",W = "}",Q,P;
        for (; ;) {
            N = Y.lastIndexOf(I);
            if (N < 0) {
                break;
            }
            M = Y.indexOf(W, N);
            if (N + 1 >= M) {
                break;
            }
            K = Y.substring(N + 1, M);
            U = K;
            X = null;
            L = U.indexOf(S);
            if (L > -1) {
                X = U.substring(L + 1);
                U = U.substring(0, L);
            }
            V = J[U];
            if (R) {
                V = R(U, V, X);
            }
            if (B.isObject(V)) {
                if (B.isArray(V)) {
                    V = B.dump(V, parseInt(X, 10));
                } else {
                    X = X || "";
                    Q = X.indexOf(O);
                    if (Q > -1) {
                        X = X.substring(4);
                    }
                    P = V.toString();
                    if (P === G || Q > -1) {
                        V = B.dump(V, parseInt(X, 10));
                    } else {
                        V = P;
                    }
                }
            } else {
                if (!B.isString(V) && !B.isNumber(V)) {
                    V = "~-" + T.length + "-~";
                    T[T.length] = K;
                }
            }
            Y = Y.substring(0, N) + V + Y.substring(M + 1);
        }
        for (N = T.length - 1; N >= 0; N = N - 1) {
            Y = Y.replace(new RegExp("~-" + N + "-~"), "{" + T[N] + "}", "g");
        }
        return Y;
    },trim:function(I) {
        try {
            return I.replace(/^\s+|\s+$/g, "");
        } catch(J) {
            return I;
        }
    },merge:function() {
        var L = {},J = arguments,I = J.length,K;
        for (K = 0; K < I; K = K + 1) {
            B.augmentObject(L, J[K], true);
        }
        return L;
    },later:function(P, J, Q, L, M) {
        P = P || 0;
        J = J || {};
        var K = Q,O = L,N,I;
        if (B.isString(Q)) {
            K = J[Q];
        }
        if (!K) {
            throw new TypeError("method undefined");
        }
        if (O && !B.isArray(O)) {
            O = [L];
        }
        N = function() {
            K.apply(J, O || E);
        };
        I = (M) ? setInterval(N, P) : setTimeout(N, P);
        return{interval:M,cancel:function() {
            if (this.interval) {
                clearInterval(I);
            } else {
                clearTimeout(I);
            }
        }};
    },isValue:function(I) {
        return(B.isObject(I) || B.isString(I) || B.isNumber(I) || B.isBoolean(I));
    }};
    B.hasOwnProperty = (A.hasOwnProperty) ? function(I, J) {
        return I && I.hasOwnProperty(J);
    } : function(I, J) {
        return !B.isUndefined(I[J]) && I.constructor.prototype[J] !== I[J];
    };
    D.augmentObject(B, D, true);
    YAHOO.util.Lang = B;
    B.augment = B.augmentProto;
    YAHOO.augment = B.augmentProto;
    YAHOO.extend = B.extend;
})();
YAHOO.register("yahoo", YAHOO, {version:"2.8.0r4",build:"2449"});
