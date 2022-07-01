//  
//  LoginModel.swift
//  CupidsQuest
//
//  Created by Vivek on 21/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import Foundation
import UIKit

struct LoginModel: Codable,dictify {
    var userID:String?
    var token:String?
    var displayName:String?
    var email : String?
    var phone :String?
    var phoneverificationKey:String?
    var emailLink : String?
    var loginMode : String?
    var gender : Int?
    var dateOfBirth : String?
    var location : String?
    var latitude : String?
    var longitude : String?
    var interests : [String]?
    var about: String?
    var interestedGender:Int?
    var partnerAge : String?
    var partnerDistance: Int?
    var deviceToken : String?
    var fcmToken : String?
    var images : [ProfileImageModel]?
}
struct ProfileImageModel:Codable {
    var id:String?
    var url:String?
    var thumbnailUrl:String?
    var isPrimary:Bool?
    var position:Int?
    var data : Data?
}
struct ProfileImageResponseModel:Codable,dictify{
    var images : [ProfileImageModel]?
}
struct ResponseModel:Codable {
    
    var code:Int?
    var status:String?
    var description:String?
}
struct UserRegistrationResponseModel : Codable {
    let code : Int?
    let status : String?
    let description : String?
    let user : UserData?
    let userDetail : UserDetail?
    let partnerPreference : PartnerPreference?
    var images : [ProfileImageModel]?
    var otherUser : OtherUser?
    
    enum CodingKeys: String, CodingKey {
        
        case code = "code"
        case status = "status"
        case description = "description"
        case user = "user"
        case userDetail = "userDetail"
        case partnerPreference = "partnerPreference"
        case images = "images"
        case otherUser = "otherUser"
    }
    
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        code = try values.decodeIfPresent(Int.self, forKey: .code)
        status = try values.decodeIfPresent(String.self, forKey: .status)
        description = try values.decodeIfPresent(String.self, forKey: .description)
        user = try values.decodeIfPresent(UserData.self, forKey: .user)
        userDetail = try values.decodeIfPresent(UserDetail.self, forKey: .userDetail)
        partnerPreference = try values.decodeIfPresent(PartnerPreference.self, forKey: .partnerPreference)
        images = try values.decodeIfPresent([ProfileImageModel].self, forKey: .images)
        otherUser = try values.decodeIfPresent(OtherUser.self, forKey: .otherUser)
    }
    
}
struct UserData : Codable {
    let isExpired : Bool?
    let createdAt : Int?
    let updatedAt : Int?
    let id : String?
    let token : String?
    let tokenExpired : Bool?
    let role : Int?
    let active : Bool?
    let lastLoggenInAt : Int?
    
    enum CodingKeys: String, CodingKey {
        
        case isExpired = "isExpired"
        case createdAt = "createdAt"
        case updatedAt = "updatedAt"
        case id = "id"
        case token = "token"
        case tokenExpired = "tokenExpired"
        case role = "role"
        case active = "active"
        case lastLoggenInAt = "lastLoggenInAt"
    }
    
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        isExpired = try values.decodeIfPresent(Bool.self, forKey: .isExpired)
        createdAt = try values.decodeIfPresent(Int.self, forKey: .createdAt)
        updatedAt = try values.decodeIfPresent(Int.self, forKey: .updatedAt)
        id = try values.decodeIfPresent(String.self, forKey: .id)
        token = try values.decodeIfPresent(String.self, forKey: .token)
        tokenExpired = try values.decodeIfPresent(Bool.self, forKey: .tokenExpired)
        role = try values.decodeIfPresent(Int.self, forKey: .role)
        active = try values.decodeIfPresent(Bool.self, forKey: .active)
        lastLoggenInAt = try values.decodeIfPresent(Int.self, forKey: .lastLoggenInAt)
    }
    
}
struct UserDetail : Codable {
    let createdAt : Int?
    let updatedAt : Int?
    let id : String?
    let displayName : String?
    let about : String?
    let phone : String?
    let email : String?
    let gender : Int?
    let dateOfBirth : String?
    let location : String?
    let latitude : String?
    let longitude : String?
    let user : String?
    let loginMode:String?
    let deviceToken : String?
    let fcmToken : String?
    let token : String?
    
    enum CodingKeys: String, CodingKey {
        
        case createdAt = "createdAt"
        case updatedAt = "updatedAt"
        case id = "id"
        case displayName = "displayName"
        case about = "about"
        case phone = "phone"
        case email = "email"
        case gender = "gender"
        case dateOfBirth = "dateOfBirth"
        case location = "location"
        case latitude = "latitude"
        case longitude = "longitude"
        case user = "user"
        case loginMode = "loginMode"
        case deviceToken = "deviceToken"
        case fcmToken = "fcmToken"
        case token = "token"
    }
    
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        createdAt = try values.decodeIfPresent(Int.self, forKey: .createdAt)
        updatedAt = try values.decodeIfPresent(Int.self, forKey: .updatedAt)
        id = try values.decodeIfPresent(String.self, forKey: .id)
        displayName = try values.decodeIfPresent(String.self, forKey: .displayName)
        about = try values.decodeIfPresent(String.self, forKey: .about)
        phone = try values.decodeIfPresent(String.self, forKey: .phone)
        email = try values.decodeIfPresent(String.self, forKey: .email)
        gender = try values.decodeIfPresent(Int.self, forKey: .gender)
        dateOfBirth = try values.decodeIfPresent(String.self, forKey: .dateOfBirth)
        location = try values.decodeIfPresent(String.self, forKey: .location)
        latitude = try values.decodeIfPresent(String.self, forKey: .latitude)
        longitude = try values.decodeIfPresent(String.self, forKey: .longitude)
        user = try values.decodeIfPresent(String.self, forKey: .user)
        loginMode = try values.decodeIfPresent(String.self, forKey: .loginMode)
        deviceToken = try values.decodeIfPresent(String.self, forKey: .deviceToken)
        fcmToken = try values.decodeIfPresent(String.self, forKey: .fcmToken)
        token = try values.decodeIfPresent(String.self, forKey: .token)
    }
    
}
struct PartnerPreference : Codable {
    let createdAt : Int?
    let updatedAt : Int?
    let id : String?
    let age : String?
    let distance : Int?
    let gender : Int?
    let user : String?
    
    enum CodingKeys: String, CodingKey {
        
        case createdAt = "createdAt"
        case updatedAt = "updatedAt"
        case id = "id"
        case age = "age"
        case distance = "distance"
        case user = "user"
        case gender = "gender"
    }
    
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        createdAt = try values.decodeIfPresent(Int.self, forKey: .createdAt)
        updatedAt = try values.decodeIfPresent(Int.self, forKey: .updatedAt)
        id = try values.decodeIfPresent(String.self, forKey: .id)
        age = try values.decodeIfPresent(String.self, forKey: .age)
        distance = try values.decodeIfPresent(Int.self, forKey: .distance)
        user = try values.decodeIfPresent(String.self, forKey: .user)
        gender = try values.decodeIfPresent(Int.self, forKey: .gender)
    }
    
}

