//  
//  LoginViewModel.swift
//  CupidsQuest
//
//  Created by Vivek on 21/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import Foundation
import GoogleSignIn
import FirebaseAuth
import FBSDKLoginKit
import TransitionButton
import SCLAlertView
import AuthenticationServices
import PhoneNumberKit
import FirebaseDatabase
import CryptoKit

class LoginViewModel:LoginViewModelProtocol {

    var serviceProtocol : LoginService?
    weak var targetController : LoginView?
    let appearance = SCLAlertView.SCLAppearance(kCircleIconHeight:60, kTitleFont:Configurations.titleFont!, kTextFont:Configurations.textFont!, kButtonFont:Configurations.buttonFont!, showCloseButton: false,showCircularIcon: true, shouldAutoDismiss: false,circleBackgroundColor: Configurations.themeBackgroundColor)
    required init(targetController: LoginView) {
        self.targetController = targetController
        self.serviceProtocol = LoginService()
    }
    func setViewGradiednt() {
        targetController?.view.setGradient()
        targetController?.checkimage.image=UIImage(named: "checkoff")!
        targetController?.checkimage.isUserInteractionEnabled = true
        let tap = UITapGestureRecognizer(target: targetController, action: #selector(targetController?.handleTap(_:)))
        targetController?.checkimage.addGestureRecognizer(tap)
    }
    func didLoadControls() {
        
        if UserDefaultsStore.userDetail != nil && UserDefaultsStore.userDetail?.loginMode == LoginMode.Email.rawValue {
            guard let emailVerification :EmailVerificationView = self.targetController?.storyboard?.instantiateVC() else {
                return
            }
            self.targetController?.navigationController?.present(emailVerification, animated: true, completion: nil)
            return
        }
        let locale = Locale.current
        let code = (locale as NSLocale).object(forKey: NSLocale.Key.countryCode) as! String?
        targetController?.countryPicker.countryPickerDelegate = targetController
        targetController?.countryPicker.showPhoneNumbers = true
        targetController?.countryPicker.setCountry(code!)
        
        targetController?.countryCode.textAlignment = .center
        targetController?.countryCode.layer.borderWidth = 1.0
        targetController?.countryCode.layer.borderColor = Configurations.themeButtonColorYes.cgColor
        targetController?.countryCode.font = UIFont.systemFont(ofSize: 14)
        targetController?.countryCode.width(30)
        targetController?.countryCode.delegate = targetController
        targetController?.countryCode.inputView = targetController?.countryPicker
        targetController?.countryCode.text = targetController?.countryPicker.currentCountry?.phoneCode
    }
    
    func didSetSocialLogin() {
        GIDSignIn.sharedInstance()?.clientID = Configurations.gClientID
        GIDSignIn.sharedInstance()?.delegate = targetController
        GIDSignIn.sharedInstance()?.presentingViewController = targetController
        
    }
    func googleSignInButtonAction(sender:UIButton) {
        
        GIDSignIn.sharedInstance()?.signIn()
        
    }
    func facebookSignInButtonAction(sender:UIButton) {
    
        self.signInWithFacebook()
    }
    func signInButtonAction(sender:UIButton) {
       
    DispatchQueue.main.async {
        self.loginWithEmailandPhone()
    }
        
    }
    func signInWithFacebook() {
       
        let manager = LoginManager()
        manager.logIn(permissions: Configurations.facebookReadPermissions, from: self.targetController) { (result, error) in
            guard error == nil else {
                self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                self.targetController?.dismissHud()
                return
            }
            if result?.isCancelled != nil && result?.isCancelled == true{
                self.targetController?.dismissHud()
                return
            }
            let facebook = FacebookAuthProvider.credential(withAccessToken: AccessToken.current!.tokenString)
             self.targetController?.showHud()
            Auth.auth().signIn(with: facebook, completion: { (result, error) in
                self.targetController?.dismissHud()
                SocketManagerClass.sharedInstance.connect()
                guard error == nil else {
                    self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                    self.targetController?.dismissHud()
                    return
                }
                let user = LoginModel.init(userID:nil,token:Auth.auth().currentUser?.uid,displayName: nil, email: result?.user.email, phone: nil, phoneverificationKey: nil, emailLink: nil, loginMode: LoginMode.Facebook.rawValue, gender: nil, dateOfBirth: nil, location: nil, latitude: nil, longitude: nil, interests: nil, about: nil,interestedGender:nil, partnerAge: nil, partnerDistance:nil,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken, images: nil)
                self.targetController!.isUserAlreadySignup(completion: { (status, model) in
                    guard status == true else{
                        UserDefaultsStore.userDetail = user
                        UserDefaultsStore.isUserLoggedin = true
                        guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                            return
                        }
                        userRegistartion.isSignedIn = false
                        DispatchQueue.main.async {
                            self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                        }
                        return
                    }
                    
                    UserDefaultsStore.userDetail = model
                    UserDefaultsStore.isUserLoggedin = true
                    guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                        return
                    }
                    userRegistartion.isSignedIn = true
                    DispatchQueue.main.async {
                        self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                    }
                    
                })
                
            })
            
        }
    }
    func appleSignIn(authorization:ASAuthorization) {
        if let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential {
            guard let nonce = self.targetController?.currentNonce else {
            fatalError("Invalid state: A login callback was received, but no login request was sent.")
          }
          guard let appleIDToken = appleIDCredential.identityToken else {
            print("Unable to fetch identity token")
            return
          }
          guard let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
            print("Unable to serialize token string from data: \(appleIDToken.debugDescription)")
            return
          }
            self.targetController?.showHud()
          // Initialize a Firebase credential.
          let credential = OAuthProvider.credential(withProviderID: "apple.com",
                                                    idToken: idTokenString,
                                                    rawNonce: nonce)
          // Sign in with Firebase.
          Auth.auth().signIn(with: credential) { (authResult, error) in
              guard error == nil else {
                  self.targetController?.dismissHud()
                  self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                  return
              }
              
            // User is signed in to Firebase with Apple.
            // ...
              if appleIDCredential.fullName?.givenName != nil {
                  let changeRequest = Auth.auth().currentUser?.createProfileChangeRequest()
                  changeRequest?.displayName = appleIDCredential.fullName?.givenName
                  changeRequest?.commitChanges(completion: { error in
                      print(error?.localizedDescription ?? "")
                  })
              } else {
                  
              }
              SocketManagerClass.sharedInstance.connect()
              let user = LoginModel.init(userID:nil,token:Auth.auth().currentUser?.uid,displayName: Auth.auth().currentUser?.displayName, email: Auth.auth().currentUser?.email, phone: nil, phoneverificationKey: nil, emailLink: nil, loginMode: LoginMode.Apple.rawValue, gender: nil, dateOfBirth: nil, location: nil, latitude: nil, longitude: nil, interests: nil, about: nil,interestedGender:nil, partnerAge: nil, partnerDistance:nil,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken,images: nil)
          
              self.serviceProtocol?.isUserAlreadySignup(controller: self.targetController!, completion: { (status, model) in
                  guard status == true else{
                      UserDefaultsStore.isUserLoggedin = true
                      UserDefaultsStore.userDetail = user
                      guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                          return
                      }
                      userRegistartion.isSignedIn = false
                      DispatchQueue.main.async {
                         self.targetController?.dismissHud()
                          self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                      }
                      
                      return
                  }
                  
                  UserDefaultsStore.userDetail = model
                  guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                      return
                  }
                  userRegistartion.isSignedIn = true
                  DispatchQueue.main.async {
                     
                      self.targetController?.dismissHud()
                      self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                  }
                  
              })
              
          }
        }
    }
    
    func signInWithGoogle(iDToken idToken:String,accessToken:String) {
        print("googgle signin is working")
        self.targetController?.showHud()
        let google = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: accessToken)
        Auth.auth().signIn(with: google) { (result, error) in
            self.targetController?.dismissHud()
            print("socket is connecting")
            SocketManagerClass.sharedInstance.connect()
            guard error == nil else {
                
                self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                 self.targetController?.dismissHud()
                return
            }
            
            let user = LoginModel.init(userID:nil,token:Auth.auth().currentUser?.uid,displayName: nil, email: result?.user.email, phone: nil, phoneverificationKey: nil, emailLink: nil, loginMode: LoginMode.Google.rawValue, gender: nil, dateOfBirth: nil, location: nil, latitude: nil, longitude: nil, interests: nil, about: nil,interestedGender:nil, partnerAge: nil, partnerDistance:nil,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken,images: nil)
            self.targetController!.isUserAlreadySignup(completion: { (status, model) in
                guard status == true else{
                    UserDefaultsStore.userDetail = user
                    UserDefaultsStore.isUserLoggedin = true
                    guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                        return
                    }
                    userRegistartion.isSignedIn = false
                    DispatchQueue.main.async {
                        self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                    }
                    
                    return
                }
                UserDefaultsStore.userDetail = model
                guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                    return
                }
                userRegistartion.isSignedIn = true
                DispatchQueue.main.async {
                    self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                }
                
            })
           
        }
    }
}

