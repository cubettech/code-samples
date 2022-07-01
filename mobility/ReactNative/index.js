// General api to access data
import axios from 'axios';
import ApiConstants from './ApiConstants';
import {store} from '../redux/store';
import * as NavigationService from '../navigation/NavigationService';
import {Alert} from 'react-native';

export default function api(route, body = null, method, token = null, baseUrl) {
  // Here, baseUrl is url after extraction
  // store.xxx.baseURL is baseUrl in store after login
  // ApiConstants.BASE_URL is the fallback or default baseUrl
  console.log('tocken', 'Bearer ' + store.getState()?.authReducer?.token);
  console.log('#step6,url check', baseUrl, store.getState()?.authReducer?.baseUrl, ApiConstants.BASE_URL);
  axios.defaults.baseURL = baseUrl ? baseUrl : store.getState()?.authReducer?.baseUrl;
  const client = axios.create({
    // baseURL: store.getState()?.authReducer?.baseUrl || baseUrl,
    headers: {
      Accept: '*/*',
      'Content-Type': 'application/json',
      Authorization: 'Bearer ' + store.getState()?.authReducer?.token,
      deviceType: 'mobile',
    },
  });

  function createAxiosResponseInterceptor() {
    const interceptor = client.interceptors.response.use(
      response => response,
      error => {
        // Reject promise if usual error
        if (__DEV__) {
          console.warn('err1', error.response, 'config', error.config);
        }
        if (error.response === undefined) {
          Alert.alert('Unable to communicate with server', '', [
            {
              text: 'OK',
              onPress: () => {},
            },
          ]);
        }

        /*
         * When response code is 403, try to refresh the token.
         * Eject the interceptor so it doesn't loop in case
         * token refresh causes the 403 response again!
         */
        if (!error) {
          toast.hideAll();
          toast.show('Unable to connect to server', {
            type: 'danger',
            position: 'bottom',
            duration: 5000,
            offset: 30,
            animationType: 'zoom-in',
          });
        }
        const originalRequest = error.config;
        console.log('err-org', originalRequest, originalRequest.url.includes(ApiConstants.RENEW));
        if (error.response.status === 403 && store.getState()?.authReducer?.refresh_token && !originalRequest._retry) {
          originalRequest._retry = true;
          axios.interceptors.response.eject(interceptor);
          return client({
            method: 'POST',
            url: ApiConstants.RENEW,
            data: {refresh_token: store.getState()?.authReducer?.refresh_token},
          })
            .then(response => {
              if (__DEV__) {
                console.log('refresh_token renew success ==>', response);
              }
              if (response) {
                store.dispatch({type: 'RENEW_TOKEN', data: response?.data});
                error.response.config.headers.Authorization = 'Bearer ' + response.data.access_token;
                return axios(error.response.config);
              }
            })
            .catch(error => {
              if (__DEV__) {
                console.log('refresh_token renew error', error);
              }
              if (error.response.status === 403 && originalRequest.url.includes(ApiConstants.RENEW)) {
                store.dispatch({type: 'LOG_OUT'});
                return Promise.reject(error);
              } else {
                toast.hideAll();
                toast.show('Session Expired! Please login again', {
                  type: 'danger',
                  position: 'bottom',
                  duration: 3000,
                  offset: 30,
                  animationType: 'zoom-in',
                });
                //clear the authReducer so that user stays in the sign-in screen
                store.dispatch({type: 'LOG_OUT'});
                return Promise.reject(error);
              }
            })
            .finally(createAxiosResponseInterceptor);
        } else {
          if (error.response.status === 401) {
            NavigationService.reset('Pending');
          } else {
            toast.hideAll();
            toast.show(error?.response?.data?.message || 'Thats an error!', {
              type: 'danger',
              position: 'bottom',
              duration: 4000,
              offset: 30,
              animationType: 'zoom-in',
            });
          }

          return Promise.reject(error);
        }
      },
    );
  }
  createAxiosResponseInterceptor();

  const onSuccess = function (response) {
    if (__DEV__) {
      console.warn('Request Successful!', response);
    }
    return response.data;
  };

  const onError = function (error) {
    //already handled by the interceptor
  };

  return client({
    method,
    url: route,
    data: body || undefined,
  })
    .then(onSuccess)
    .catch(onError);
}
