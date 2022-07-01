class NotificationFeedRequest {
  NotificationFeedRequest({
    required this.page,
    required this.limit,
  });
  late final int page;
  late final String limit;

  NotificationFeedRequest.fromJson(Map<String, dynamic> json) {
    page = json['page'];
    limit = json['limit'];
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['page'] = page;
    _data['limit'] = limit;
    return _data;
  }
}
