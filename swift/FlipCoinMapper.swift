//
//  FlipCoinMapper.swift
//  Foogle
//
//  Created by dev288 on 22/08/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import Foundation
import ObjectMapper

///map objects from viewcontroller
class FlipCoinMap: Mappable {
    var game_id: Int!
    var game_owner_id: Int!
    var game_session_id: Int!
    
    init() {
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        game_id <- map["game_id"]
        game_owner_id <- map["game_owner_id"]
        game_session_id <- map["game_session_id"]
    }
}

class GameInvitationMap: Mappable {
    var accept_status: Int!
    var game_session_id: Int!
    var game_id: Int!
    
    init() {
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        accept_status <- map["accept_status"]
        game_session_id <- map["game_session_id"]
        game_id <- map["game_id"]
    }
}

class GameInvitationAPIMap: Mappable {
    var status: Int!
    var message: String!
    var error: String!
    var title: String!
    
    init() {
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        status <- map["status"]
        message <- map["message"]
        error <- map["error"]
        title <- map["title"]
    }
}

//MARK: - API
///map objects from api
class ListenStatusAPIMap: Mappable {
    var status: Int!
    var message: String!
    var error: String!
    var title: String!
    var game_owner_id: Int!
    var game_owner_item: String!
    var game_owner_item_url: String!
    var game_owner_name: String!
    var game_owner_option: String!
    var game_session_id: Int!
    var game_status: Int!
    var opponent_status: Int!
    var owner_participant_id: Int!
    var self_status: Int!
    var user_status: Int!
    var opponents: [GameOpponentsMap]!
    
    init() {
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        status <- map["status"]
        message <- map["message"]
        error <- map["error"]
        title <- map["title"]
        game_owner_id <- map["game_owner_id"]
        game_owner_item <- map["game_owner_item"]
        game_owner_item_url <- map["game_owner_item_url"]
        game_owner_name <- map["game_owner_name"]
        game_owner_option <- map["game_owner_option"]
        game_session_id <- map["game_session_id"]
        game_status <- map["game_status"]
        opponent_status <- map["opponent_status"]
        owner_participant_id <- map["owner_participant_id"]
        self_status <- map["self_status"]
        user_status <- map["user_status"]
        opponents <- map["opponents"]
    }
}

class GameOpponentsMap: Mappable {
    var game_opponent_id: Int!
    var game_opponent_item: String!
    var game_opponent_item_url: String!
    var game_opponent_name: String!
    var game_opponent_option: String!
    var opponent_participant_id: Int!
    var user_status: Int!

    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        game_opponent_id <- map["game_opponent_id"]
        game_opponent_item <- map["game_opponent_item"]
        game_opponent_item_url <- map["game_opponent_item_url"]
        game_opponent_name <- map["game_opponent_name"]
        game_opponent_option <- map["game_opponent_option"]
        opponent_participant_id <- map["opponent_participant_id"]
        user_status <- map["user_status"]

    }
}

class ExitGameAPIMap: Mappable {
    var status: Int!
    var message: String!
    var error: String!
    var title: String!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        status <- map["status"]
        message <- map["message"]
        error <- map["error"]
        title <- map["title"]
    }
}

class SetToPreviousAPIMap: Mappable {
    var status: Int!
    var message: String!
    var error: String!
    var title: String!
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        status <- map["status"]
        message <- map["message"]
        error <- map["error"]
        title <- map["title"]
    }
}

class SetToPreviousMap: Mappable {
    var game_session_id: Int!
    var participant_id: Int!
    var owner_id: Int!
    
    init() {
    }
    
    required init?(map: Map){
    }
    
    func mapping(map: Map) {
        game_session_id <- map["game_session_id"]
        participant_id <- map["participant_id"]
        owner_id <- map["owner_id"]
    }
}
