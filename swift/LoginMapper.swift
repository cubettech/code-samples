//
//  LoginMapper.swift
//  Foogle
//
//  Created by dev288 on 27/07/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import Foundation
import ObjectMapper

///map objects from viewcontroller
class LoginMap: Mappable {
    var emailAddress: String!
    var password: String!
    var device_token: String!
    var device_type: String!
    
    init() {
        self.emailAddress = nil
        self.password = nil
        self.device_token = nil
        self.device_type = nil
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        emailAddress <- map["emailAddress"]
        password <- map["password"]
        device_token <- map["device_token"]
        device_type <- map["device_type"]
    }
}

class FacebookLoginMap: Mappable {
    var access_token: String!
    var account_id: String!
    var account_type: String!
    var name: String!
    var email: String!
    var mobile_number: Int!
    var device_token: String!
    var device_type: String!
    
    init() {
        self.access_token = nil
        self.account_id = nil
        self.account_type = nil
        self.name = nil
        self.email = nil
        self.mobile_number = nil
        self.device_token = nil
        self.device_type = nil
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        access_token <- map["access_token"]
        account_id <- map["account_id"]
        account_type <- map["account_type"]
        name <- map["name"]
        email <- map["email"]
        mobile_number <- map["mobile_number"]
        device_token <- map["device_token"]
        device_type <- map["device_type"]
    }
}

//MARK: - API
///map objects from api
class LoginAPIMap: Mappable {
    var status: Int!
    var message: String!
    var error: String!
    var is_verified: String!
    var title: String!
    var data: LoginData!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        status <- map["status"]
        message <- map["message"]
        error <- map["error"]
        is_verified <- map["is_verified"]
        title <- map["title"]
        data <- map["data"]
    }
}

class LoginData: Mappable {
    
    var token : String!
    var user: LoginUser!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        token <- map["token"]
        user <- map["user"]
    }
}

class LoginUser: Mappable {
    
    var id : Int!
    var country_code : String!
    var created_at : String!
    var dob : String!
    var email : String!
    var is_external_login : Int!
    var is_verified : Int!
    var mobile_number : Int!
    var name : String!
    var password_token : String!
    var password_token_expires : String!
    var profile_image : String!
    var updated_at : LoginUserProfile!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        id <- map["id"]
        country_code <- map["country_code"]
        created_at <- map["created_at"]
        dob <- map["dob"]
        email <- map["email"]
        is_external_login <- map["is_external_login"]
        is_verified <- map["is_verified"]
        mobile_number <- map["mobile_number"]
        name <- map["name"]
        password_token <- map["password_token"]
        password_token_expires <- map["password_token_expires"]
        profile_image <- map["profile_image"]
        updated_at <- map["updated_at"]
    }
}

class LoginUserProfile: Mappable {
    
    var id : Int!
    var user_id : Int!
    var first_name : String!
    var last_name : String!
    var dob : String!
    var bio : String!
    var avatar : String!
    var gender : String!
    var created_at : String!
    var updated_at : String!
    var url : String!
    var thumbPath : String!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        id <- map["id"]
        user_id <- map["user_id"]
        first_name <- map["first_name"]
        last_name <- map["last_name"]
        dob <- map["dob"]
        bio <- map["bio"]
        avatar <- map["avatar"]
        gender <- map["gender"]
        created_at <- map["created_at"]
        updated_at <- map["updated_at"]
        url <- map["url"]
        thumbPath <- map["thumbPath"]
    }
}
