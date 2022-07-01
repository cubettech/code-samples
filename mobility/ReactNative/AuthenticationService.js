import axios from 'axios';
import {SOMETHING_IS_WRONG} from '../constants/Constants';
import {GET_LOGIN} from '../constants/ServerAPIs';
import {
  configHeaderFormdata,
  configHeaderUrlEncoded,
} from '../utils/ServiceManager';
import qs from 'qs';

/**
 * Function for login API call
 * @param params object request params of login
 * @return object response or error of login
 */
export const getLogin = params => {
  var headers = configHeaderUrlEncoded();

  return axios
    .post(GET_LOGIN, qs.stringify(params), headers)
    .then(response => {
      const data = response.data;
      return data;
    })
    .catch(error => {
      var data = {};
      if (error.response) {
        data.isSuccess = false;
        data.msg =
          error.response.data && error.response.data.error_description
            ? error.response.data.error_description
            : SOMETHING_IS_WRONG;
        data.ErrorType = 'Catch';
      } else {
        data.isSuccess = false;
        data.msg = SOMETHING_IS_WRONG;
        data.ErrorType = 'Catch';
      }
      return data;
    });
};


