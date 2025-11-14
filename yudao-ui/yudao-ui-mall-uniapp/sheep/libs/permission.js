/// null = 未请求，1 = 已允许，0 = 拒绝|受限, 2 = 系统未开启

var isIOS;

function album() {
  var result = 0;
  var PHPhotoLibrary = plus.ios.import('PHPhotoLibrary');
  var authStatus = PHPhotoLibrary.authorizationStatus();
  if (authStatus === 0) {
    result = null;
  } else if (authStatus == 3) {
    result = 1;
  } else {
    result = 0;
  }
  plus.ios.deleteObject(PHPhotoLibrary);
  return result;
}

function camera() {
  var result = 0;
  var AVCaptureDevice = plus.ios.import('AVCaptureDevice');
  var authStatus = AVCaptureDevice.authorizationStatusForMediaType('vide');
  if (authStatus === 0) {
    result = null;
  } else if (authStatus == 3) {
    result = 1;
  } else {
    result = 0;
  }
  plus.ios.deleteObject(AVCaptureDevice);
  return result;
}

function location() {
  var result = 0;
  var cllocationManger = plus.ios.import('CLLocationManager');
  var enable = cllocationManger.locationServicesEnabled();
  var status = cllocationManger.authorizationStatus();
  if (!enable) {
    result = 2;
  } else if (status === 0) {
    result = null;
  } else if (status === 3 || status === 4) {
    result = 1;
  } else {
    result = 0;
  }
  plus.ios.deleteObject(cllocationManger);
  return result;
}

function push() {
  var result = 0;
  var UIApplication = plus.ios.import('UIApplication');
  var app = UIApplication.sharedApplication();
  var enabledTypes = 0;
  if (app.currentUserNotificationSettings) {
    var settings = app.currentUserNotificationSettings();
    enabledTypes = settings.plusGetAttribute('types');
    if (enabledTypes == 0) {
      result = 0;
      console.log('推送权限没有开启');
    } else {
      result = 1;
      console.log('已经开启推送功能!');
    }
    plus.ios.deleteObject(settings);
  } else {
    enabledTypes = app.enabledRemoteNotificationTypes();
    if (enabledTypes == 0) {
      result = 3;
      console.log('推送权限没有开启!');
    } else {
      result = 4;
      console.log('已经开启推送功能!');
    }
  }
  plus.ios.deleteObject(app);
  plus.ios.deleteObject(UIApplication);
  return result;
}

function contact() {
  var result = 0;
  var CNContactStore = plus.ios.import('CNContactStore');
  var cnAuthStatus = CNContactStore.authorizationStatusForEntityType(0);
  if (cnAuthStatus === 0) {
    result = null;
  } else if (cnAuthStatus == 3) {
    result = 1;
  } else {
    result = 0;
  }
  plus.ios.deleteObject(CNContactStore);
  return result;
}

function record() {
  var result = null;
  var avaudiosession = plus.ios.import('AVAudioSession');
  var avaudio = avaudiosession.sharedInstance();
  var status = avaudio.recordPermission();
  console.log('permissionStatus:' + status);
  if (status === 1970168948) {
    result = null;
  } else if (status === 1735552628) {
    result = 1;
  } else {
    result = 0;
  }
  plus.ios.deleteObject(avaudiosession);
  return result;
}

function calendar() {
  var result = null;
  var EKEventStore = plus.ios.import('EKEventStore');
  var ekAuthStatus = EKEventStore.authorizationStatusForEntityType(0);
  if (ekAuthStatus == 3) {
    result = 1;
    console.log('日历权限已经开启');
  } else {
    console.log('日历权限没有开启');
  }
  plus.ios.deleteObject(EKEventStore);
  return result;
}

function memo() {
  var result = null;
  var EKEventStore = plus.ios.import('EKEventStore');
  var ekAuthStatus = EKEventStore.authorizationStatusForEntityType(1);
  if (ekAuthStatus == 3) {
    result = 1;
    console.log('备忘录权限已经开启');
  } else {
    console.log('备忘录权限没有开启');
  }
  plus.ios.deleteObject(EKEventStore);
  return result;
}

function requestIOS(permissionID) {
  return new Promise((resolve, reject) => {
    switch (permissionID) {
      case 'push':
        resolve(push());
        break;
      case 'location':
        resolve(location());
        break;
      case 'record':
        resolve(record());
        break;
      case 'camera':
        resolve(camera());
        break;
      case 'album':
        resolve(album());
        break;
      case 'contact':
        resolve(contact());
        break;
      case 'calendar':
        resolve(calendar());
        break;
      case 'memo':
        resolve(memo());
        break;
      default:
        resolve(0);
        break;
    }
  });
}

function requestAndroid(permissionID) {
  return new Promise((resolve, reject) => {
    plus.android.requestPermissions(
      [permissionID],
      function (resultObj) {
        var result = 0;
        for (var i = 0; i < resultObj.granted.length; i++) {
          var grantedPermission = resultObj.granted[i];
          console.log('已获取的权限：' + grantedPermission);
          result = 1;
        }
        for (var i = 0; i < resultObj.deniedPresent.length; i++) {
          var deniedPresentPermission = resultObj.deniedPresent[i];
          console.log('拒绝本次申请的权限：' + deniedPresentPermission);
          result = 0;
        }
        for (var i = 0; i < resultObj.deniedAlways.length; i++) {
          var deniedAlwaysPermission = resultObj.deniedAlways[i];
          console.log('永久拒绝申请的权限：' + deniedAlwaysPermission);
          result = -1;
        }
        resolve(result);
      },
      function (error) {
        console.log('result error: ' + error.message);
        resolve({
          code: error.code,
          message: error.message,
        });
      },
    );
  });
}

function gotoAppPermissionSetting() {
  if (permission.isIOS) {
    var UIApplication = plus.ios.import('UIApplication');
    var application2 = UIApplication.sharedApplication();
    var NSURL2 = plus.ios.import('NSURL');
    var setting2 = NSURL2.URLWithString('app-settings:');
    application2.openURL(setting2);
    plus.ios.deleteObject(setting2);
    plus.ios.deleteObject(NSURL2);
    plus.ios.deleteObject(application2);
  } else {
    var Intent = plus.android.importClass('android.content.Intent');
    var Settings = plus.android.importClass('android.provider.Settings');
    var Uri = plus.android.importClass('android.net.Uri');
    var mainActivity = plus.android.runtimeMainActivity();
    var intent = new Intent();
    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    var uri = Uri.fromParts('package', mainActivity.getPackageName(), null);
    intent.setData(uri);
    mainActivity.startActivity(intent);
  }
}

const permission = {
  get isIOS() {
    return typeof isIOS === 'boolean' ? isIOS : (isIOS = uni.getDeviceInfo().platform === 'ios');
  },
  requestIOS: requestIOS,
  requestAndroid: requestAndroid,
  gotoAppSetting: gotoAppPermissionSetting,
};

export default permission;
