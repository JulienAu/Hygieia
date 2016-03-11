/**
 * View controller for the build widget
 */
(function () {
    'use strict';

    angular
        .module('devops-dashboard')
        .controller('GraphWidgetViewController', GraphWidgetViewController);

    GraphWidgetViewController.$inject = ['$scope', 'buildData', 'DISPLAY_STATE', '$q', '$modal'];
    function GraphWidgetViewController($scope, buildData, DISPLAY_STATE, $q, $modal) {
        var ctrl = this;
        var builds = [];

        //region Chart Configuration
        // line chart config
        ctrl.lineData = {
            labels: [],
            series: []
        };

        ctrl.lineDataFail = {
            labels: [],
            series: []
        };


        ctrl.lineOptions = {
            plugins: [
                Chartist.plugins.gridBoundaries(),
                Chartist.plugins.lineAboveArea(),
                Chartist.plugins.tooltip(),
                Chartist.plugins.pointHalo()
            ],
            showArea: true,
            lineSmooth: false,
            fullWidth: true,
            chartPadding: 7,
            axisY: {
                offset: 30,
                //showGrid: false,
                showLabel: true,
                labelInterpolationFnc: function(value) {
                    return value === 0 ? 0 : ((Math.round(value * 100) / 100) + '');
                }
            }
        };

 // bar chart config
        ctrl.buildDurationData = {
            labels: [],
            series: [[]]
        };

        ctrl.buildDurationOptions = {
            plugins: [
                Chartist.plugins.threshold({
                    threshold: $scope.widgetConfig.options.buildDurationThreshold || 10
                }),
                Chartist.plugins.gridBoundaries(),
                Chartist.plugins.tooltip()
            ],
            stackBars: true,
            centerLabels: true,
            axisY: {
                offset: 30,
                labelInterpolationFnc: function(value) {
                    return value === 0 ? 0 : ((Math.round(value * 100) / 100) + '');
                }
            }
        };

        ctrl.buildDurationEvents = {
            'draw': draw
        };
        //endregion
       

        ctrl.load = function() {
            var deferred = $q.defer();
            var params = {
                componentId: $scope.widgetConfig.componentId,
                numberOfDays: 15
            };
            buildData.details(params).then(function(data) {
                builds = data.result;
                processResponse(builds);
                deferred.resolve(data.lastUpdated);
            });
            return deferred.promise;
        };
        

        ctrl.open = function (url) {
            window.open(url);
        };

        
        // creates the two-color point design
        // the custom class, 'ct-point-halo' can be used to style the outline
        function draw(data) {
            if (data.type === 'bar') {
                if (data.value > 0) {
                    data.group.append(new Chartist.Svg('circle', {
                        cx: data.x2,
                        cy: data.y2,
                        r: 7
                    }, 'ct-slice'));
                    data.y2 -= 7;
                }
            }

            if (data.type === 'point') {
                data.group.append(new Chartist.Svg('circle', {
                    cx: data.x,
                    cy: data.y,
                    r: 2
                }, 'ct-point-halo'), true);
            }
        }

        //region Processing API Response
        function processResponse(data) {
            var worker = {
                    buildsPerDays: buildsPerDays//,
                    //buildsPerDaysFail: buildsPerDaysFail
                };

            //region web worker method implementations
            function filter(data) {
                    return _.filter(data, function (item) {
                        return Math.floor(moment(item.endTime).endOf('day').diff(moment(new Date()).endOf('day'), 'days')) >= -365;
                    });
                }

                function group(data) {
                    return _.groupBy(data, function (item) {
                        return moment(item.endTime).format('L');
                    });
                }

                function getSeries() {
                    var result = getPassFail(simplify(group(filter(data))));

                    return result.passed;
                }

                function getSeries2() {
                    var result = getPassFail(simplify2(group(filter(data))));

                    return result.passed;
                }

                 
                function simplify(data) {
                    // create array with date as the key and build duration times in an array
                    var simplifiedData = {};
                    _.forEach(data, function (buildDay, key) {
                        if (!simplifiedData[key]) {
                            simplifiedData[key] = [];
                        }

                        _.forEach(buildDay, function (build) {
                            var duration = parseInt(build.passCount);
                            simplifiedData[key].push(duration);
                            
                        });
                    });

                    return simplifiedData;
                }

                function simplify2(data) {
                    // create array with date as the key and build duration times in an array
                    var simplifiedData = {};
                    _.forEach(data, function (buildDay, key) {
                        if (!simplifiedData[key]) {
                            simplifiedData[key] = [];
                        }

                        _.forEach(buildDay, function (build) {
                            var duration = parseInt(build.failCount);
                            simplifiedData[key].push(duration);
                            
                        });
                    });

                    return simplifiedData;
                }

                function getPassFail(simplifiedData) {
                    // loop through all days in the past two weeks in case there weren't any builds
                    // on that date
                    var passed = [];
                    for (var x = 0; x <= 364; x++) {
                        var date = moment(new Date()).subtract(x, 'days').format('L');
                        var data = simplifiedData[date];

                        // if date has no builds, add 0,0
                        if (!data || !data.length) {
                            //passed.push(0);
                        }
                        else {
                            // calculate average and put in proper
                            var avg = _(data).reduce(function(a,b) {
                                    return a + b;
                                }) / data.length;
                            /*if (avg > buildThreshold) {
                                passed.push(0);
                            }
                            else {*/
                                passed.push(avg);
 
                            //}
                        }
                    }

                    return {
                        passed: passed.reverse(),
                    };
                }


                function toMidnight(date) {
                    date.setHours(0, 0, 0, 0);
                    return date;
                }

            function buildsPerDays(data, cb) {
                cb({
                    passed: getSeries(),
                    failed: getSeries2()
                });

            }

/*
            function buildsPerDaysFail(data, cb) {
                cb({
                    failed: getSeries2()
                });

            }*/

            //endregion

            //region web worker calls
            // call to webworker methods nad set the controller variables with the processed values
            worker.buildsPerDays(data, function (data) {
                //$scope.$apply(function () {
                    console.log(data);

                    ctrl.lineData.series = [
                        {
                            name: 'success',
                            data: data.passed
                        }, {
                            name: 'failures',
                            data: data.failed
                        }
                    ];
                //});
            });

            /*
            worker.buildsPerDaysFail(data, function (data) {
                //$scope.$apply(function () {
                    console.log(data);

                    ctrl.lineDataFail.series = [
                        {
                            name: 'success',
                            data: data.passed
                        }, {
                            name: 'failures',
                            data: data.failed
                        }
                    ];
                //});
            });*/
        }
        //endregion
    }
})();
