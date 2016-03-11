(function () {
    'use strict';

    var widget_state,
        config = {
            view: {
                defaults: {
                    title: 'graph' // widget title
                },
                controller: 'GraphWidgetViewController',
                controllerAs: 'buildView',
                templateUrl: 'components/widgets/graph/view.html'
            },
            config: {
                controller: 'GraphWidgetConfigController',
                controllerAs: 'graphConfig',
                templateUrl: 'components/widgets/graph/config.html'
            },
            getState: getState,
            collectors: ['graph']
        };

    angular
        .module('devops-dashboard')
        .config(register);

    register.$inject = ['widgetManagerProvider', 'WIDGET_STATE'];
    function register(widgetManagerProvider, WIDGET_STATE) {
        widget_state = WIDGET_STATE;
        widgetManagerProvider.register('graph', config);
    }

    function getState(config) {
        // make sure config values are set
        return localTesting || (config.id && config.options.buildDurationThreshold && config.options.consecutiveFailureThreshold) ?
            widget_state.READY :
            widget_state.CONFIGURE;
    }
})();
