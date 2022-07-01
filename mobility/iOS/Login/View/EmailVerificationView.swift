//
//  EmailVerificationView.swift
//  CupidsQuest
//
//  Created by Vivek on 28/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import UIKit
import FirebaseAuth

class EmailVerificationView: UIViewController {

    @IBOutlet weak var email: UILabel!
    var content = "We found an account with \(UserDefaultsStore.userDetail?.email ?? ""), an email has been sent. Please check your email in a moment."
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.setGradient()
        let atributedText = NSAttributedString(base: content, keyWords: [UserDefaultsStore.userDetail!.email!], foregroundColor: UIColor.white, font: UIFont(name: "Futura-Medium", size: 20.0)!, hightLightFont: UIFont(name: "Futura-Bold", size: 20.0)!, highlightForeground: UIColor.white, highlighBackground: UIColor.clear)
        self.email.attributedText = atributedText
        // Do any additional setup after loading the view.
    }
    
    @IBAction func unAuthorizeLink(_ sender: UIButton) {
    UserDefaultsStore.clearData()
    self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func resendEmail(_ sender: UIButton) {
        let actionCodeSettings = ActionCodeSettings()
        actionCodeSettings.url = URL(string: "https://cupidsquest-a0a16.firebaseapp.com/?email=\(email.text!)")
        // The sign-in operation has to always be completed in the app.
        actionCodeSettings.dynamicLinkDomain = "cupidsquest-a0a16.firebaseapp.com"
        actionCodeSettings.handleCodeInApp = true
        actionCodeSettings.setIOSBundleID(Bundle.main.bundleIdentifier!)
        Auth.auth().sendSignInLink(toEmail: email.text!, actionCodeSettings: actionCodeSettings, completion: { (error) in
            
        })
    }
    

}
