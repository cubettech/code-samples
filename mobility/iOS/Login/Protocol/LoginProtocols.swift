//  
//  LoginProtocols.swift
//  CupidsQuest
//
//  Created by Vivek on 21/06/19.
//  Copyright © 2019 Vivek. All rights reserved.
//

import Foundation
import TransitionButton
import AuthenticationServices

protocol LoginServiceProtocol:class {
    func isUserAlreadySignup(controller:UIViewController,completion:@escaping(Bool,LoginModel?)->Void)
}
protocol LoginViewModelProtocol:class {
    init(targetController: LoginView)
    func didSetSocialLogin()
    func signInWithGoogle(iDToken idToken:String,accessToken:String)
    func appleSignIn(authorization:ASAuthorization)
    func googleSignInButtonAction(sender:UIButton)
    func facebookSignInButtonAction(sender:UIButton)
    func signInButtonAction(sender:UIButton)
    func setViewGradiednt()
    func didLoadControls()
}
