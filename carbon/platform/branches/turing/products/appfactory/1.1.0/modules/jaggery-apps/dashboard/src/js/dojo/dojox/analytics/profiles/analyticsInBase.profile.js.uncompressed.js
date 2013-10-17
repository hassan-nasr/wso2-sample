dependencies = {
        layers: [
                {
			//name: "../dojox/analytics.js",
			name: "dojo.js",
                        dependencies: [
								"dojox.analytics",
								"dojox.analytics.plugins.dojo",
								"dojox.analytics.plugins.window",
								"dojox.analytics.plugins.consoleMessages",
								"dojox.analytics.plugins.mouseOver",
								"dojox.analytics.plugins.mouseClick",
								"dojox.analytics.plugins.touchPress",
								"dojox.analytics.plugins.touchMove",
								"dojox.analytics.plugins.gestureEvents",
								"dojox.analytics.plugins.idle"]
                }
	],

	prefixes: [
                [ "dojox", "../dojox" ],
                [ "dijit", "../dijit" ]
        ]
}
