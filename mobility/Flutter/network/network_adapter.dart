import 'dart:io' show Platform;

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../helpers/constants.dart';
import 'endpoint.dart';
import 'exceptions.dart';

enum RequestType { get, post, patch }

class NetworkAdapter extends BaseController {
  // Singleton
  static final NetworkAdapter shared = NetworkAdapter._privateConstructor();

  NetworkAdapter._privateConstructor();

  static BaseOptions options = BaseOptions(
    connectTimeout: 60000,
    receiveTimeout: 60000,
    contentType: Headers.jsonContentType,
    followRedirects: false,
    validateStatus: (status) {
      return status! < 500;
    },
  );

  static LogInterceptor logInterceptor = LogInterceptor(
      requestHeader: true,
      requestBody: true,
      responseBody: true,
      request: true);

  Future<Response> post(
      {EndPoint? endPoint, Map<String, dynamic>? params}) async {
    Dio dio = Dio(options);
    dio.interceptors.add(logInterceptor);

    if (endPoint?.shouldAddToken == true) {
      options.headers = {
        // "bearer_token": await SecureStorageHelper.shared.getToken()
        'Authorization': 'Bearer ${await database.read('token')}',
      };
    } else {
      options.headers = {};
    }

    if (params != null) {
      if (defaultTargetPlatform == TargetPlatform.iOS ||
          defaultTargetPlatform == TargetPlatform.android) {
        params["platform"] = Platform.isAndroid ? "android" : "ios";
      } else if (defaultTargetPlatform == TargetPlatform.linux ||
          defaultTargetPlatform == TargetPlatform.macOS ||
          defaultTargetPlatform == TargetPlatform.windows) {
        // Some desktop specific code there
      } else {
        // Some web specific code there
      }
    }

    Response response;

    try {
      switch (endPoint!.requestType) {
        case RequestType.get:
          response = await dio.get(endPoint.url, queryParameters: params);
          break;
        case RequestType.post:
          response = await dio.post(endPoint.url, data: params);
          // FormData formData = FormData.fromMap(params!);
          // response = await dio.post(endPoint.url, data: formData);
          break;
        case RequestType.patch:
          response = await dio.patch(endPoint.url, data: params);
      }
    } on DioError catch (error) {
      switch (error.type) {
        case DioErrorType.connectTimeout:
        case DioErrorType.sendTimeout:
        case DioErrorType.receiveTimeout:
          throw FetchDataException('Timeout Error\n\n${error.message}');
        case DioErrorType.response:
          response = error.message as Response; // If response is available.
          break;
        case DioErrorType.cancel:
          throw FetchDataException('Request Cancelled\n\n${error.message}');
        case DioErrorType.other:
          String message = error.message.contains('SocketException')
              ? AppConstants.noNetwork
              : AppConstants.somethingWentWrong;
          throw FetchDataException('$message\n\n${error.message}');
      }
    }

    return checkAndReturnResponse(response);
  }

  dynamic checkAndReturnResponse(Response response) {
    String? description;

    // App specific handling!
    if (response.data is Map) {
      description = response.data.containsKey('description')
          ? response.data['description']
          : null;
    }

    switch (response.statusCode) {
      case 200:
      case 201:
        // Null check for response.data
        if (response.data == null) {
          throw FetchDataException(
              'Returned response data is null : ${response.statusMessage}');
        }

        return response;
      case 400:
        throw BadRequestException(description ?? response.statusMessage);
      case 401:
      case 403:
        throw UnauthorizedException(description ?? response.statusMessage);
      case 404:
        throw NotFoundException(description ?? response.statusMessage);
      case 500:
        throw InternalServerException(description ?? response.statusMessage);
      default:
        throw FetchDataException(
            "Unknown error occurred\n\nerror Code: ${response.statusCode}  error: ${response.statusMessage}");
    }
  }
}
