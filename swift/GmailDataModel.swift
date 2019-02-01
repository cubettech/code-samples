//
//  GmailDataModel.swift
//  EmailClient
//
//  Created by Vivek iLeaf on 12/27/18.
//  Copyright © 2018 Vivek iLeaf. All rights reserved.
//

import Foundation
//MARK:- All Message Mail List with ids

struct MessageList : Codable {
  var messages : [Messages]?
  var nextPageToken: String?
  let resultSizeEstimate : Int?
  
  enum CodingKeys: String, CodingKey {
    
    case messages = "messages"
    case nextPageToken = "nextPageToken"
    case resultSizeEstimate = "resultSizeEstimate"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    messages = try values.decodeIfPresent([Messages].self, forKey: .messages)
    nextPageToken = try values.decodeIfPresent(String.self, forKey: .nextPageToken)
    resultSizeEstimate = try values.decodeIfPresent(Int.self, forKey: .resultSizeEstimate)
  }
}
//MARK:- Each Mail Message Model
struct Messages : Codable {
  let id : String?
  let threadId : String?
  
  enum CodingKeys: String, CodingKey {
    
    case id = "id"
    case threadId = "threadId"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    id = try values.decodeIfPresent(String.self, forKey: .id)
    threadId = try values.decodeIfPresent(String.self, forKey: .threadId)
  }
}
struct MessageData : Codable {
  let id : String?
  let threadId : String?
  let labelIds : [String]?
  let snippet : String?
  let historyId : String?
  let internalDate : String?
  let payload : Payload?
  let sizeEstimate : Int?
  
  enum CodingKeys: String, CodingKey {
    
    case id = "id"
    case threadId = "threadId"
    case labelIds = "labelIds"
    case snippet = "snippet"
    case historyId = "historyId"
    case internalDate = "internalDate"
    case payload = "payload"
    case sizeEstimate = "sizeEstimate"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    id = try values.decodeIfPresent(String.self, forKey: .id)
    threadId = try values.decodeIfPresent(String.self, forKey: .threadId)
    labelIds = try values.decodeIfPresent([String].self, forKey: .labelIds)
    snippet = try values.decodeIfPresent(String.self, forKey: .snippet)
    historyId = try values.decodeIfPresent(String.self, forKey: .historyId)
    internalDate = try values.decodeIfPresent(String.self, forKey: .internalDate)
    payload = try values.decodeIfPresent(Payload.self, forKey: .payload)
    sizeEstimate = try values.decodeIfPresent(Int.self, forKey: .sizeEstimate)
  }
  
}
struct Payload : Codable {
  let partId : String?
  let mimeType : String?
  let filename : String?
  let headers : [Headers]?
  let body : Body?
  let parts : [Parts]?
  
  enum CodingKeys: String, CodingKey {
    
    case partId = "partId"
    case mimeType = "mimeType"
    case filename = "filename"
    case headers = "headers"
    case body = "body"
    case parts = "parts"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    partId = try values.decodeIfPresent(String.self, forKey: .partId)
    mimeType = try values.decodeIfPresent(String.self, forKey: .mimeType)
    filename = try values.decodeIfPresent(String.self, forKey: .filename)
    headers = try values.decodeIfPresent([Headers].self, forKey: .headers)
    body = try values.decodeIfPresent(Body.self, forKey: .body)
    parts = try values.decodeIfPresent([Parts].self, forKey: .parts)
  }
  
}
struct Headers : Codable {
  let name : String?
  let value : String?
  
  enum CodingKeys: String, CodingKey {
    
    case name = "name"
    case value = "value"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    name = try values.decodeIfPresent(String.self, forKey: .name)
    value = try values.decodeIfPresent(String.self, forKey: .value)
  }
  
}
struct Body : Codable {
  let size : Int?
  let data : String?
  let attachmentId:String?
  
  enum CodingKeys: String, CodingKey {
    
    case size = "size"
    case data = "data"
    case attachmentId = "attachmentId"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    size = try values.decodeIfPresent(Int.self, forKey: .size)
    data = try values.decodeIfPresent(String.self, forKey: .data)
    attachmentId = try values.decodeIfPresent(String.self, forKey: .attachmentId)
  }
  
}
struct Parts : Codable {
  let partId : String?
  let mimeType : String?
  let filename : String?
  let headers : [Headers]?
  let body : Body?
  
  enum CodingKeys: String, CodingKey {
    
    case partId = "partId"
    case mimeType = "mimeType"
    case filename = "filename"
    case headers = "headers"
    case body = "body"
  }
  
  init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    partId = try values.decodeIfPresent(String.self, forKey: .partId)
    mimeType = try values.decodeIfPresent(String.self, forKey: .mimeType)
    filename = try values.decodeIfPresent(String.self, forKey: .filename)
    headers = try values.decodeIfPresent([Headers].self, forKey: .headers)
    body = try values.decodeIfPresent(Body.self, forKey: .body)
  }
  
}
