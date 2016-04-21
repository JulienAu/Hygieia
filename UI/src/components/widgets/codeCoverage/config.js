/**
 * Build widget configuration
 */
(function () {
    'use strict';

    angular
        .module('devops-dashboard')
        .controller('CodeCoverageWidgetConfigController', CodeCoverageWidgetConfigController);

    CodeCoverageWidgetConfigController.$inject = ['modalData', '$scope', 'collectorData', '$modalInstance'];
    function CodeCoverageWidgetConfigController(modalData, $scope, collectorData, $modalInstance) {
        var ctrl = this;
        var widgetConfig = modalData.widgetConfig;

        // public variables
        ctrl.toolsDropdownPlaceholder = 'Loading Build Jobs...';
        ctrl.toolsDropdownDisabled = true;

        ctrl.CodeCoverage = 'UT_Coverage';
        ctrl.CodeCoverageFunctional = 'Functional_Coverage';

        ctrl.submitted = false;

        // set values from config
        if (widgetConfig) {
            if (widgetConfig.options.CodeCoverage) {
                ctrl.CodeCoverage = widgetConfig.options.CodeCoverage;
            }

            if (widgetConfig.options.CodeCoverageFunctional) {
                ctrl.CodeCoverageFunctional = widgetConfig.options.CodeCoverageFunctional;
            }
        }

        // public methods
        ctrl.submit = submitForm;

        // request all the build collector items
        collectorData.itemsByType('build2').then(processResponse);

        // method implementations
        function processResponse(data) {
            var worker = {
                getBuildJobs: getBuildJobs
            };

            function getBuildJobs(data, currentCollectorItemId, cb) {
                var builds = [],
                    selectedIndex = null;

                for (var x = 0; x < data.length; x++) {
                    var obj = data[x];
                    var item = {
                        value: obj.id,
                        name: obj.collector.name + ' - ' + obj.description
                    };

                    builds.push(item);

                    if (currentCollectorItemId !== null && item.value == currentCollectorItemId) {
                        selectedIndex = x;
                    }
                }

                cb({
                    builds: builds,
                    selectedIndex: selectedIndex
                });
            }

            var buildCollector = modalData.dashboard.application.components[0].collectorItems.Build2;
            var buildCollectorId = buildCollector ? buildCollector[0].id : null;
            worker.getBuildJobs(data, buildCollectorId, getBuildsCallback);
        }

        function getBuildsCallback(data) {
            //$scope.$apply(function () {
                ctrl.buildJobs = data.builds;
                ctrl.toolsDropdownPlaceholder = 'Select a Build Job';
                ctrl.toolsDropdownDisabled = false;

                if (data.selectedIndex !== null) {
                    ctrl.collectorItemId = ctrl.buildJobs[data.selectedIndex];
                }
            //});
        }

        function submitForm(valid) {
            ctrl.submitted = true;
            if (valid) {
                var form = document.configForm;
                var postObj = {
                    name: 'codeCoverage',
                    options: {
                        id: widgetConfig.options.id,
                        CodeCoverage: form.CodeCoverage.value,
                        CodeCoverageFunctional: form.CodeCoverageFunctional.value
                    },
                    componentId: modalData.dashboard.application.components[0].id,
                    collectorItemId: form.collectorItemId.value
                };

                // pass this new config to the modal closing so it's saved
                $modalInstance.close(postObj);
            }
        }
    }
})();
