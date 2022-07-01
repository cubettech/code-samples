import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import 'package:get/get.dart';
import 'package:job_matcher/app/components/general_components.dart';
import 'package:text_scroll/text_scroll.dart';

import '../../../components/custom_icon_button.dart';
import '../../../data/apis/response/notifications_response.dart';
import '../../../helpers/assets_helper.dart';
import '../../../helpers/constants.dart';
import '../../../helpers/translations_helper.dart';
import '../controllers/notifications_controller.dart';

class NotificationsView extends GetView<NotificationsController> {
  const NotificationsView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return controller.obx(
      (state) {
        return notificationView(state[0]);
      },
      onLoading: Scaffold(
          appBar: customAppBarBack(),
          body: const Center(child: CircularProgressIndicator())),
      onEmpty: Container(
        height: Get.height,
        width: Get.width,
        decoration: const BoxDecoration(
          gradient: AppColors.backgroundGreen,
        ),
        child: Scaffold(
          backgroundColor: AppColors.transparentColor,
          appBar: AppBar(
            centerTitle: true,
            leading: CustomIconButton(
                iconPath: AssetsHelper.iconArrow, onPressed: () => Get.back()),
            title: Container(
              // alignment: Alignment.centerLeft,
              padding: const EdgeInsets.only(
                  left: AppDimensions.px20, top: AppDimensions.px15),
              child: Text(
                TranslationsHelper.notifications,
                style:
                    AppFontStyles.style18700.copyWith(color: AppColors.white),
                maxLines: 1,
              ),
            ),
            elevation: 0,
            backgroundColor: AppColors.transparentColor,
          ),
          body: SizedBox(
            height: Get.height,
            width: Get.width,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(TranslationsHelper.noDataToDisplay),
                const SizedBox(height: AppDimensions.px15),
                OutlinedButton(
                  onPressed: () => controller.fetchData(),
                  child: Container(
                    width: Get.width / 5,
                    height: AppDimensions.px30,
                    alignment: Alignment.center,
                    child: Text(
                      TranslationsHelper.fetchData,
                      style: AppFontStyles.style14500,
                    ),
                  ),
                  style: OutlinedButton.styleFrom(
                    backgroundColor: AppColors.secondaryColor,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppDimensions.px13),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
      onError: (_) => Center(
        child: Text(TranslationsHelper.errorLoadingPage),
      ),
    );
  }

  Widget notificationView(List<NotificationFeedData> snapshot) {
    return Container(
      height: Get.height,
      width: Get.width,
      decoration: const BoxDecoration(
        gradient: AppColors.backgroundGreen,
      ),
      child: Scaffold(
          appBar: AppBar(
            centerTitle: true,
            leading: CustomIconButton(
                iconPath: AssetsHelper.iconArrow, onPressed: () => Get.back()),
            title: Container(
              // alignment: Alignment.centerLeft,
              padding: const EdgeInsets.only(
                  left: AppDimensions.px20, top: AppDimensions.px15),
              child: Text(
                TranslationsHelper.notifications,
                style:
                    AppFontStyles.style18700.copyWith(color: AppColors.white),
                maxLines: 1,
              ),
            ),
            elevation: 0,
            backgroundColor: AppColors.transparentColor,
          ),
          backgroundColor: AppColors.transparentColor,
          body: Container(
            padding: const EdgeInsets.symmetric(vertical: AppDimensions.px10),
            child: ListView.separated(
              itemBuilder: (context, index) {
                return notificationItem(snapshot[index], index);
              },
              separatorBuilder: (context, index) {
                return const Divider();
              },
              itemCount: snapshot.length,
            ),
          )),
    );
  }

  Widget notificationItem(NotificationFeedData snapshot, int index) {
    return ListTile(
      onTap: () {},
      title: SizedBox(
        child: Row(
          children: [
            snapshot.userImage == ''
                ? Container(
                    margin: const EdgeInsets.symmetric(
                        horizontal: AppDimensions.px10),
                    height: AppDimensions.px40,
                    width: AppDimensions.px40,
                    decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(AppDimensions.px55),
                        image: const DecorationImage(
                          image: AssetImage(
                            AssetsHelper.imageOnboarding,
                          ),
                          fit: BoxFit.cover,
                        )))
                : CachedNetworkImage(
                    imageUrl: snapshot.userImage,
                    placeholder: (_, __) => Container(
                        height: AppDimensions.px40,
                        width: AppDimensions.px40,
                        decoration: BoxDecoration(
                            borderRadius:
                                BorderRadius.circular(AppDimensions.px55),
                            image: const DecorationImage(
                              image: AssetImage(
                                AssetsHelper.imageOnboarding,
                              ),
                              fit: BoxFit.cover,
                            ))),
                    imageBuilder: (context, imageProvider) => Container(
                      height: AppDimensions.px40,
                      width: AppDimensions.px40,
                      decoration: BoxDecoration(
                          borderRadius:
                              BorderRadius.circular(AppDimensions.px20),
                          image: DecorationImage(
                            image: imageProvider,
                            fit: BoxFit.cover,
                          )),
                    ),
                    errorWidget: (context, url, _) => Container(
                        alignment: Alignment.center,
                        height: AppDimensions.px20,
                        width: AppDimensions.px20,
                        child: const CircularProgressIndicator()),
                    // placeholderFadeInDuration: const Duration(milliseconds: 1000),
                  ),
            const SizedBox(width: AppDimensions.px10),
            Text(
              snapshot.username,
              style: AppFontStyles.style14700,
            ),
            const SizedBox(width: AppDimensions.px10),
            Expanded(
              child: TextScroll(
                snapshot.message,
                style: AppFontStyles.style14400,
                // pauseBetween: Duration(milliseconds: 1000),
                mode: TextScrollMode.endless,
                numberOfReps: 10,
                selectable: false,
                delayBefore: const Duration(seconds: 1),
                velocity: const Velocity(pixelsPerSecond: Offset(30, 0)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
