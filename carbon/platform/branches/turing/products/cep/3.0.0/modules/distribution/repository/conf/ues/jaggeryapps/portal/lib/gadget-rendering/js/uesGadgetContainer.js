/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var ues = ues || {};
ues.gadgets = ues.gadgets || {};

(function () {
// url base should be <host>:<port>//<contextRoot>
    var urlBase = location.href.substr(0, location.href.indexOf('/uec'));
    var contextRoot = urlBase.substr(urlBase.indexOf(location.host) + location.host.length);

    var uesConfig = uesConfig || {};
    uesConfig[osapi.container.ServiceConfig.API_PATH] = contextRoot + '/rpc';
    uesConfig[osapi.container.ContainerConfig.RENDER_DEBUG] = '1';

// Default the security token for the container. Using this example security token requires enabling
// the DefaultSecurityTokenCodec to let UrlParameterAuthenticationHandler create valid security token.
// 10 seconds is fast, but this is mostly for demonstration purposes.
    uesConfig[osapi.container.ContainerConfig.GET_CONTAINER_TOKEN] = function (callback) {
//    gadgets.log('Updating container security token.');
        callback('john.doe:john.doe:appid:cont:url:0:default', 10);
    };

//  Create the new ues.gadgetContainer
    ues.gadgets.gadgetContainer = new osapi.container.Container(uesConfig);

//Gadget site to title id map
    var siteToTitleMap = {};

// Need to pull these from values supplied in the dialog
    ues.gadgets.gadgetContainer.init = function () {

        //Create my new managed hub
        ues.gadgets.gadgetContainer.managedHub = new OpenAjax.hub.ManagedHub({
            onSubscribe: function (topic, container) {
                log(container.getClientID() + " subscribes to this topic '" + topic + "'");
                return true;// return false to reject the request.
            },
            onUnsubscribe: function (topic, container) {
                log(container.getClientID() + " unsubscribes from this topic '" + topic + "'");
                return true;
            },
            onPublish: function (topic, data, pcont, scont) {
                log(pcont.getClientID() + " publishes '" + data + "' to topic '" + topic + "' subscribed by " + scont.getClientID());
                return true;
                // return false to reject the request.
            }
        });
        //  initialize managed hub for the Container
        gadgets.pubsub2router.init({
            hub: ues.gadgets.gadgetContainer.managedHub
        });

        ues.gadgets.gadgetContainer.rpcRegister('set_title', window.setTitleHandler);
        ues.gadgets.gadgetContainer.addGadgetLifecycleCallback('com.example.commoncontainer', lifecycle());

        try {

            // Connect to the ManagedHub
            ues.gadgets.gadgetContainer.inlineClient =
                new OpenAjax.hub.InlineContainer(ues.gadgets.gadgetContainer.managedHub, 'container',
                    {
                        Container: {
                            onSecurityAlert: function (source, alertType) { /* Handle client-side security alerts */
                            },
                            onConnect: function (container) { /* Called when client connects */
                            },
                            onDisconnect: function (container) { /* Called when client connects */
                            }
                        }
                    });
            //connect to the inline client
            ues.gadgets.gadgetContainer.inlineClient.connect();

        } catch (e) {
            // TODO: error handling should be consistent with other OS gadget initialization error handling
            alert('ERROR creating or connecting InlineClient in ues.gadgets.gadgetContainer.managedHub [' + e.message + ']');
        }
    };

//Wrapper function to set the gadget site/id and default width.  Currently have some inconsistency with width actually being set. This
//seems to be related to the pubsub2 feature.
    ues.gadgets.gadgetContainer.renderGadget = function (gadgetURL, gadgetId, element, params) {
        params[osapi.container.RenderParam.WIDTH] = '100%';
        var gadgetSite = ues.gadgets.gadgetContainer.newGadgetSite(element);
        ues.gadgets.gadgetContainer.navigateGadget(gadgetSite, gadgetURL, {}, params);
        return gadgetSite;
    };

//TODO:  To be implemented. Identify where to hook this into the page (in the gadget title bar/gadget management, etc)
    ues.gadgets.gadgetContainer.navigateView = function (gadgetSite, gadgetURL, view) {
        var renderParms = {};
        if (view === null || view === '') {
            view = 'default';
        }
        //TODO Evaluate Parms based on configuration
        renderParms[osapi.container.RenderParam.WIDTH] = '100%';
        renderParms['view'] = view;

        ues.gadgets.gadgetContainer.navigateGadget(gadgetSite, gadgetURL, {}, renderParms);
    };

//TODO:  Add in UI controls in portlet header to remove gadget from the canvas
    ues.gadgets.gadgetContainer.collapseGadget = function (gadgetSite) {
        ues.gadgets.gadgetContainer.closeGadget(gadgetSite);
    };

//display the pubsub 2 event details
    function log(message) {
        //document.getElementById('output').innerHTML = gadgets.util.escapeString(message) + '<br/>' + document.getElementById('output').innerHTML;
    }

    var lifecycle = function () {
        var preloadStart;
        var navigateStart;
        var closeStart;
        var unloadStart;
        var renderStart;
        var listeners = {};
        listeners[osapi.container.CallbackType.ON_BEFORE_PRELOAD] = function (gadgetUrls) {
            preloadStart = osapi.container.util.getCurrentTimeMs();
        };
        listeners[osapi.container.CallbackType.ON_PRELOADED] = function (response) {
            var urls = [];
            for (url in response) {
                urls[urls.length] = url;
            }
            var dif = osapi.container.util.getCurrentTimeMs() - preloadStart;
            log('It took ' + dif + 'ms to preload the URL(s) ' + urls + '.');
        };
        listeners[osapi.container.CallbackType.ON_BEFORE_NAVIGATE] = function (gadgetUrl) {
            navigateStart = osapi.container.util.getCurrentTimeMs();
        };
        listeners[osapi.container.CallbackType.ON_NAVIGATED] = function (site) {
            log('It took ' + (osapi.container.util.getCurrentTimeMs() - navigateStart) + ' ms' +
                ' for the site ' + site.getId() + ' to navigate.');
        };
        listeners[osapi.container.CallbackType.ON_BEFORE_CLOSE] = function (site) {
            closeStart = osapi.container.util.getCurrentTimeMs();
        };
        listeners[osapi.container.CallbackType.ON_CLOSED] = function (site) {
            log('It took ' + (osapi.container.util.getCurrentTimeMs() - closeStart) +
                ' ms to close the gadget in the site with id ' + site.getId());
        };
        listeners[osapi.container.CallbackType.ON_BEFORE_RENDER] = function (gadgetUrl) {
            renderStart = osapi.container.util.getCurrentTimeMs();
        };
        listeners[osapi.container.CallbackType.ON_RENDER] = function (gadgetUrl) {
            log('It took ' + (osapi.container.util.getCurrentTimeMs() - renderStart) +
                ' ms to render the gadget at the URL ' + gadgetUrl);
        };
        return listeners;
    }
}());
