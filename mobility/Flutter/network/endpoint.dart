import '../helpers/constants.dart';
import 'network_adapter.dart';

enum EndPoint {
  notificationFeed,
}

extension URLExtension on EndPoint {
  static final String? _baseUrl = AppConstants.baseURL;
  String get url {
    switch (this) {
      case EndPoint.notificationFeed:
        return _baseUrl! + AppConstants.notificationFeed;
    }
  }
}

extension RequestMode on EndPoint {
  RequestType get requestType {
    RequestType requestType = RequestType.get;
    switch (this) {
      case EndPoint.notificationFeed:
        break;
      default:
        break;
    }
    return requestType;
  }
}

extension Token on EndPoint {
  bool get shouldAddToken {
    var shouldAdd = true;
    switch (this) {
      case EndPoint.notificationFeed:
        shouldAdd = true;
        break;
      default:
        break;
    }
    return shouldAdd;
  }
}
