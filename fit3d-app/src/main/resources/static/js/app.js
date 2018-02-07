var app = angular.module('app', ['ngRoute', 'ngResource', 'ngMaterial', 'ngAnimate']);
app.config(function ($locationProvider, $routeProvider, $mdThemingProvider) {
    $locationProvider.html5Mode({
        enabled: true
    });
    $routeProvider
        .when('/template-based', {
            templateUrl: '/views/template-based.html',
            controller: 'templateBasedController'
        })
        .when('/template-free', {
            templateUrl: '/views/template-free.html',
            controller: 'templateFreeController'
        })
        .otherwise(
            {redirectTo: '/'}
        );
    $mdThemingProvider.theme('default')
        .dark();
});

/**
 * Directive model for file upload.
 */
app.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

/**
 * Service for file upload.
 */
app.service('fileUploadService', ['$http', function ($http) {
    this.uploadFileToUrl = function (file, uploadUrl, job) {
        var fd = new FormData();
        fd.append('file', file);
        fd.append('id', job.id);
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
        //     .success(function () {
        //
        // }).error(function () {
        //
        // });
    }
}]);

app.service('jobService', ['$http', function ($http) {
    this.createTemplateBasedJob = function createTemplateBasedJob() {
        return $http({
            method: 'GET',
            url: 'api/create',
            headers: 'Accept:application/json'
        }).then(function (response) {
            console.log("created job: " + response.data.id);
            return angular.fromJson(response.data);
        });
    };
}]);
