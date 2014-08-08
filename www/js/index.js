'use strict';

var destinationType;
var gImageURI = '';
var gFileSystem = {};
var db;
var lastId = 0;
var tmpImg;
var minMaxScore = 0;
var imgPath = "file:///storage/emulated/0/barones-app/";
var connected = false;

var app = {
    initialize: function() {
        this.bindEvents();
    },
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    onDeviceReady: function() {
        destinationType=navigator.camera.DestinationType;

        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, onRequestFileSystemSuccess, null);

        db = window.openDatabase("baronapp_db", "1.0", "Barones", 100000);
        db.transaction(DAO.populateDB, DAO.errorCB, DAO.successCB);

        //app.receivedEvent('deviceready');
    }
};

var birraApp = angular.module('birraApp', [
    'ngRoute',
    'ngAnimate'
]);

birraApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/home', {
                templateUrl: 'views/home.html',
                controller: 'HomeCtrl'
            }).
            when('/scores', {
                templateUrl: 'views/scores.html',
                controller: 'ScoreCtrl'
            }).
            when('/connect', {
                templateUrl: 'views/connect.html',
                controller: 'ConnectCtrl'
            }).
            when('/test', {
                templateUrl: 'views/test.html',
                controller: 'TestCtrl'
            }).
            when('/camera', {
                templateUrl: 'views/camera.html',
                controller: 'CameraCtrl'
            }).
            when('/template', {
                templateUrl: 'views/template.html'//,
                //controller: 'AnimCtrl'
            }).
            otherwise({
                redirectTo: '/home'
            });
    }]);

birraApp.controller('HomeCtrl', function($scope, $rootScope) {

    if(connected) {
        document.getElementById('test_li').classList.remove('hidden');
    } else {
        document.getElementById('test_li').classList.add('hidden');
    }

});

birraApp.controller('TestCtrl', function($scope, $rootScope) {
    $scope.step = 0;
    document.getElementById('step1').classList.remove('hidden');
    document.getElementById('step2').classList.add('hidden');
    document.getElementById('step3').classList.add('hidden');

    document.getElementById('step3_title_fail').classList.add('hidden');
    document.getElementById('step3_fail').classList.add('hidden');

    document.getElementById('step3_title_success').classList.add('hidden');
    document.getElementById('step3_success').classList.add('hidden');

    $scope.seconds = 4;

    $scope.loopFrame = function() {
        switch($scope.step) {
            case 2:
                clearInterval($scope.loopVal);

                document.getElementById('step2').classList.add('hidden');
                document.getElementById('step3').classList.remove('hidden');

                if($rootScope.maxScore>minMaxScore) {
                    document.getElementById('step3_title_success').classList.remove('hidden');
                    document.getElementById('step3_success').classList.remove('hidden');
                } else {
                    document.getElementById('step3_title_fail').classList.remove('hidden');
                    document.getElementById('step3_fail').classList.remove('hidden');
                }

                break;
            case 1:
                $scope.seconds--;
                if($scope.seconds<=0) {
                    $scope.seconds = 4;
                    $scope.step++;
                }
                break;
            default:
            case 0:
                $scope.seconds--;
                if($scope.seconds<=0) {
                    $scope.seconds = 10;
                    $rootScope.maxScore = 0;

                    document.getElementById('step1').classList.add('hidden');
                    document.getElementById('step2').classList.remove('hidden');

                    $scope.step++;
                }
                break;
        }
    }

    $scope.loopVal = setInterval($scope.loopFrame, 1000);

});

birraApp.controller('ConnectCtrl', function($scope, $rootScope) {
    $rootScope.fields = {btData : 0};
    $scope.listPorts = function() {
        bluetoothSerial.list(
            function(results) {
                $scope.btDevices = results;
                $scope.$apply();
            },
            function(error) {
                alert(JSON.stringify(error));
            }
        );
    };

    $scope.notEnabled = function() {
        connected = false;
        alert("Bluetooth is not enabled.")
    };

    bluetoothSerial.isEnabled(
        $scope.listPorts,
        $scope.notEnabled
    );


    $scope.connectBT = function(address) {
        document.getElementById('connect_div').classList.remove('hidden');
        //BT.connect(address);
        bluetoothSerial.connect(
            address,
            $scope.openPort,
            $scope.showError
        );
    }



    $scope.disconnect = function () {
        connected = false;
        bluetoothSerial.disconnect(
            $scope.closePort,
            $scope.showError
        );
    };

    $scope.openPort = function() {
        connected = true;
        document.getElementById('connect_div').classList.add('hidden');
        bluetoothSerial.subscribe('\n', function (data) {
            $rootScope.fields.btData = data;
            if(data>$rootScope.maxScore) {
                $rootScope.maxScore = data;
            }
            $rootScope.$apply();
        });
        document.location = "#/";
    };

    $scope.closePort = function() {
        connected = false;
        bluetoothSerial.unsubscribe(
            function (data) {
            },
            $scope.showError
        );
    };

    $scope.showError = function(error) {
        connected = false;
        console.log(error);
    };


});

birraApp.controller('CameraCtrl', function($scope, $rootScope) {
    capturePhoto();

    $scope.saveCamera = function() {
        if($rootScope<minMaxScore) {
            minMaxScore = $rootScope.maxScore;
        }
        lastId++;
        var name = document.getElementById("name").value;
        db.transaction(function (tx) {
            tx.executeSql('INSERT INTO TBL_DATA (id, name, date, score) VALUES (' + lastId + ', \'' + name + '\', \'' + getCurrentDate() + '\', ' + $rootScope.maxScore + ') ', [], function (tx, result) {}, function(tx, err) {});
        });
        FileIO.updateCameraImages(tmpImg);
    }
});

