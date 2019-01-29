//
//  LoginVC.swift
//  Foogle
//
//  Created by dev288 on 25/05/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import UIKit
import FBSDKLoginKit

class LoginVC: BaseVC {
    //MARK: Storyboard connections
    @IBOutlet weak var txtEmailAddress: UITextField!
    @IBOutlet weak var txtPassword: UITextField!
    
    ///custom objects
    private let loginModel = LoginModel()
    
    //MARK: View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: - API
    func loginAPI(data: LoginMap) {
        showLoader(withMessage: "")
        loginModel.requestLoginAPI(data: data) { (status, message, data) in
            self.hideLoader()
            if status {
                if !message.isEmpty {
                    self.view.makeToast(message)
                }
                
                let homeVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
                self.navigationController?.push(viewController: homeVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
            }
            else {
                if data?.is_verified == "0" {
                    self.showAlertWithTitle(title: (data?.title)!, message: message, options: "Ok", completion: { (option) in
                        let verificationVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "VerificationVC") as! VerificationVC
                        self.navigationController?.push(viewController: verificationVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
                    })
                }
                else {
                    self.view.makeToast(message)
                }
            }
        }
    }
    
    func facebookLoginAPI(data: FacebookLoginMap) {
        showLoader(withMessage: "")
        loginModel.requestFacebookLoginAPI(data: data) { (status, message, apiresponse) in
            self.hideLoader()
            if status {
                if !message.isEmpty {
                    self.view.makeToast(message)
                }
                
                let homeVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
                self.navigationController?.push(viewController: homeVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
            }
            else {
                if apiresponse?.is_verified == "0" {
                    self.showAlertWithTitle(title: (apiresponse?.title)!, message: message, options: "Ok", completion: { (option) in
                        let verificationVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "VerificationVC") as! VerificationVC
                        self.navigationController?.push(viewController: verificationVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
                    })
                }
                else if apiresponse?.error == "You're account is not connected to Facebook. Please Sign in with Facebook" {
                    let signUpVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SignUpVC") as! SignUpVC
                    signUpVC.facebookUserData = data
                    signUpVC.signupType = AppConstants.facebookLogin
                    self.navigationController?.push(viewController: signUpVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
                }
                else {
                    self.view.makeToast(message)
                }
            }
        }
    }
    
    //MARK: - Textfield
    override func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == txtEmailAddress && (txtPassword.text?.isEmpty)! {
            txtPassword.becomeFirstResponder()
            return false
        }
        else {
            textField.resignFirstResponder()
            return true
        }
    }
    
    //MARK: - Button Actions
    @IBAction func facebookLoginAction(_ sender: Any) {
        hideKeyboard()
        facebookLoginAction { (status, message, data) in
            if status {
                let loginData: FacebookLoginMap = FacebookLoginMap()
                loginData.access_token = FBSDKAccessToken.current().tokenString
                loginData.account_id = data!["id"] as! String
                loginData.account_type = AppConstants.facebookLogin
                loginData.name = data!["name"] as! String
                loginData.email = data!["email"] as! String
                loginData.device_token = UserDefaults.standard.object(forKey: DefaultsKeys.push_token) as! String
                loginData.device_type = AppConstants.ios
                
                self.facebookLoginAPI(data: loginData)
            }
            else {
                if !message.isEmpty {
                    self.view.makeToast(message)
                }
            }
        }
    }
    
    @IBAction func forgotPasswordAction(_ sender: Any) {
        hideKeyboard()
        
        let forgotPasswordVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "ForgotPasswordVC") as! ForgotPasswordVC
        self.navigationController?.push(viewController: forgotPasswordVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
    }
    
    @IBAction func loginAction(_ sender: Any) {
        hideKeyboard()
        let loginData: LoginMap = LoginMap()
        loginData.emailAddress = txtEmailAddress.text
        loginData.password = txtPassword.text
        loginData.device_token = UserDefaults.standard.object(forKey: DefaultsKeys.push_token) as! String
        loginData.device_type = AppConstants.ios
        
        loginModel.validate(data: loginData) { (status, message, data) in
            if status {
                self.loginAPI(data: data)
            }
            else {
                self.view.makeToast(message)
            }
        }
    }
    
    @IBAction func signUpAction(_ sender: Any) {
        let signUpVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SignUpVC") as! SignUpVC
        signUpVC.signupType = AppConstants.emailLogin
        self.navigationController?.push(viewController: signUpVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
    }
}
