import axios from 'axios';
import {SOMETHING_IS_WRONG} from '../constants/Constants';
import {
  ADD_CUSTOMER_FARM_FIELD,
  GET_CUSTOMERS,
  GET_USER,
  SEND_SCOUT_EMAIL,
} from '../constants/ServerAPIs';
import {configHeader} from '../utils/ServiceManager';

/**
 * Function for user API call
 * @param none
 * @return object response or error of user data
 */
export const getUser = () => {
  var headers = configHeader();
  return axios
    .get(GET_USER, headers)
    .then(response => {
      const data = response.data;
      data.isSuccess = true;
      return data;
    })
    .catch(error => {
      var data = {};
      data.isSuccess = false;
      data.msg = SOMETHING_IS_WRONG;
      data.ErrorType = 'Catch';
      return data;
    });
};

/**
 * Function for customer API call
 * @param none
 * @return object response or error of customer data
 */
export const getCustomers = () => {
  var headers = configHeader();
  return axios
    .get(GET_CUSTOMERS, headers)
    .then(response => {
      const data = response.data;
      data.isSuccess = true;
      // console.log('Customer==Response=====', JSON.stringify(response.data));
      return data;
    })
    .catch(error => {
      var data = {};
      data.isSuccess = false;
      data.msg = SOMETHING_IS_WRONG;
      data.ErrorType = 'Catch';
      return data;
    });
};

/**
 * Function for add customer farm field API call
 * @param params object of customer farm field details
 * @return object response or error of customer farm field data
 */
export const addCustomerFarmField = params => {
  var headers = configHeader();
  return axios
    .post(ADD_CUSTOMER_FARM_FIELD, params, headers)
    .then(response => {
      const data = response.data;
      data.isSuccess = true;
      return data;
    })
    .catch(error => {
      var data = {};
      if (error.response) {
        data.isSuccess = false;
        data.msg = error.response.data
          ? error.response.data
          : SOMETHING_IS_WRONG;
        data.ErrorType = 'Catch';
        return data;
      } else {
        data.isSuccess = false;
        data.msg = SOMETHING_IS_WRONG;
        data.ErrorType = 'Catch';
        return data;
      }
    });
};

/**
 * Function for send scout mail API call
 * @param params object of scout mail details
 * @return object response or error of scout mail data
 */
export const sendScoutEmail = params => {
  var headers = configHeader();
  return axios
    .post(SEND_SCOUT_EMAIL, params, headers)
    .then(response => {
      var data = {};
      data.finalData = response.data;
      data.isSuccess = true;
      return data;
    })
    .catch(error => {
      var data = {};
      data.isSuccess = false;
      data.msg = SOMETHING_IS_WRONG;
      data.ErrorType = 'Catch';
      return data;
    });
};
