class AppException implements Exception {
  final String? _message;
  final String? _prefix;

  AppException([this._message, this._prefix]);

  @override
  String toString() {
    return "$_prefix$_message";
  }
}

class FetchDataException extends AppException {
  FetchDataException([String? message]) : super(message, "");
}

class BadRequestException extends AppException {
  BadRequestException([String? message])
      : super(message, "Invalid Request\n\n");
}

class UnauthorizedException extends AppException {
  UnauthorizedException([String? message]) : super(message, "Unauthorized\n\n");
}

class InvalidInputException extends AppException {
  InvalidInputException([String? message])
      : super(message, "Invalid Input\n\n");
}

class NotFoundException extends AppException {
  NotFoundException([String? message]) : super(message, "Not Found\n\n");
}

class InternalServerException extends AppException {
  InternalServerException([String? message])
      : super(message, "Internal Server Error\n\n");
}
