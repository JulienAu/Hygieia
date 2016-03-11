(function () {
    'use strict';

    var app = angular
        .module('devops-dashboard');

    var directives = [
        'buildsPerDays',
        'buildsPerDaysFail',
    ];

    _(directives).forEach(function (name) {
        app.directive(name, function () {
            return {
                restrict: 'E',
                templateUrl: 'components/widgets/graph/directives/' + name + '.html'
            };
        });
    });


})();
