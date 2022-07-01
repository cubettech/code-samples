//  
//  LoginView.swift
//  CupidsQuest
//
//  Created by Vivek on 21/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//
import UIKit
import GoogleSignIn
import FBSDKLoginKit
import TransitionButton
import SCLAlertView
import CountryPicker
import Spring
import AWSS3
import CryptoKit
import AuthenticationServices
import FirebaseAuth
import FirebaseDatabase


class LoginView: UIViewController,ImagePickerPresentable {

    @IBOutlet weak var checkimage: UIImageView!
    
    // OUTLETS HERE
    @IBOutlet weak var logoLabel: UIView!
    @IBOutlet weak var loginWithEmailPhone: UIButton!
    @IBOutlet var countryPicker: CountryPicker!
    var currentNonce: String?
    var ischecked:Bool = false
    
    // VARIABLES HERE
    let countryCode = UITextField(frame: CGRect(x: 0, y: 0, width:50, height: 30))
    var viewModel : LoginViewModel?

    override func viewDidLoad() {
        super.viewDidLoad()
        self.viewModel = LoginViewModel(targetController: self)
        self.viewModel?.didSetSocialLogin()
        self.viewModel?.didLoadControls()
        
    }
    override func viewWillAppear(_ animated: Bool) {
        self.viewModel?.setViewGradiednt()
        
    }
    @objc func handleTap(_ sender: UITapGestureRecognizer) {
        print("executing")
        guard let getTag = sender.view?.tag else { return }
       print("getTag == \(getTag)")
        if ischecked{
            checkimage.image = UIImage(named: "checkoff")!
            ischecked = false
        }
        else{
            checkimage.image = UIImage(named: "checkon")!
            ischecked = true
        }
   }
    @IBAction func btnTerms(_ sender: SpringButton) {
        
        guard let partner:TermsAndServiceLogViewController = self.storyboard?.instantiateVC()else{return}
        partner.webUrl = "https://cupidquest.net/services/"
        partner.navigationTitleName = "Terms Of Service"
        self.navigationController?.pushViewController(partner, animated: true)
        
    }
    
    @IBAction func loginWithGoogle(_ sender: SpringButton) {
    
        if ischecked{
        self.viewModel?.googleSignInButtonAction(sender: sender)
        }
        else{
            self.showAlertMessage(message: "Please Accept Terms and Services", alertType: AlertType.Info)
                    }
    }
    @IBAction func loginWithFacebook(_ sender: SpringButton) {
        if ischecked{
        self.viewModel?.facebookSignInButtonAction(sender: sender)
        }
        else{
            self.showAlertMessage(message: "Please Accept Terms and Services", alertType: AlertType.Info)
                    }
    }
    @IBAction func loginWithEmail(_ sender: SpringButton) {
        if ischecked{
        self.viewModel?.emailLogin()
        }
        else{
            self.showAlertMessage(message: "Please Accept Terms and Services", alertType: AlertType.Info)
                    
        }
        
    }
    @IBAction func loginWithPhone(_ sender: SpringButton) {
        if ischecked{
        self.viewModel?.phoneLogin()
        }
        else{
        self.showAlertMessage(message: "Please Accept Terms and Services", alertType: AlertType.Info)
        }
    }
    
    @IBAction func appleSignin(_ sender: SpringButton) {
        if ischecked{
        self.viewModel?.startSignInWithAppleFlow()
        }
        else{
            self.showAlertMessage(message: "Please Accept Terms and Services", alertType: AlertType.Info)
        }
    }

}

extension LoginView:GIDSignInDelegate,CountryPickerDelegate,UITextFieldDelegate {
    
    
    func countryPhoneCodePicker(_ picker: CountryPicker, didSelectCountryWithName name: String, countryCode: String, phoneCode: String, flag: UIImage) {
        self.countryCode.text = phoneCode
    }

    func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
        guard error == nil else {
            self.showAlertMessage(message: error.localizedDescription, alertType: AlertType.Error)
            return
        }
        print("function inside the delagate method")
        self.viewModel?.signInWithGoogle(iDToken: user.authentication.idToken, accessToken: user.authentication.accessToken)
        
    }
}
    extension LoginView:ASAuthorizationControllerDelegate,ASAuthorizationControllerPresentationContextProviding {
        func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
            return view.window!
        }

        
        func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
            print("sucesss")
            self.viewModel?.appleSignIn(authorization: authorization)
        }
        
        func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
            print("failed")
            self.showAlertMessage(message: error.localizedDescription, alertType: AlertType.Error)
        }
    }
