(function () {
    'use strict';

    var widget_state,
        config = {
            view: {
                defaults: {
                    title: 'codeCoverage' // widget title
                },
                controller: 'CodeCoverageWidgetViewController',
                controllerAs: 'buildView',
                templateUrl: 'components/widgets/codeCoverage/view.html'
            },
            config: {
                controller: 'CodeCoverageWidgetConfigController',
                controllerAs: 'codeCoverageConfig',
                templateUrl: 'components/widgets/codeCoverage/config.html'
            },
            getState: getState,
            collectors: ['codeCoverage']
        };

    angular
        .module('devops-dashboard')
        .config(register);

    register.$inject = ['widgetManagerProvider', 'WIDGET_STATE'];
    function register(widgetManagerProvider, WIDGET_STATE) {
        widget_state = WIDGET_STATE;
        widgetManagerProvider.register('codeCoverage', config);
    }

    function getState(config) {
        // make sure config values are set
        return localTesting || (config.id && config.options.CodeCoverage && config.options.CodeCoverageFunctional) ?
            widget_state.READY :
            widget_state.CONFIGURE;
    }
})();
