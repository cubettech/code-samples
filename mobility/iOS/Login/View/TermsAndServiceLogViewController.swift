//
//  TermsAndServiceLogViewController.swift
//  CupidsQuest
//
//  Created by iLeaf Solutions on 24/11/21.
//  Copyright © 2021 Vivek. All rights reserved.
//

import UIKit
import WebKit

class TermsAndServiceLogViewController: UIViewController {

    @IBOutlet weak var webload: WKWebView!
    var webUrl : String = String()
    var navigationTitleName:String = String()
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = navigationTitleName
        let backButton = UIBarButtonItem(image: UIImage(named: "back"), style: .plain, target: self, action: #selector(backButtonAction))
        self.navigationItem.leftBarButtonItem = backButton
        let web_url = URL(string:webUrl)!
        let web_request = URLRequest(url: web_url)
        webload.navigationDelegate = self
        webload.load(web_request)
    }
    @objc func backButtonAction(){
        self.navigationController?.popViewController(animated: true)
    }
    

    
}
extension TermsAndServiceLogViewController:WKUIDelegate,WKNavigationDelegate {
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy {
        let requestURl:String = navigationAction.request.url!.absoluteString
        if requestURl == webUrl {
            return .allow
        } else {
            return .cancel
        }
        
    }
}
