//  
//  LoginService.swift
//  CupidsQuest
//
//  Created by Vivek on 21/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import Foundation
import Firebase
import UIKit

class LoginService: LoginServiceProtocol {
    // Call protocol function
    func isUserAlreadySignup(controller:UIViewController,completion:@escaping(Bool,LoginModel?)->Void){
        Themes.initiateThemes()
        APIServiceManager.shared.request(servicename: .checkUserExist) { (data, json, result, error, response) in
            if error == nil{
                let finalResponse = Helper.getErroDetailFromApi(response: response!)
                if finalResponse.0 {
                    let model = controller.newparser(modelToParse: UserRegistrationResponseModel.self, result: data!)
                    let user = LoginModel.init(userID: model!.userDetail?.user, token: Auth.auth().currentUser!.uid, displayName: model?.userDetail?.displayName, email: model?.userDetail?.email, phone: model?.userDetail?.phone, phoneverificationKey: nil, emailLink: nil, loginMode:model?.userDetail?.loginMode, gender: model?.userDetail?.gender, dateOfBirth: model?.userDetail?.dateOfBirth, location: model?.userDetail?.location, latitude: model?.userDetail?.latitude, longitude: model?.userDetail?.longitude, interests: nil, about: model?.userDetail?.about,interestedGender:model?.partnerPreference?.gender, partnerAge: model?.partnerPreference?.age, partnerDistance: model?.partnerPreference?.distance,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken, images: model?.images)
                    UserDefaultsStore.partnerDetail = model?.otherUser
                    UserDefaultsStore.isUserLoggedin = true
                    completion(true,user)
                }else{
                    
                    completion(false,nil)
                }
            }else{
                completion(false,nil)
            }
        }
    }

}
