/**
 * vue-router v3.0.1
 * (c) 2017 Evan You
 * @license MIT
 */
(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
        typeof define === 'function' && define.amd ? define(factory) :
            (global.VueRouter = factory());
}(this, (function () { 'use strict';

    /*  */

    function assert (condition, message) {
        if (!condition) {
            throw new Error(("[vue-router] " + message))
        }
    }

    function warn (condition, message) {
        if ("development" !== 'production' && !condition) {
            typeof console !== 'undefined' && console.warn(("[vue-router] " + message));
        }
    }

    function isError (err) {
        return Object.prototype.toString.call(err).indexOf('Error') > -1
    }

    var View = {
        name: 'router-view',
        functional: true,
        props: {
            name: {
                type: String,
                default: 'default'
            }
        },
        render: function render (_, ref) {
            var props = ref.props;
            var children = ref.children;
            var parent = ref.parent;
            var data = ref.data;

            data.routerView = true;

            // directly use parent context's createElement() function
            // so that components rendered by router-view can resolve named slots
            var h = parent.$createElement;
            var name = props.name;
            var route = parent.$route;
            var cache = parent._routerViewCache || (parent._routerViewCache = {});

            // determine current view depth, also check to see if the tree
            // has been toggled inactive but kept-alive.
            var depth = 0;
            var inactive = false;
            while (parent && parent._routerRoot !== parent) {
                if (parent.$vnode && parent.$vnode.data.routerView) {
                    depth++;
                }
                if (parent._inactive) {
                    inactive = true;
                }
                parent = parent.$parent;
            }
            data.routerViewDepth = depth;

            // render previous view if the tree is inactive and kept-alive
            if (inactive) {
                return h(cache[name], data, children)
            }

            var matched = route.matched[depth];
            // render empty node if no matched route
            if (!matched) {
                cache[name] = null;
                return h()
            }

            var component = cache[name] = matched.components[name];

            // attach instance registration hook
            // this will be called in the instance's injected lifecycle hooks
            data.registerRouteInstance = function (vm, val) {
                // val could be undefined for unregistration
                var current = matched.instances[name];
                if (
                    (val && current !== vm) ||
                    (!val && current === vm)
                ) {
                    matched.instances[name] = val;
                }
            }

            // also register instance in prepatch hook
            // in case the same component instance is reused across different routes
            ;(data.hook || (data.hook = {})).prepatch = function (_, vnode) {
                matched.instances[name] = vnode.componentInstance;
            };

            // resolve props
            var propsToPass = data.props = resolveProps(route, matched.props && matched.props[name]);
            if (propsToPass) {
                // clone to prevent mutation
                propsToPass = data.props = extend({}, propsToPass);
                // pass non-declared props as attrs
                var attrs = data.attrs = data.attrs || {};
                for (var key in propsToPass) {
                    if (!component.props || !(key in component.props)) {
                        attrs[key] = propsToPass[key];
                        delete propsToPass[key];
                    }
                }
            }

            return h(component, data, children)
        }
    };

    function resolveProps (route, config) {
        switch (typeof config) {
            case 'undefined':
                return
            case 'object':
                return config
            case 'function':
                return config(route)
            case 'boolean':
                return config ? route.params : undefined
            default:
            {
                warn(
                    false,
                    "props in \"" + (route.path) + "\" is a " + (typeof config) + ", " +
                    "expecting an object, function or boolean."
                );
            }
        }
    }

    function extend (to, from) {
        for (var key in from) {
            to[key] = from[key];
        }
        return to
    }

    /*  */

    var encodeReserveRE = /[!'()*]/g;
    var encodeReserveReplacer = function (c) { return '%' + c.charCodeAt(0).toString(16); };
    var commaRE = /%2C/g;

// fixed encodeURIComponent which is more conformant to RFC3986:
// - escapes [!'()*]
// - preserve commas
    var encode = function (str) { return encodeURIComponent(str)
        .replace(encodeReserveRE, encodeReserveReplacer)
        .replace(commaRE, ','); };

    var decode = decodeURIComponent;

    function resolveQuery (
        query,
        extraQuery,
        _parseQuery
    ) {
        if ( extraQuery === void 0 ) extraQuery = {};

        var parse = _parseQuery || parseQuery;
        var parsedQuery;
        try {
            parsedQuery = parse(query || '');
        } catch (e) {
            "development" !== 'production' && warn(false, e.message);
            parsedQuery = {};
        }
        for (var key in extraQuery) {
            parsedQuery[key] = extraQuery[key];
        }
        return parsedQuery
    }

    function parseQuery (query) {
        var res = {};

        query = query.trim().replace(/^(\?|#|&)/, '');

        if (!query) {
            return res
        }

        query.split('&').forEach(function (param) {
            var parts = param.replace(/\+/g, ' ').split('=');
            var key = decode(parts.shift());
            var val = parts.length > 0
                ? decode(parts.join('='))
                : null;

            if (res[key] === undefined) {
                res[key] = val;
            } else if (Array.isArray(res[key])) {
                res[key].push(val);
            } else {
                res[key] = [res[key], val];
            }
        });

        return res
    }

    function stringifyQuery (obj) {
        var res = obj ? Object.keys(obj).map(function (key) {
            var val = obj[key];

            if (val === undefined) {
                return ''
            }

            if (val === null) {
                return encode(key)
            }

            if (Array.isArray(val)) {
                var result = [];
                val.forEach(function (val2) {
                    if (val2 === undefined) {
                        return
                    }
                    if (val2 === null) {
                        result.push(encode(key));
                    } else {
                        result.push(encode(key) + '=' + encode(val2));
                    }
                });
                return result.join('&')
            }

            return encode(key) + '=' + encode(val)
        }).filter(function (x) { return x.length > 0; }).join('&') : null;
        return res ? ("?" + res) : ''
    }

    /*  */


    var trailingSlashRE = /\/?$/;

    function createRoute (
        record,
        location,
        redirectedFrom,
        router
    ) {
        var stringifyQuery$$1 = router && router.options.stringifyQuery;

        var query = location.query || {};
        try {
            query = clone(query);
        } catch (e) {}

        var route = {
            name: location.name || (record && record.name),
            meta: (record && record.meta) || {},
            path: location.path || '/',
            hash: location.hash || '',
            query: query,
            params: location.params || {},
            fullPath: getFullPath(location, stringifyQuery$$1),
            matched: record ? formatMatch(record) : []
        };
        if (redirectedFrom) {
            route.redirectedFrom = getFullPath(redirectedFrom, stringifyQuery$$1);
        }
        return Object.freeze(route)
    }

    function clone (value) {
        if (Array.isArray(value)) {
            return value.map(clone)
        } else if (value && typeof value === 'object') {
            var res = {};
            for (var key in value) {
                res[key] = clone(value[key]);
            }
            return res
        } else {
            return value
        }
    }

// the starting route that represents the initial state
    var START = createRoute(null, {
        path: '/'
    });

    function formatMatch (record) {
        var res = [];
        while (record) {
            res.unshift(record);
            record = record.parent;
        }
        return res
    }

    function getFullPath (
        ref,
        _stringifyQuery
    ) {
        var path = ref.path;
        var query = ref.query; if ( query === void 0 ) query = {};
        var hash = ref.hash; if ( hash === void 0 ) hash = '';

        var stringify = _stringifyQuery || stringifyQuery;
        return (path || '/') + stringify(query) + hash
    }

    function isSameRoute (a, b) {
        if (b === START) {
            return a === b
        } else if (!b) {
            return false
        } else if (a.path && b.path) {
            return (
                a.path.replace(trailingSlashRE, '') === b.path.replace(trailingSlashRE, '') &&
                a.hash === b.hash &&
                isObjectEqual(a.query, b.query)
            )
        } else if (a.name && b.name) {
            return (
                a.name === b.name &&
                a.hash === b.hash &&
                isObjectEqual(a.query, b.query) &&
                isObjectEqual(a.params, b.params)
            )
        } else {
            return false
        }
    }

    function isObjectEqual (a, b) {
        if ( a === void 0 ) a = {};
        if ( b === void 0 ) b = {};

        // handle null value #1566
        if (!a || !b) { return a === b }
        var aKeys = Object.keys(a);
        var bKeys = Object.keys(b);
        if (aKeys.length !== bKeys.length) {
            return false
        }
        return aKeys.every(function (key) {
            var aVal = a[key];
            var bVal = b[key];
            // check nested equality
            if (typeof aVal === 'object' && typeof bVal === 'object') {
                return isObjectEqual(aVal, bVal)
            }
            return String(aVal) === String(bVal)
        })
    }

    function isIncludedRoute (current, target) {
        return (
            current.path.replace(trailingSlashRE, '/').indexOf(
                target.path.replace(trailingSlashRE, '/')
            ) === 0 &&
            (!target.hash || current.hash === target.hash) &&
            queryIncludes(current.query, target.query)
        )
    }

    function queryIncludes (current, target) {
        for (var key in target) {
            if (!(key in current)) {
                return false
            }
        }
        return true
    }

    /*  */

// work around weird flow bug
    var toTypes = [String, Object];
    var eventTypes = [String, Array];

    var Link = {
        name: 'router-link',
        props: {
            to: {
                type: toTypes,
                required: true
            },
            tag: {
                type: String,
                default: 'a'
            },
            exact: Boolean,
            append: Boolean,
            replace: Boolean,
            activeClass: String,
            exactActiveClass: String,
            event: {
                type: eventTypes,
                default: 'click'
            }
        },
        render: function render (h) {
            var this$1 = this;

            var router = this.$router;
            var current = this.$route;
            var ref = router.resolve(this.to, current, this.append);
            var location = ref.location;
            var route = ref.route;
            var href = ref.href;

            var classes = {};
            var globalActiveClass = router.options.linkActiveClass;
            var globalExactActiveClass = router.options.linkExactActiveClass;
            // Support global empty active class
            var activeClassFallback = globalActiveClass == null
                ? 'router-link-active'
                : globalActiveClass;
            var exactActiveClassFallback = globalExactActiveClass == null
                ? 'router-link-exact-active'
                : globalExactActiveClass;
            var activeClass = this.activeClass == null
                ? activeClassFallback
                : this.activeClass;
            var exactActiveClass = this.exactActiveClass == null
                ? exactActiveClassFallback
                : this.exactActiveClass;
            var compareTarget = location.path
                ? createRoute(null, location, null, router)
                : route;

            classes[exactActiveClass] = isSameRoute(current, compareTarget);
            classes[activeClass] = this.exact
                ? classes[exactActiveClass]
                : isIncludedRoute(current, compareTarget);

            var handler = function (e) {
                if (guardEvent(e)) {
                    if (this$1.replace) {
                        router.replace(location);
                    } else {
                        router.push(location);
                    }
                }
            };

            var on = { click: guardEvent };
            if (Array.isArray(this.event)) {
                this.event.forEach(function (e) { on[e] = handler; });
            } else {
                on[this.event] = handler;
            }

            var data = {
                class: classes
            };

            if (this.tag === 'a') {
                data.on = on;
                data.attrs = { href: href };
            } else {
                // find the first <a> child and apply listener and href
                var a = findAnchor(this.$slots.default);
                if (a) {
                    // in case the <a> is a static node
                    a.isStatic = false;
                    var extend = _Vue.util.extend;
                    var aData = a.data = extend({}, a.data);
                    aData.on = on;
                    var aAttrs = a.data.attrs = extend({}, a.data.attrs);
                    aAttrs.href = href;
                } else {
                    // doesn't have <a> child, apply listener to self
                    data.on = on;
                }
            }

            return h(this.tag, data, this.$slots.default)
        }
    };

    function guardEvent (e) {
        // don't redirect with control keys
        if (e.metaKey || e.altKey || e.ctrlKey || e.shiftKey) { return }
        // don't redirect when preventDefault called
        if (e.defaultPrevented) { return }
        // don't redirect on right click
        if (e.button !== undefined && e.button !== 0) { return }
        // don't redirect if `target="_blank"`
        if (e.currentTarget && e.currentTarget.getAttribute) {
            var target = e.currentTarget.getAttribute('target');
            if (/\b_blank\b/i.test(target)) { return }
        }
        // this may be a Weex event which doesn't have this method
        if (e.preventDefault) {
            e.preventDefault();
        }
        return true
    }

    function findAnchor (children) {
        if (children) {
            var child;
            for (var i = 0; i < children.length; i++) {
                child = children[i];
                if (child.tag === 'a') {
                    return child
                }
                if (child.children && (child = findAnchor(child.children))) {
                    return child
                }
            }
        }
    }

    var _Vue;

    function install (Vue) {
        if (install.installed && _Vue === Vue) { return }
        install.installed = true;

        _Vue = Vue;

        var isDef = function (v) { return v !== undefined; };

        var registerInstance = function (vm, callVal) {
            var i = vm.$options._parentVnode;
            if (isDef(i) && isDef(i = i.data) && isDef(i = i.registerRouteInstance)) {
                i(vm, callVal);
            }
        };

        Vue.mixin({
            beforeCreate: function beforeCreate () {
                if (isDef(this.$options.router)) {
                    this._routerRoot = this;
                    this._router = this.$options.router;
                    this._router.init(this);
                    Vue.util.defineReactive(this, '_route', this._router.history.current);
                } else {
                    this._routerRoot = (this.$parent && this.$parent._routerRoot) || this;
                }
                registerInstance(this, this);
            },
            destroyed: function destroyed () {
                registerInstance(this);
            }
        });

        Object.defineProperty(Vue.prototype, '$router', {
            get: function get () { return this._routerRoot._router }
        });

        Object.defineProperty(Vue.prototype, '$route', {
            get: function get () { return this._routerRoot._route }
        });

        Vue.component('router-view', View);
        Vue.component('router-link', Link);

        var strats = Vue.config.optionMergeStrategies;
        // use the same hook merging strategy for route hooks
        strats.beforeRouteEnter = strats.beforeRouteLeave = strats.beforeRouteUpdate = strats.created;
    }

    /*  */

    var inBrowser = typeof window !== 'undefined';

    /*  */

    function resolvePath (
        relative,
        base,
        append
    ) {
        var firstChar = relative.charAt(0);
        if (firstChar === '/') {
            return relative
        }

        if (firstChar === '?' || firstChar === '#') {
            return base + relative
        }

        var stack = base.split('/');

        // remove trailing segment if:
        // - not appending
        // - appending to trailing slash (last segment is empty)
        if (!append || !stack[stack.length - 1]) {
            stack.pop();
        }

        // resolve relative path
        var segments = relative.replace(/^\//, '').split('/');
        for (var i = 0; i < segments.length; i++) {
            var segment = segments[i];
            if (segment === '..') {
                stack.pop();
            } else if (segment !== '.') {
                stack.push(segment);
            }
        }

        // ensure leading slash
        if (stack[0] !== '') {
            stack.unshift('');
        }

        return stack.join('/')
    }

    function parsePath (path) {
        var hash = '';
        var query = '';

        var hashIndex = path.indexOf('#');
        if (hashIndex >= 0) {
            hash = path.slice(hashIndex);
            path = path.slice(0, hashIndex);
        }

        var queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            query = path.slice(queryIndex + 1);
            path = path.slice(0, queryIndex);
        }

        return {
            path: path,
            query: query,
            hash: hash
        }
    }

    function cleanPath (path) {
        return path.replace(/\/\//g, '/')
    }

    var isarray = Array.isArray || function (arr) {
            return Object.prototype.toString.call(arr) == '[object Array]';
        };

    /**
     * Expose `pathToRegexp`.
     */
    var pathToRegexp_1 = pathToRegexp;
    var parse_1 = parse;
    var compile_1 = compile;
    var tokensToFunction_1 = tokensToFunction;
    var tokensToRegExp_1 = tokensToRegExp;

    /**
     * The main path matching regexp utility.
     *
     * @type {RegExp}
     */
    var PATH_REGEXP = new RegExp([
        // Match escaped characters that would otherwise appear in future matches.
        // This allows the user to escape special characters that won't transform.
        '(\\\\.)',
        // Match Express-style parameters and un-named parameters with a prefix
        // and optional suffixes. Matches appear as:
        //
        // "/:test(\\d+)?" => ["/", "test", "\d+", undefined, "?", undefined]
        // "/route(\\d+)"  => [undefined, undefined, undefined, "\d+", undefined, undefined]
        // "/*"            => ["/", undefined, undefined, undefined, undefined, "*"]
        '([\\/.])?(?:(?:\\:(\\w+)(?:\\(((?:\\\\.|[^\\\\()])+)\\))?|\\(((?:\\\\.|[^\\\\()])+)\\))([+*?])?|(\\*))'
    ].join('|'), 'g');

    /**
     * Parse a string for the raw tokens.
     *
     * @param  {string}  str
     * @param  {Object=} options
     * @return {!Array}
     */
    function parse (str, options) {
        var tokens = [];
        var key = 0;
        var index = 0;
        var path = '';
        var defaultDelimiter = options && options.delimiter || '/';
        var res;

        while ((res = PATH_REGEXP.exec(str)) != null) {
            var m = res[0];
            var escaped = res[1];
            var offset = res.index;
            path += str.slice(index, offset);
            index = offset + m.length;

            // Ignore already escaped sequences.
            if (escaped) {
                path += escaped[1];
                continue
            }

            var next = str[index];
            var prefix = res[2];
            var name = res[3];
            var capture = res[4];
            var group = res[5];
            var modifier = res[6];
            var asterisk = res[7];

            // Push the current path onto the tokens.
            if (path) {
                tokens.push(path);
                path = '';
            }

            var partial = prefix != null && next != null && next !== prefix;
            var repeat = modifier === '+' || modifier === '*';
            var optional = modifier === '?' || modifier === '*';
            var delimiter = res[2] || defaultDelimiter;
            var pattern = capture || group;

            tokens.push({
                name: name || key++,
                prefix: prefix || '',
                delimiter: delimiter,
                optional: optional,
                repeat: repeat,
                partial: partial,
                asterisk: !!asterisk,
                pattern: pattern ? escapeGroup(pattern) : (asterisk ? '.*' : '[^' + escapeString(delimiter) + ']+?')
            });
        }

        // Match any characters still remaining.
        if (index < str.length) {
            path += str.substr(index);
        }

        // If the path exists, push it onto the end.
        if (path) {
            tokens.push(path);
        }

        return tokens
    }

    /**
     * Compile a string to a template function for the path.
     *
     * @param  {string}             str
     * @param  {Object=}            options
     * @return {!function(Object=, Object=)}
     */
    function compile (str, options) {
        return tokensToFunction(parse(str, options))
    }

    /**
     * Prettier encoding of URI path segments.
     *
     * @param  {string}
     * @return {string}
     */
    function encodeURIComponentPretty (str) {
        return encodeURI(str).replace(/[\/?#]/g, function (c) {
            return '%' + c.charCodeAt(0).toString(16).toUpperCase()
        })
    }

    /**
     * Encode the asterisk parameter. Similar to `pretty`, but allows slashes.
     *
     * @param  {string}
     * @return {string}
     */
    function encodeAsterisk (str) {
        return encodeURI(str).replace(/[?#]/g, function (c) {
            return '%' + c.charCodeAt(0).toString(16).toUpperCase()
        })
    }

    /**
     * Expose a method for transforming tokens into the path function.
     */
    function tokensToFunction (tokens) {
        // Compile all the tokens into regexps.
        var matches = new Array(tokens.length);

        // Compile all the patterns before compilation.
        for (var i = 0; i < tokens.length; i++) {
            if (typeof tokens[i] === 'object') {
                matches[i] = new RegExp('^(?:' + tokens[i].pattern + ')$');
            }
        }

        return function (obj, opts) {
            var path = '';
            var data = obj || {};
            var options = opts || {};
            var encode = options.pretty ? encodeURIComponentPretty : encodeURIComponent;

            for (var i = 0; i < tokens.length; i++) {
                var token = tokens[i];

                if (typeof token === 'string') {
                    path += token;

                    continue
                }

                var value = data[token.name];
                var segment;

                if (value == null) {
                    if (token.optional) {
                        // Prepend partial segment prefixes.
                        if (token.partial) {
                            path += token.prefix;
                        }

                        continue
                    } else {
                        throw new TypeError('Expected "' + token.name + '" to be defined')
                    }
                }

                if (isarray(value)) {
                    if (!token.repeat) {
                        throw new TypeError('Expected "' + token.name + '" to not repeat, but received `' + JSON.stringify(value) + '`')
                    }

                    if (value.length === 0) {
                        if (token.optional) {
                            continue
                        } else {
                            throw new TypeError('Expected "' + token.name + '" to not be empty')
                        }
                    }

                    for (var j = 0; j < value.length; j++) {
                        segment = encode(value[j]);

                        if (!matches[i].test(segment)) {
                            throw new TypeError('Expected all "' + token.name + '" to match "' + token.pattern + '", but received `' + JSON.stringify(segment) + '`')
                        }

                        path += (j === 0 ? token.prefix : token.delimiter) + segment;
                    }

                    continue
                }

                segment = token.asterisk ? encodeAsterisk(value) : encode(value);

                if (!matches[i].test(segment)) {
                    throw new TypeError('Expected "' + token.name + '" to match "' + token.pattern + '", but received "' + segment + '"')
                }

                path += token.prefix + segment;
            }

            return path
        }
    }

    /**
     * Escape a regular expression string.
     *
     * @param  {string} str
     * @return {string}
     */
    function escapeString (str) {
        return str.replace(/([.+*?=^!:${}()[\]|\/\\])/g, '\\$1')
    }

    /**
     * Escape the capturing group by escaping special characters and meaning.
     *
     * @param  {string} group
     * @return {string}
     */
    function escapeGroup (group) {
        return group.replace(/([=!:$\/()])/g, '\\$1')
    }

    /**
     * Attach the keys as a property of the regexp.
     *
     * @param  {!RegExp} re
     * @param  {Array}   keys
     * @return {!RegExp}
     */
    function attachKeys (re, keys) {
        re.keys = keys;
        return re
    }

    /**
     * Get the flags for a regexp from the options.
     *
     * @param  {Object} options
     * @return {string}
     */
    function flags (options) {
        return options.sensitive ? '' : 'i'
    }

    /**
     * Pull out keys from a regexp.
     *
     * @param  {!RegExp} path
     * @param  {!Array}  keys
     * @return {!RegExp}
     */
    function regexpToRegexp (path, keys) {
        // Use a negative lookahead to match only capturing groups.
        var groups = path.source.match(/\((?!\?)/g);

        if (groups) {
            for (var i = 0; i < groups.length; i++) {
                keys.push({
                    name: i,
                    prefix: null,
                    delimiter: null,
                    optional: false,
                    repeat: false,
                    partial: false,
                    asterisk: false,
                    pattern: null
                });
            }
        }

        return attachKeys(path, keys)
    }

    /**
     * Transform an array into a regexp.
     *
     * @param  {!Array}  path
     * @param  {Array}   keys
     * @param  {!Object} options
     * @return {!RegExp}
     */
    function arrayToRegexp (path, keys, options) {
        var parts = [];

        for (var i = 0; i < path.length; i++) {
            parts.push(pathToRegexp(path[i], keys, options).source);
        }

        var regexp = new RegExp('(?:' + parts.join('|') + ')', flags(options));

        return attachKeys(regexp, keys)
    }

    /**
     * Create a path regexp from string input.
     *
     * @param  {string}  path
     * @param  {!Array}  keys
     * @param  {!Object} options
     * @return {!RegExp}
     */
    function stringToRegexp (path, keys, options) {
        return tokensToRegExp(parse(path, options), keys, options)
    }

    /**
     * Expose a function for taking tokens and returning a RegExp.
     *
     * @param  {!Array}          tokens
     * @param  {(Array|Object)=} keys
     * @param  {Object=}         options
     * @return {!RegExp}
     */
    function tokensToRegExp (tokens, keys, options) {
        if (!isarray(keys)) {
            options = /** @type {!Object} */ (keys || options);
            keys = [];
        }

        options = options || {};

        var strict = options.strict;
        var end = options.end !== false;
        var route = '';

        // Iterate over the tokens and create our regexp string.
        for (var i = 0; i < tokens.length; i++) {
            var token = tokens[i];

            if (typeof token === 'string') {
                route += escapeString(token);
            } else {
                var prefix = escapeString(token.prefix);
                var capture = '(?:' + token.pattern + ')';

                keys.push(token);

                if (token.repeat) {
                    capture += '(?:' + prefix + capture + ')*';
                }

                if (token.optional) {
                    if (!token.partial) {
                        capture = '(?:' + prefix + '(' + capture + '))?';
                    } else {
                        capture = prefix + '(' + capture + ')?';
                    }
                } else {
                    capture = prefix + '(' + capture + ')';
                }

                route += capture;
            }
        }

        var delimiter = escapeString(options.delimiter || '/');
        var endsWithDelimiter = route.slice(-delimiter.length) === delimiter;

        // In non-strict mode we allow a slash at the end of match. If the path to
        // match already ends with a slash, we remove it for consistency. The slash
        // is valid at the end of a path match, not in the middle. This is important
        // in non-ending mode, where "/test/" shouldn't match "/test//route".
        if (!strict) {
            route = (endsWithDelimiter ? route.slice(0, -delimiter.length) : route) + '(?:' + delimiter + '(?=$))?';
        }

        if (end) {
            route += '$';
        } else {
            // In non-ending mode, we need the capturing groups to match as much as
            // possible by using a positive lookahead to the end or next path segment.
            route += strict && endsWithDelimiter ? '' : '(?=' + delimiter + '|$)';
        }

        return attachKeys(new RegExp('^' + route, flags(options)), keys)
    }

    /**
     * Normalize the given path string, returning a regular expression.
     *
     * An empty array can be passed in for the keys, which will hold the
     * placeholder key descriptions. For example, using `/user/:id`, `keys` will
     * contain `[{ name: 'id', delimiter: '/', optional: false, repeat: false }]`.
     *
     * @param  {(string|RegExp|Array)} path
     * @param  {(Array|Object)=}       keys
     * @param  {Object=}               options
     * @return {!RegExp}
     */
    function pathToRegexp (path, keys, options) {
        if (!isarray(keys)) {
            options = /** @type {!Object} */ (keys || options);
            keys = [];
        }

        options = options || {};

        if (path instanceof RegExp) {
            return regexpToRegexp(path, /** @type {!Array} */ (keys))
        }

        if (isarray(path)) {
            return arrayToRegexp(/** @type {!Array} */ (path), /** @type {!Array} */ (keys), options)
        }

        return stringToRegexp(/** @type {string} */ (path), /** @type {!Array} */ (keys), options)
    }

    pathToRegexp_1.parse = parse_1;
    pathToRegexp_1.compile = compile_1;
    pathToRegexp_1.tokensToFunction = tokensToFunction_1;
    pathToRegexp_1.tokensToRegExp = tokensToRegExp_1;

    /*  */

// $flow-disable-line
    var regexpCompileCache = Object.create(null);

    function fillParams (
        path,
        params,
        routeMsg
    ) {
        try {
            var filler =
                regexpCompileCache[path] ||
                (regexpCompileCache[path] = pathToRegexp_1.compile(path));
            return filler(params || {}, { pretty: true })
        } catch (e) {
            {
                warn(false, ("missing param for " + routeMsg + ": " + (e.message)));
            }
            return ''
        }
    }

    /*  */

    function createRouteMap (
        routes,
        oldPathList,
        oldPathMap,
        oldNameMap
    ) {
        // the path list is used to control path matching priority
        var pathList = oldPathList || [];
        // $flow-disable-line
        var pathMap = oldPathMap || Object.create(null);
        // $flow-disable-line
        var nameMap = oldNameMap || Object.create(null);

        routes.forEach(function (route) {
            addRouteRecord(pathList, pathMap, nameMap, route);
        });

        // ensure wildcard routes are always at the end
        for (var i = 0, l = pathList.length; i < l; i++) {
            if (pathList[i] === '*') {
                pathList.push(pathList.splice(i, 1)[0]);
                l--;
                i--;
            }
        }

        return {
            pathList: pathList,
            pathMap: pathMap,
            nameMap: nameMap
        }
    }

    function addRouteRecord (
        pathList,
        pathMap,
        nameMap,
        route,
        parent,
        matchAs
    ) {
        var path = route.path;
        var name = route.name;
        {
            assert(path != null, "\"path\" is required in a route configuration.");
            assert(
                typeof route.component !== 'string',
                "route config \"component\" for path: " + (String(path || name)) + " cannot be a " +
                "string id. Use an actual component instead."
            );
        }

        var pathToRegexpOptions = route.pathToRegexpOptions || {};
        var normalizedPath = normalizePath(
            path,
            parent,
            pathToRegexpOptions.strict
        );

        if (typeof route.caseSensitive === 'boolean') {
            pathToRegexpOptions.sensitive = route.caseSensitive;
        }

        var record = {
            path: normalizedPath,
            regex: compileRouteRegex(normalizedPath, pathToRegexpOptions),
            components: route.components || { default: route.component },
            instances: {},
            name: name,
            parent: parent,
            matchAs: matchAs,
            redirect: route.redirect,
            beforeEnter: route.beforeEnter,
            meta: route.meta || {},
            props: route.props == null
                ? {}
                : route.components
                    ? route.props
                    : { default: route.props }
        };

        if (route.children) {
            // Warn if route is named, does not redirect and has a default child route.
            // If users navigate to this route by name, the default child will
            // not be rendered (GH Issue #629)
            {
                if (route.name && !route.redirect && route.children.some(function (child) { return /^\/?$/.test(child.path); })) {
                    warn(
                        false,
                        "Named Route '" + (route.name) + "' has a default child route. " +
                        "When navigating to this named route (:to=\"{name: '" + (route.name) + "'\"), " +
                        "the default child route will not be rendered. Remove the name from " +
                        "this route and use the name of the default child route for named " +
                        "links instead."
                    );
                }
            }
            route.children.forEach(function (child) {
                var childMatchAs = matchAs
                    ? cleanPath((matchAs + "/" + (child.path)))
                    : undefined;
                addRouteRecord(pathList, pathMap, nameMap, child, record, childMatchAs);
            });
        }

        if (route.alias !== undefined) {
            var aliases = Array.isArray(route.alias)
                ? route.alias
                : [route.alias];

            aliases.forEach(function (alias) {
                var aliasRoute = {
                    path: alias,
                    children: route.children
                };
                addRouteRecord(
                    pathList,
                    pathMap,
                    nameMap,
                    aliasRoute,
                    parent,
                    record.path || '/' // matchAs
                );
            });
        }

        if (!pathMap[record.path]) {
            pathList.push(record.path);
            pathMap[record.path] = record;
        }

        if (name) {
            if (!nameMap[name]) {
                nameMap[name] = record;
            } else if ("development" !== 'production' && !matchAs) {
                warn(
                    false,
                    "Duplicate named routes definition: " +
                    "{ name: \"" + name + "\", path: \"" + (record.path) + "\" }"
                );
            }
        }
    }

    function compileRouteRegex (path, pathToRegexpOptions) {
        var regex = pathToRegexp_1(path, [], pathToRegexpOptions);
        {
            var keys = Object.create(null);
            regex.keys.forEach(function (key) {
                warn(!keys[key.name], ("Duplicate param keys in route with path: \"" + path + "\""));
                keys[key.name] = true;
            });
        }
        return regex
    }

    function normalizePath (path, parent, strict) {
        if (!strict) { path = path.replace(/\/$/, ''); }
        if (path[0] === '/') { return path }
        if (parent == null) { return path }
        return cleanPath(((parent.path) + "/" + path))
    }

    /*  */


    function normalizeLocation (
        raw,
        current,
        append,
        router
    ) {
        var next = typeof raw === 'string' ? { path: raw } : raw;
        // named target
        if (next.name || next._normalized