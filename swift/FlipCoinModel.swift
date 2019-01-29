//
//  FlipCoinModel.swift
//  Foogle
//
//  Created by dev288 on 22/08/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import UIKit
import GameKit
import ObjectMapper

enum ChooseMealType {
    case EatingOut
    case Cooking
    case IKnowWhatIWant
    case TakeChance
}

class FlipCoinModel: NSObject {
    func generateRandomNumber() -> Int {
        let randomNum = GKRandomSource.sharedRandom().nextInt(upperBound: 2)
        return randomNum
    }
    
    //MARK: API
    func requestListenStatusAPI(data: FlipCoinMap,completion: @escaping (Bool, String, ListenStatusAPIMap?) -> ()) {
        APIManager.ListenStatus(game_id: data.game_id, game_session_id: data.game_session_id).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<ListenStatusAPIMap>().map(JSONObject: response) as ListenStatusAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, apiResponse)
            }
            else if apiResponse.status ==  0 {
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, nil)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
    
    func requestExitGameAPI(data: FlipCoinMap,completion: @escaping (Bool, String, ExitGameAPIMap?) -> ()) {
        APIManager.ExitGame(game_session_id: data.game_session_id).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<ExitGameAPIMap>().map(JSONObject: response) as ExitGameAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, apiResponse)
            }
            else if apiResponse.status ==  0 {
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, nil)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
    
    func requestAddOpponentAPI(data: AddOpponentMap, completion: @escaping (Bool, String, AddOpponentData?) -> ()) {
        APIManager.AddOpponent(participant_id: data.participant_id, game_id: data.game_id).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<AddOpponentAPIMap>().map(JSONObject: response) as AddOpponentAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, apiResponse.data)
            }
            else if apiResponse.status ==  0 {
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, nil)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
    
    func requestSetPreviousAPI(data: SetToPreviousMap,completion: @escaping (Bool, String, SetToPreviousAPIMap?) -> ()) {
        APIManager.SetToPrevious(game_session_id: data.game_session_id, participant_id: data.participant_id, owner_id: data.owner_id).requestURL(success: { (response) in
            
            guard let apiResponse = Mapper<SetToPreviousAPIMap>().map(JSONObject: response) as SetToPreviousAPIMap? else {
                completion(false, MessageCommon.serverError, nil)
                return
            }
            
            if apiResponse.status == 1 {
                completion(true, apiResponse.message == nil ? "" : apiResponse.message, apiResponse)
            }
            else if apiResponse.status ==  0 {
                completion(false, apiResponse.error == nil ? (apiResponse.message == nil ? MessageCommon.serverError : apiResponse.message) : apiResponse.error, nil)
            }
            else {
                completion(false, MessageCommon.serverError, nil)
            }
            
        }) { (failure) in
            completion(false, MessageCommon.serverError, nil)
        }
    }
}
