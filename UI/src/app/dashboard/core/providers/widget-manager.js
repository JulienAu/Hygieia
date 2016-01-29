/**
 * Registers the widget configuration typically defined in the module.js
 */
(function () {
    'use strict';

    var widgets = {};
    var k = 1;
    angular
        .module('devops-dashboard.core')
        .provider('widgetManager', widgetManagerProvider);


    function widgetManagerProvider() {
        return {
            $get: widgetManagerApi,
            register: register
        };
    }

    function widgetManagerApi() {
        return {
            getWidgets: getWidgets,
            getWidget: getWidget
        };
    }

    function register(widgetName, options) {
        widgetName = widgetName.toLowerCase();

        // don't allow widgets to be registered twice
       if (widgets[widgetName]) {
            //throw new Error(widgetName + ' already registered!');
            k = k+1 ;
            widgetName = widgetName+"k";
        }

        // make sure certain values are set
        if (!options.view || !options.view.controller || !options.view.templateUrl) {
            throw new Error(widgetName + ' must be registered with the controller, and templateUrl values defined');
        }

        widgets[widgetName] = options;
    }

    function getWidgets() {
        return widgets;
    }

    function getWidget(widgetName) {
        widgetName = widgetName.toLowerCase();

        return widgets[widgetName];
    }
})();