birraApp.controller('ScoreCtrl', function($scope, $rootScope) {
    $scope.imgPath = imgPath;
    db.transaction(function (tx) {
        tx.executeSql(
            'SELECT * FROM TBL_DATA ORDER BY score DESC ',
            [],
            function (tx, result) {
                var len = result.rows.length;
                $rootScope.scoreList = [];
                if(len>0) {
                    for(var k = 0; k<len; k++) {
                        $rootScope.scoreList.push({
                            'id' : result.rows.item(k).id,
                            'name' : result.rows.item(k).name,
                            'date' : result.rows.item(k).date,
                            'score' : result.rows.item(k).score,
                            'img' : imgPath + result.rows.item(k).id + '.jpg'
                        });
                    }
                    $rootScope.$apply();
                }
            },
            function(tx, err) {}
        );
    });

});


function capturePhoto() {
    // Take picture using device camera and retrieve image as base64-encoded string
    navigator.camera.getPicture(onPhotoURISuccess, onFail, {
        quality: 50,
        destinationType: destinationType.FILE_URI,
        targetWidth: 600,
        targetHeight: 800
    });
}
function onPhotoURISuccess(imageURI) {
    var cameraImage = document.getElementById('camera_img');
    cameraImage.src = imageURI;

    tmpImg = imageURI;
}

function onFail(message) {
    alert('Fail: ' + message);
}


/**
 * CREATE FOLDER TO STORE IMAGES
 */
function onRequestFileSystemSuccess(fileSystem) {
    gFileSystem = fileSystem;
    var entry=fileSystem.root;
    imgPath = gFileSystem.root.nativeURL + "barones-app/";
    entry.getDirectory("barones-app", {create: true, exclusive: false}, onGetDirectorySuccess, onGetDirectoryFail);
}

function onGetDirectorySuccess(dir) {
}

function onGetDirectoryFail(error) {
}

function getCurrentDate() {
    var newDate = new Date();
    var dateString = newDate.getFullYear() + "-";
    var tmp = (newDate.getMonth() + 1);
    if(tmp.length==1) { tmp = "0" + tmp; };
    dateString += tmp + "-";
    tmp = newDate.getDate();
    if(tmp.length==1) { tmp = "0" + tmp; };
    dateString += tmp + " ";

    tmp = newDate.getHours();
    if(tmp.length==1) { tmp = "0" + tmp; };
    dateString += tmp + ":";
    tmp = newDate.getMinutes();
    if(tmp.length==1) { tmp = "0" + tmp; };
    dateString += tmp + ":";
    tmp = newDate.getSeconds();
    if(tmp.length==1) { tmp = "0" + tmp; };
    dateString += tmp;

    return dateString;
}

var FileIO = {
    gotFS : function(fileSystem) {
        gFileSystem = fileSystem;
    },

    updateCameraImages : function(imageURI) {
        gImageURI = imageURI;
        window.resolveLocalFileSystemURL(imageURI, FileIO.gotImageURI, FileIO.errorHandler);
    },

    gotImageURI : function(fileEntry) {
        var newName = "barones-app/" + lastId + ".jpg";
        fileEntry.moveTo(gFileSystem.root, newName, FileIO.movedImageSuccess, FileIO.errorHandler);
    },

    movedImageSuccess : function(fileEntry) {
        document.location = '#/scores';
    },

    errorHandler : function(e) {
        var msg = '';
        switch (e.code) {
            case FileError.QUOTA_EXCEEDED_ERR:
                msg = 'QUOTA_EXCEEDED_ERR';
                break;
            case FileError.NOT_FOUND_ERR:
                msg = 'NOT_FOUND_ERR';
                break;
            case FileError.SECURITY_ERR:
                msg = 'SECURITY_ERR';
                break;
            case FileError.INVALID_MODIFICATION_ERR:
                msg = 'INVALID_MODIFICATION_ERR';
                break;
            case FileError.INVALID_STATE_ERR:
                msg = 'INVALID_STATE_ERR';
                break;
            default:
                msg = e.code;
                break;
        };
        console.log('Error: ' + msg);
    }
};


var DAO = {
    populateDB : function(tx) {
        tx.executeSql('CREATE TABLE IF NOT EXISTS TBL_DATA (id unique, name, date, score)');
        tx.executeSql('SELECT * FROM TBL_DATA ORDER BY id DESC LIMIT 1', [], DAO.querySuccess, DAO.errorCB);
    },

    errorCB : function(tx, err) {
        //alert("Error processing SQL: "+err.code);
    },

    successCB : function() {
        //alert("success!");
    },

    querySuccess : function (tx, result) {
        tx.executeSql('SELECT * FROM TBL_DATA ORDER BY score ASC LIMIT 1', [], DAO.queryScoreSuccess, DAO.errorCB);
        var len = result.rows.length;
        if(len>0) {
            lastId = result.rows.item(0).id;
        } else {
            lastId = 0;
        }
    },

    queryScoreSuccess : function (tx, result) {
        var len = result.rows.length;
        if(len>0) {
            minMaxScore = result.rows.item(0).score;
        } else {
            minMaxScore = 0;
        }
    }
}
