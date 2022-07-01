import axios from 'axios';
import {SOMETHING_IS_WRONG} from '../constants/Constants';
import {GET_AGRONOMY_LOOKUP} from '../constants/ServerAPIs';
import {configHeader} from '../utils/ServiceManager';

/**
 * Function for agronomy lookup API call
 * @param none
 * @return object response or error of agronomy lookup data
 */
export const getAgronomyLookup = () => {
  var headers = configHeader();
  return axios
    .get(GET_AGRONOMY_LOOKUP, headers)
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
