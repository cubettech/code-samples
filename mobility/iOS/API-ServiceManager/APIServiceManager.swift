//
//  APIServiceManager.swift
//  CupidsQuest
//
//  Created by Vivek on 10/07/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import Foundation
import UIKit
import FirebaseAuth

class APIServiceManager
{
    private let session = URLSession.shared
    let boundary = "Boundary-\(UUID().uuidString)"
    static let shared = APIServiceManager()
    
    func request(servicename : APIEndpoints,completion:@escaping(Data?,String?,AnyObject?,NSError?,ResponseModel?)->Void)
    {
        let task = session.dataTask(with: servicename.urlRequest, completionHandler: {data, response, error -> Void in
            if error == nil
            {
                let result : AnyObject?
                do
                {
                      result = try JSONSerialization.jsonObject(with: data!, options: JSONSerialization.ReadingOptions.allowFragments) as AnyObject
                     let jsonString = String(data: data!, encoding: String.Encoding.utf8)
                    let responseModel = try JSONDecoder().decode(ResponseModel.self, from: data!)
                  
                    completion(data,jsonString!,result,nil,responseModel)
                }
                catch let catchError as NSError
                {
                    completion(nil,nil,nil,catchError,nil)
                }
            }
            else
            {
                completion(nil,nil,nil,error as NSError?,nil)
            }
        })
        task.resume()
        
    }
    
}
//MARK:- APIManager Protocol
enum APIEndpoints
{
    case checkUserExist
    
}
public protocol APIEndpoint {
    var token:String{get}
    var baseURL: String { get }
    var path: String { get }
    var fullURL: String { get }
    var urlRequest : URLRequest{get}
    var contentType : String{get}
    var body:Data{get}
    
}
//MARK:- Enums for APIManager
enum ContentType:String
{
    case formxurlEncode = "application/x-www-form-urlencoded"
    case multipartFormadata = "multipart/form-data; boundary="
    case json = "appication/json"
}



//MARK:-Extensions For APIManager
public extension UIViewController
{
    func newparser<ModelClass:Codable>(modelToParse:ModelClass.Type,result:Data)->ModelClass?{
        do {
            let jsonDecoder = JSONDecoder()
            let authResponse = try jsonDecoder.decode(modelToParse, from: result)
            return authResponse
        } catch let error {
            
            print(error.localizedDescription)
            return nil
        }
    }
}


extension NSMutableData
{
    func appendStrings(string: String)
    {
        let data = string.data(using: String.Encoding.utf8, allowLossyConversion: false)
        append(data!)
    }
}
extension APIEndpoints
{
    
    private func dataType(dataType : String) -> String
    {
        var fileName : String!
        switch dataType {
        case "image":
            fileName =  self.makeFileNameforimage()
        default:
            fileName = self.makeFileNameforvideo()
        }
        return fileName
    }
    
    /// File name for image
    ///
    /// - Returns: file name for image to upload
    private func makeFileNameforimage()->String
    {
        let dateFormatter : DateFormatter = DateFormatter()
        
        dateFormatter.dateFormat =  "yyMMddHHmmssSSS"
        
        let dateString : NSString = dateFormatter.string(from: Date()) as NSString
        
        let randomValue : Int = Int(arc4random_uniform(3))
        let returnString : String = String(format: "\(dateString)\(randomValue).jpg")
        
        return returnString
    }
    
    /// File name for video
    ///
    /// - Returns: file name for video to upload
    private func makeFileNameforvideo()->String
    {
        let dateFormatter : DateFormatter = DateFormatter()
        
        dateFormatter.dateFormat =  "yyMMddHHmmssSSS"
        
        let dateString : NSString = dateFormatter.string(from: Date()) as NSString
        
        let randomValue : Int = Int(arc4random_uniform(3))
        let returnString : String = String(format: "\(dateString)\(randomValue).mov")
        
        return returnString
    }
    private func createBodyforJson(parameters:NSDictionary)->Data?
    {
        do
        {
            return   try JSONSerialization.data(withJSONObject: parameters, options: JSONSerialization.WritingOptions.prettyPrinted)
        }
        catch _ as NSError
        {
            return nil
        }
    }
    private func createBodyForURlEncoded(parameters:NSDictionary)->Data
    {
        var stngObj = String()
        for obj in parameters.enumerated()
        {
            if stngObj == ""
            {
                stngObj = "\(obj.element.key)=\(obj.element.value)"
            }
            else
            {
                stngObj = "\(stngObj)&\(obj.element.key)=\(obj.element.value)"
            }
        }
        return  stngObj.data(using: String.Encoding.utf8, allowLossyConversion: false)!
    }
    private func createBodyWithParametersandData(parameters: NSDictionary?, filePathKey: [String]?, DataArray: [Data]?,datType: String?, boundary: String) -> Data
    {
        let body = NSMutableData()
        
        if parameters != nil {
            for (key, value) in parameters! {
                body.appendStrings(string: "\r\n--\(boundary)\r\n")
                body.appendStrings(string: "Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n\(value)")
                
            }
        }
        
        if DataArray != nil {
            
            for (idx,Data) in (DataArray?.enumerated())!
            {
                
                body.appendStrings(string: "\r\n--\(boundary)\r\n")
                body.appendStrings(string: "Content-Disposition: form-data; name=\"\(filePathKey![idx])\"; filename=\"\(self.dataType(dataType: datType!))\"\r\n")
                body.appendStrings(string: "Content-Type: application/octet-stream\r\n\r\n")
                body.append(Data as Data)
                
            }
            
        }
        body.appendStrings(string: "\r\n--\(boundary)--\r\n")
        return body as Data
    }
}
extension APIEndpoints:APIEndpoint
{
    var body: Data {
        switch self {
        case .checkUserExist:
             return self.createBodyforJson(parameters: ["fcmToken":UserDefaultsStore.fcmToken ?? "" ,"deviceToken":UserDefaultsStore.deviceToken ?? ""] as NSDictionary)!
        }
    }
    
    
    var contentType: String {
        switch self {
        case .checkUserExist:
            return ContentType.json.rawValue
        }
    }
    
    var token: String {
        return Auth.auth().currentUser!.uid 
        
    }
    
    var urlRequest: URLRequest {
        let urlRequest = NSMutableURLRequest(url: URL(string: fullURL)!)
        switch self
        {
        case .checkUserExist:
            urlRequest.httpMethod = "POST"
            urlRequest.httpBody = body
            urlRequest.setValue(token, forHTTPHeaderField: "token")
            urlRequest.setValue(contentType, forHTTPHeaderField: "Content-Type")
        }
        return urlRequest as URLRequest
    }
    
    var baseURL: String {
        return Configurations.developmentURL
    }
    
    var path: String {
        switch self {
        case .checkUserExist:
            return "endpointname"
        
        }
    }
    
    var fullURL: String {
        return baseURL+path
    }
    
}
