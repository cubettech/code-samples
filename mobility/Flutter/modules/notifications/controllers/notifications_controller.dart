import 'package:get/get.dart';
import 'package:job_matcher/app/data/api_controllers/notifications_api_controller.dart';

import '../../../helpers/base_controller.dart';

class NotificationsController extends BaseController {
  @override
  void onInit() {
    super.onInit();
    fetchData();
  }

  @override
  void onReady() {
    super.onReady();
  }

  @override
  void onClose() {}

  fetchData() {
    change([], status: RxStatus.loading());
    NotificationApis().getNotifications().then((value) {
      if (value.isEmpty) {
        change([value], status: RxStatus.empty());
      } else {
        change([value], status: RxStatus.success());
      }
    });
  }
}
