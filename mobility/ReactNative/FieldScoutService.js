import axios from 'axios';
import {SOMETHING_IS_WRONG} from '../constants/Constants';
import {
  ADD_FIELD_SCOUT,
  GET_CROP_PLAN_FOR_SCOUT,
  GET_CUSTOMER_FIELD_SCOUT,
  GET_FIELD_POLYGONS,
  GET_FIELD_SCOUT,
} from '../constants/ServerAPIs';
import {configHeader} from '../utils/ServiceManager';

/**
 * Function for field scout API call
 * @param fieldScoutId Integer fieldScoutId of particular field
 * @return object response or error of field scout data
 */
export const getFieldScout = fieldScoutId => {
  var headers = configHeader();
  var url = GET_FIELD_SCOUT + '/' + fieldScoutId;
  return axios
    .get(url, headers)
    .then(response => {
      const data = response.data;
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
 * Function for customer field scout API call
 * @param customerId Integer customerId of particular customer
 * @return object response or error of customer field scout data
 */
export const getCustomerFieldScout = customerId => {
  var headers = configHeader();
  var url = GET_CUSTOMER_FIELD_SCOUT + '/?CustID=' + customerId;
  return axios
    .get(url, headers)
    .then(response => {
      const data = response.data;
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
 * Function for field polygon API call
 * @param none
 * @return object response or error of field polygon data
 */
export const getFieldPolygons = farmFieldIds => {
  var url = GET_FIELD_POLYGONS;
  if (farmFieldIds.length == 1) {
    url = GET_FIELD_POLYGONS + '?farmfieldid=' + farmFieldIds[0];
  } else {
    var s = '';
    farmFieldIds.map((item, i) => {
      if (i == 0) {
        s += '?farmfieldid=' + item;
      } else {
        s += '&farmfieldid=' + item;
      }
    });
    url = GET_FIELD_POLYGONS + s;
  }
  var headers = configHeader();
  return axios
    .get(url, headers)
    .then(response => {
      const data = response.data;
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
 * Function for add field scout API call
 * @param params object fieldScout detail
 * @return object response or error of add field scout data
 */
export const addFieldScout = params => {
  var headers = configHeader();
  return axios
    .post(ADD_FIELD_SCOUT, params, headers)
    .then(response => {
      var data = {};
      data.finalData = response.data;
      data.isSuccess = true;
      return data;
    })
    .catch(error => {
      var data = {};

      if (error.response) {

        data.isSuccess = false;
        data.serverMessage = error.response.data
          ? error.response.data
          : SOMETHING_IS_WRONG;
        data.msg =
          error.response.data && error.response.data.message
            ? error.response.data.message
            : SOMETHING_IS_WRONG;
        data.detailMsg =
          error.response.data && error.response.data.modelState
            ? error.response.data.modelState
            : '';
        data.ErrorType = 'Catch';
      } else {
        data.isSuccess = false;
        data.msg = SOMETHING_IS_WRONG;
        data.ErrorType = 'Catch';
      }
      return data;
    });
};

/**
 * Function for crop plan for scout API call
 * @param farmFieldId Integer farmFieldId of particular customer's farm
 * @param cropYear Integer current year
 * @return object response or error of crop plan for scout data
 */
export const getCropPlanForScout = (farmFieldId, cropYear) => {
  var headers = configHeader();
  var url =
    GET_CROP_PLAN_FOR_SCOUT +
    '/?FarmFieldID=' +
    farmFieldId +
    '&CropYear=' +
    cropYear;
  return axios
    .get(url, headers)
    .then(response => {
      const data = response.data;
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
