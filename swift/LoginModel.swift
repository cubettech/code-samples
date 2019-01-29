//
//  LoginModel.swift
//  Foogle
//
//  Created by dev288 on 27/07/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import UIKit
import ObjectMapper

class LoginModel: NSObject {
    //MARK: Validations
    func validate(data: LoginMap, completion: @escaping (Bool, String, LoginMap) -> ()) {
        guard Connectivity.isConnectedToInternet() else {
            completion(false, MessageCommon.connectionError, data)
            return
        }
        guard data.emailAddress != "" else {
            completion(false, MessageLogin.blankEmail, data)
            return
        }
        guard data.emailAddress.isValidEmail() else {
            completion(false, MessageLogin.invalidEmail, data)
            return
        }
        guard data.password != "" else {
            completion(false, MessageLogin.blankPassword, data)
            return
        }
        completion(true, "", data)
    }
    
    //MARK: API
    func requestLoginAPI(data: LoginMap, completion: @escaping (Bool, String, LoginAPIMap?) -> ()) {
        
        APIManager.Login(email: data.emailAddress, password: data.password, device_token: data.device_token, device_type: data.device_type).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<LoginAPIMap>().map(JSONObject: response) as LoginAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                ///save user details to userdefaults after successfull login
                UserManager.sharedInstance.saveUser(userData: apiResponse.data)
                UserManager.sharedInstance.setLoginStatus(status: true)
                
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, nil)
            }
            else if apiResponse.status ==  0 {
                if apiResponse.is_verified == "0" {
                    UserManager.sharedInstance.saveUser(userData: apiResponse.data)
                }
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, apiResponse)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
    
    func requestFacebookLoginAPI(data: FacebookLoginMap, completion: @escaping (Bool, String, LoginAPIMap?) -> ()) {
        
        APIManager.FacebookLogin(access_token: data.access_token, account_id: data.account_id, account_type: data.account_type, device_token: data.device_token, device_type: data.device_type).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<LoginAPIMap>().map(JSONObject: response) as LoginAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                ///save user details to userdefaults after successfull login
                UserManager.sharedInstance.saveUser(userData: apiResponse.data)
                UserManager.sharedInstance.setLoginStatus(status: true)
                
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, nil)
            }
            else if apiResponse.status ==  0 {
                if apiResponse.is_verified == "0" {
                    UserManager.sharedInstance.saveUser(userData: apiResponse.data)
                }
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, apiResponse)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
}
