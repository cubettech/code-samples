//
//  MessageListModel.swift
//  EmailClient
//
//  Created by Vivek iLeaf on 12/26/18.
//  Copyright © 2018 Vivek iLeaf. All rights reserved.
//

import Foundation
import UIKit
//MARK:- Singleton to store the message metadata and raw messages
class MailBoxData {
  var messageMetaData : MessageList?
  var messageDetails : [MessageData] = []
  static let shared = MailBoxData()
}
//MARK:- Extension for Message Data for handing the gmail Metadata
extension MessageData
{
  //Decode TEXT encrypted body from Gmail API
  func extractBodyText()->String
  {
    let payload: Payload = self.payload!
    if payload.parts != nil
    {
      if ((payload.parts?.filter{$0.mimeType == "text/plain"})?.count)! > 0
      {
        return ((payload.parts?.filter{$0.mimeType == "text/plain"}.first!.body?.data)!.decryptMessage() ?? "")
      }
      else
      {
        return ((payload.parts?.filter{$0.mimeType == "text/html"}.first!.body?.data)!.decryptMessage() ?? "")
      }
      
    }
    else
    {
      return (payload.body?.data?.decryptMessage() ?? "")!
    }
    
  }
  //Whether Mail has attachment
  func isAttachment()->Bool
  {
    if self.payload?.mimeType == "multipart/related" || self.payload?.mimeType == "multipart/mixed" || self.payload?.mimeType == "multipart/report"
    {
      return true
    }
    else
    {
      return false
    }
  }
  //Decode TEXT encrypted body from Gmail API
  func extractBodyHTML()->String
  {
    let payload: Payload = self.payload!
    if payload.parts != nil
    {
      if ((payload.parts?.filter{$0.mimeType == "text/html"})?.count)! > 0
      {
        return ((payload.parts?.filter{$0.mimeType == "text/html"}.first!.body?.data)!).decryptMessage() ?? ""
      }
      else
      {
        return ((payload.parts?.filter{$0.mimeType == "text/plain "}.first!.body?.data)!).decryptMessage() ?? ""
      }
    }
    else
    {
      return (payload.body?.data)!.decryptMessage() ?? ""
    }
    
  }
  //Extract Email Sender Name from metadata
  func extractSenderName()->String
  {
    return self.payload?.headers?.filter{$0.name == "From"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "From"}.first?.value!)!
  }
  //Extract email Snippet Name from metadata
  func extractSnippet()-> String
  {
    return self.snippet == nil ? "" : self.snippet!
  }
  //Extract email message time from metadata
  func extractMessageTime()->String
  {
    return self.payload?.headers?.filter{$0.name == "Date"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "Date"}.first?.value!)!
  }
  //Extract reciever name from metadata
  func extractRecieversName()->String
  {
    return self.payload?.headers?.filter{$0.name == "To"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "To"}.first?.value!)!
  }
  //Extract subject from metadata
  func extractSubject()->String
  {
    return self.payload?.headers?.filter{$0.name == "Subject"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "Subject"}.first?.value!)!
  }
  //Extract cc recipents from metadata
  func extractCCPerson()->String
  {
    return self.payload?.headers?.filter{$0.name == "Cc"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "Cc"}.first?.value!)!
  }
  //Extract bcc recipents from metadata
  func extractBCCPerson()->String
  {
    return self.payload?.headers?.filter{$0.name == "Bcc"}.first?.value == nil ? "" : (self.payload?.headers?.filter{$0.name == "Bcc"}.first?.value!)!
  }
  //Extract messageTime in string Format
  func extractMessageTimeFromTimeStamp()->String
  {
    let timeInterval : TimeInterval = TimeInterval(self.internalDate!)!
    let messageDate : Date = Date(timeIntervalSince1970: timeInterval/1000)
    return messageDate.dateToString()
  }
}

