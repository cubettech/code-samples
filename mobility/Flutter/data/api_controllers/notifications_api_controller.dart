import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../../helpers/base_controller.dart';
import '../../network/endpoint.dart';
import '../../network/network_adapter.dart';

class NotificationApis extends BaseController {
  Future<List<NotificationFeedData>> getNotifications(
      {int limit = 15, int page = 1}) async {
    try {
      NotificationFeedRequest notificationFeedRequest =
          NotificationFeedRequest(limit: limit.toString(), page: page);
      Response response = (await NetworkAdapter.shared.post(
          endPoint: EndPoint.notificationFeed,
          params: notificationFeedRequest.toJson()));
      NotificationsResponse notificationsResponse =
          await compute(NotificationsResponse.parse, (response.data));
      if (kDebugMode) {
        print(notificationsResponse);
      }
      if (notificationsResponse.message == 'Success') {
        return notificationsResponse.data.feed;
      }
    } catch (error) {
      if (kDebugMode) {
        print(error);
      }
    }
    return [];
  }
}