extension LoginViewModel {
    func phoneLogin() {
        
        let alertView = SCLAlertView(appearance: appearance)
        let phone = alertView.addTextField("Phone Number")
        phone.leftViewMode = .always
        phone.keyboardType = .numberPad
        phone.leftView = targetController?.countryCode
        alertView.addButton("Send OTP", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            if phone.text == "" || ((self.targetController?.countryCode.text!)!+phone.text!).isValidPhoneNumber() == false {
                phone.isError(baseColor: UIColor.red.cgColor, numberOfShakes: 2, revert: true)
                phone.performErrorValidation(message: "Invalid phone number", textColor: UIColor.red, duration: 1.0)
                return
            } else {
                alertView.hideView()
                self.targetController?.showHud()
                let phoneNumber = (self.targetController?.countryCode.text!)!+phone.text!
                PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil, completion: { (verificationID, error) in
                    if error == nil {
                        let user = LoginModel.init(userID:nil,token:nil,displayName: nil, email: nil, phone: phoneNumber, phoneverificationKey: verificationID, emailLink: nil, loginMode: LoginMode.Phone.rawValue, gender: nil, dateOfBirth: nil, location: nil, latitude: nil, longitude: nil, interests: nil, about: nil,interestedGender:nil, partnerAge: nil,partnerDistance:nil,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken,images: nil)
                    UserDefaultsStore.userDetail = user
                    self.targetController?.dismissHud()
                    self.otpVerification()
                        
                    }
                    else{
                        self.targetController?.dismissHud()
                        self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                        print(error!.localizedDescription)
                    }
                })
            }
        }
        alertView.addButton("Cancel", backgroundColor: Configurations.themeButtonColorNo, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
        }
        alertView.showCustom("Login with Phone Number", subTitle: "Please fill out the details", color: Configurations.themeButtonColorYes, icon: UIImage(named: "popupicon")!, closeButtonTitle: nil, timeout: nil, colorStyle: 0, colorTextButton: 0, circleIconImage: nil, animationStyle: .topToBottom)
    }
    func otpVerification() {
        
        let alertView = SCLAlertView(appearance: appearance)
        let otp = alertView.addTextField("Enter your OTP")
        otp.keyboardType = .numberPad
        otp.textContentType = UITextContentType.oneTimeCode
        alertView.addButton("Verify", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            if otp.text != ""{
            let credential = PhoneAuthProvider.provider().credential(withVerificationID: (UserDefaultsStore.userDetail?.phoneverificationKey!)!, verificationCode: otp.text!)
                Auth.auth().signIn(with: credential, completion: { (result, error) in
                    guard error == nil else {
                        
                        self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                        return
                    }
                    alertView.hideView()
                    SocketManagerClass.sharedInstance.connect()
                    var user = UserDefaultsStore.userDetail
                    user?.phoneverificationKey = nil
                    user?.token = Auth.auth().currentUser?.uid
                    self.targetController!.isUserAlreadySignup(completion: { (status, model) in
                        guard status == true else{
                            UserDefaultsStore.userDetail = user
                            UserDefaultsStore.isUserLoggedin = true
                            guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                                return
                            }
                            userRegistartion.isSignedIn = false
                            DispatchQueue.main.async {
                                self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                            }
                            
                            return
                        }
                        UserDefaultsStore.userDetail = model
                        UserDefaultsStore.isUserLoggedin = true
                        SocketManagerClass.sharedInstance.connect()
                        guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                            return
                        }
                        userRegistartion.isSignedIn = true
                        DispatchQueue.main.async {
                            self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                        }
                        
                    })
                    
                })
            } else {
                otp.isError(baseColor: UIColor.red.cgColor, numberOfShakes: 2, revert: true)
                otp.performErrorValidation(message: "Kindly enter your OTP", textColor: UIColor.red, duration: 1.0)
            }
        }
        alertView.addButton("Resend OTP", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            PhoneAuthProvider.provider().verifyPhoneNumber(UserDefaultsStore.userDetail?.phone ?? "", uiDelegate: nil, completion: { (verificationID, error) in
                if error == nil {
                    var user = UserDefaultsStore.userDetail
                    user?.phoneverificationKey = verificationID
                    UserDefaultsStore.userDetail = user
                    
                }
                else{
                    self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: AlertType.Error)
                    print(error!.localizedDescription)
                }
            })
            
        }
        alertView.addButton("Cancel", backgroundColor:Configurations.themeButtonColorNo, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
        }
        alertView.showCustom("OTP Verification", subTitle: "Please enter the verification code sent to your phone number \(UserDefaultsStore.userDetail?.phone ?? "")", color: Configurations.themeButtonColorYes, icon: UIImage(named: "popupicon")!, closeButtonTitle: nil, timeout: nil, colorStyle: 0, colorTextButton: 0, circleIconImage: nil, animationStyle: .bottomToTop)
        otp.becomeFirstResponder()
        
    }
    func emailLogin() {
        
        let alertView = SCLAlertView(appearance: appearance)
        let email = alertView.addTextField("Email")
        let password = alertView.addTextField("Password")
        email.keyboardType = .emailAddress
        password.isSecureTextEntry = true
        alertView.addButton("Verify", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            if email.text == "" || email.text?.isValidEmail() == false{
                
                email.isError(baseColor: UIColor.red.cgColor, numberOfShakes: 2, revert: true)
                email.performErrorValidation(message: "Invalid Email", textColor: UIColor.red, duration: 1.0)
            }
            else if password.text == "" || password.text!.count < 6 {
                
                password.isError(baseColor: UIColor.red.cgColor, numberOfShakes: 2, revert: true)
                password.performErrorValidation(message: "Password should have atleast 6 characters", textColor: UIColor.red, duration: 1.0)
            }
            else {
                var user = LoginModel.init(userID:nil,token:nil,displayName: nil, email: email.text!, phone: nil, phoneverificationKey: nil, emailLink: nil, loginMode: LoginMode.Email.rawValue, gender: nil, dateOfBirth: nil, location: nil, latitude: nil, longitude: nil, interests: nil, about: nil,interestedGender:nil, partnerAge: nil, partnerDistance:nil,deviceToken:UserDefaultsStore.deviceToken,fcmToken:UserDefaultsStore.fcmToken, images: nil)
                alertView.hideView()
                self.targetController?.showHud()
                Auth.auth().signIn(withEmail: email.text!, password: password.text!) { result, error in
                    guard error == nil else {
                        self.targetController?.dismissHud()
                        self.targetController?.showAlertMessage(message: error!.localizedDescription, alertType: .Error)
                        return
                    }
                    user.token = Auth.auth().currentUser!.uid
                    self.targetController!.isUserAlreadySignup(completion: { (status, model) in
                        self.targetController?.dismissHud()
                        guard status == true else{
                            UserDefaultsStore.userDetail = user
                            UserDefaultsStore.isUserLoggedin = true
                            guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                                return
                            }
                            userRegistartion.isSignedIn = false
                            DispatchQueue.main.async {
                                self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                            }
                            
                            return
                        }
                        UserDefaultsStore.userDetail = model
                        UserDefaultsStore.isUserLoggedin = true
                        SocketManagerClass.sharedInstance.connect()
                        guard let userRegistartion : GetToKnowAboutYouView = UIStoryboard(name: "GetToKnowAboutYouView", bundle: nil).instantiateVC() else {
                            return
                        }
                        userRegistartion.isSignedIn = true
                        DispatchQueue.main.async {
                            self.targetController?.navigationController?.pushViewController(userRegistartion, animated: true)
                        }
                        
                    })
                }
            }
        }
        alertView.addButton("Cancel", backgroundColor:Configurations.themeButtonColorNo, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
        }
        alertView.showCustom("Login with Email", subTitle: "Please fill out the details", color: Configurations.themeButtonColorYes, icon: UIImage(named: "popupicon")!, closeButtonTitle: nil, timeout: nil, colorStyle: 0, colorTextButton: 0, circleIconImage: nil, animationStyle: .bottomToTop)
    }
    func loginWithEmailandPhone() {

        let alertView = SCLAlertView(appearance: appearance)
        alertView.addButton("Email", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
            self.emailLogin()
        }
        alertView.addButton("Phone", backgroundColor: Configurations.themeButtonColorYes, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
            self.phoneLogin()
        }
        alertView.addButton("Cancel", backgroundColor: Configurations.themeButtonColorNo, textColor: UIColor.white, showTimeout: nil) {
            alertView.hideView()
        }
        
        alertView.showCustom("Login with Email or Phone Number", subTitle: "Please choose the following options to continue", color: Configurations.themeButtonColorYes, icon: UIImage(named: "popupicon")!, closeButtonTitle: nil, timeout: nil, colorStyle: 0, colorTextButton: 0, circleIconImage: nil, animationStyle: .topToBottom)
    }
    
    private func randomNonceString(length: Int = 32) -> String {
      precondition(length > 0)
      let charset: Array<Character> =
          Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
      var result = ""
      var remainingLength = length

      while remainingLength > 0 {
        let randoms: [UInt8] = (0 ..< 16).map { _ in
          var random: UInt8 = 0
          let errorCode = SecRandomCopyBytes(kSecRandomDefault, 1, &random)
          if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
          }
          return random
        }

        randoms.forEach { random in
          if remainingLength == 0 {
            return
          }

          if random < charset.count {
            result.append(charset[Int(random)])
            remainingLength -= 1
          }
        }
      }

      return result
    }

       func startSignInWithAppleFlow() {
        print("inside the function")
         let nonce = randomNonceString()
         targetController?.currentNonce = nonce
         let appleIDProvider = ASAuthorizationAppleIDProvider()
         let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]
         request.nonce = sha256(nonce)

         let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self.targetController
        authorizationController.presentationContextProvider = self.targetController
         authorizationController.performRequests()
        print("authorizaton has done")
       }

       private func sha256(_ input: String) -> String {
         let inputData = Data(input.utf8)
         let hashedData = SHA256.hash(data: inputData)
         let hashString = hashedData.compactMap {
           return String(format: "%02x", $0)
         }.joined()

         return hashString
       }
}

