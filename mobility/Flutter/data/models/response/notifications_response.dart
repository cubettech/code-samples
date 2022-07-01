class NotificationsResponse {
  NotificationsResponse({
    required this.data,
    required this.code,
    required this.status,
    required this.message,
  });
  late final NotificationsResponseData data;
  late final String code;
  late final String status;
  late final String message;

  static NotificationsResponse parse(dynamic json) {
    return NotificationsResponse.fromJson(json);
  }

  NotificationsResponse.fromJson(Map<String, dynamic> json) {
    data = NotificationsResponseData.fromJson(json['data']);
    code = json['code'];
    status = json['status'];
    message = json['message'];
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['data'] = data.toJson();
    _data['code'] = code;
    _data['status'] = status;
    _data['message'] = message;
    return _data;
  }
}

class NotificationsResponseData {
  NotificationsResponseData({
    required this.token,
    required this.feed,
  });
  late final String token;
  late final List<NotificationFeedData> feed;

  NotificationsResponseData.fromJson(Map<String, dynamic> json) {
    token = json['token'];
    feed = List.from(json['feed'])
        .map((e) => NotificationFeedData.fromJson(e))
        .toList();
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['token'] = token;
    _data['feed'] = feed.map((e) => e.toJson()).toList();
    return _data;
  }
}

class NotificationFeedData {
  NotificationFeedData({
    required this.section,
    // required this.id,
    required this.userId,
    required this.username,
    required this.userImage,
    required this.message,
    required this.readStatus,
  });
  late final String section;
  // late final String id;
  late final String userId;
  late final String username;
  late final String userImage;
  late final String message;
  late final String readStatus;

  NotificationFeedData.fromJson(Map<String, dynamic> json) {
    section = json['section'];
    // if (json[id] != null) {
    //   id = json['id'];
    // }
    userId = json['userid'];
    username = json['username'];
    userImage = json['userimage'];
    message = json['message'];
    readStatus = json['readstatus'];
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['section'] = section;
    // _data['id'] = id;
    _data['userid'] = userId;
    _data['username'] = username;
    _data['userimage'] = userImage;
    _data['message'] = message;
    _data['readstatus'] = readStatus;
    return _data;
  }
}
