app.controller('templateBasedController', function ($scope, $http, fileUploadService, jobService) {

    $scope.headingTitle = "Template-based motif detection";
    $scope.job = jobService.createTemplateBasedJob();

    $scope.uploadFile = function () {
        var file = $scope.myFile;
        var uploadUrl = '/api/submit/upload';
        fileUploadService.uploadFileToUrl(file, uploadUrl, $scope.job);
    };

    $scope.submitJob = function (job) {
        $scope.uploadFile();
        console.log(job.id);
        $http.post('/api/submit/template-based', job);
    };
});

app.controller('templateFreeController', function ($scope, $http, fileUploadService, jobService) {

    $scope.headingTitle = "Template-free motif detection";

});