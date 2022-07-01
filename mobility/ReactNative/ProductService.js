import axios from 'axios';
import {SOMETHING_IS_WRONG} from '../constants/Constants';
import {GET_PRODUCTS} from '../constants/ServerAPIs';
import {configHeader} from '../utils/ServiceManager';

/**
 * Function for product API call
 * @param none
 * @return object response or error of product data
 */
export const getProducts = () => {
  var headers = configHeader();
  return axios
    .get(GET_PRODUCTS, headers)
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
