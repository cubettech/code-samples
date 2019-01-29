//
//  FlipCoinVC.swift
//  Foogle
//
//  Created by dev288 on 20/08/18.
//  Copyright © 2018 dev288. All rights reserved.
//

import UIKit
import SideMenu
import QuartzCore

let kMaxRepeats = 1
let kAnimationDuration: Double = 0.3
let kMenuAction = "menu_action"
let kBackAction = "back_action"

enum SelectionProgress {
    case NotStarted
    case OpponentAdded
    case ChooseMealTapped
    case ChooseMeal_Step1
    case MealSelected
    case GameStarted
}

class FlipCoinVC: BaseVC {
    //MARK: Storyboard connections
    @IBOutlet weak var btnAddOpponent: UIButton!
    @IBOutlet weak var btnChooseMeal: UIButton!
    @IBOutlet weak var btnFlipCoin: UIButton!
    @IBOutlet weak var viewCoinBase: UIView!
    @IBOutlet weak var viewCoinBg: UIView!
    @IBOutlet weak var lblFlipResult: UILabel!
    @IBOutlet weak var btnYou: UIButton!
    @IBOutlet weak var lblYouMeal: UILabel!
    @IBOutlet weak var btnOpponent: UIButton!
    @IBOutlet weak var lblOpponentName: UILabel!
    @IBOutlet weak var lblOpponentMeal: UILabel!
    @IBOutlet weak var btnSkip: UIButton!
    @IBOutlet weak var btnChooseMeal1: HBSegmentedControl!
    @IBOutlet weak var btnChooseMeal2: HBSegmentedControl!
    @IBOutlet weak var viewAddOpponentIndicator: UIView!
    @IBOutlet weak var viewMealIndicator: UIView!
    @IBOutlet weak var viewFlipCoinIndicator: UIView!
    
    ///animation of coin
    private var repeatCount = 0
    private var animationDuration = kAnimationDuration
    private var maxReps = kMaxRepeats
    
    ///custom objects
    private let flipCoinModel = FlipCoinModel()
    var userStatus : UserStatus = .Game_Not_Started
    var meal1Type: ChooseMealType = .EatingOut
    var meal2Type: ChooseMealType = .IKnowWhatIWant
    var gameStatus: ListenStatusAPIMap = ListenStatusAPIMap()
    var opponentUser: AddOpponentData = AddOpponentData()
    var winnerUserForOpponent: WinnerMap = WinnerMap()
    
    //MARK: View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
        
        userStatus = .Game_Not_Started
        updateViews()
        
        btnChooseMeal1.items = ["I'M EATING OUT", "I'M COOKING"]
        btnChooseMeal2.items = ["I KNOW WHAT I WANT", "TAKE A CHANCE"]
        
        let layer = viewCoinBg.layer
        layer.contents = #imageLiteral(resourceName: "coin").cgImage
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        getGameStatusAPI()
        
        NotificationCenter.default.addObserver(self, selector: #selector(self.notificationReceived(notification:)), name: NSNotification.Name(rawValue: AppConstants.pushRefresh), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.gameCompletion(notification:)), name: NSNotification.Name(rawValue: AppConstants.pushGameCompletion), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: AppConstants.pushRefresh), object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: AppConstants.pushGameCompletion), object: nil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: - API
    func getGameStatusAPI() {
        showLoader(withMessage: "")
        let flipCoinMap: FlipCoinMap = FlipCoinMap()
        flipCoinMap.game_session_id = opponentUser.game_session_id
        flipCoinMap.game_id = AppConstants.gameIdRockOff
        
        flipCoinModel.requestListenStatusAPI(data: flipCoinMap) { (status, message, data) in
            self.hideLoader()
            if status {
                self.gameStatus = data!
                
                if data?.opponent_status == 1 {
                    ///user is opponent
                    switch self.getUserStatus(status: (data?.opponents[0].user_status)!) {
                    case .Game_Not_Started:
                        self.userStatus = .User_Added_Opponent
                        
                    case .User_Added_Opponent:
                        self.userStatus = .User_Added_Opponent
                        
                    case .Meal_Selected:
                        self.userStatus = .Meal_Selected
                        
                    default:
                        self.userStatus = .Game_Not_Started
                    }
                }
                else {
                    ///user is opponent
                    switch self.getUserStatus(status: (data?.user_status)!) {
                    case .Game_Not_Started:
                        self.userStatus = .User_Added_Opponent
                        
                    case .User_Added_Opponent:
                        self.userStatus = .User_Added_Opponent
                        
                    case .Meal_Selected:
                        self.userStatus = .Meal_Selected
                        
                    default:
                        self.userStatus = .Game_Not_Started
                    }
                }
                
                self.updateViews()
            }
            else {
                self.gameStatus = ListenStatusAPIMap()
                self.userStatus = .Game_Not_Started
                self.updateViews()
            }
        }
    }
    
    func exitGameAPI(aAction: String) {
        showLoader(withMessage: "")
        let flipCoinMap: FlipCoinMap = FlipCoinMap()
        flipCoinMap.game_session_id = opponentUser.game_session_id
        
        flipCoinModel.requestExitGameAPI(data: flipCoinMap) { (status, message, data) in
            self.hideLoader()
            if status {
                if aAction == kBackAction {
                    if self.gameStatus.self_status == 1 {
                        self.getGameStatusAPI()
                    }
                    else {
                        self.backAction()
                    }
                }
                else if aAction == kMenuAction {
                    self.userStatus = .Game_Not_Started
                    self.updateViews()
                    self.menuAction()
                }
            }
            else {
                self.view.makeToast(message)
            }
        }
    }
    
    func addOpponentAPI() {
        showLoader(withMessage: "")
        let addOpponentMap: AddOpponentMap = AddOpponentMap()
        addOpponentMap.participant_id = "\(UserManager.sharedInstance.currentUser?.user.id! ?? 0)"
        addOpponentMap.game_id = AppConstants.gameIdFlipCoin
        
        flipCoinModel.requestAddOpponentAPI(data: addOpponentMap) { (status, message, data) in
            self.hideLoader()
            if status {
                self.opponentUser = data!
                self.getGameStatusAPI()
            }
            else {
                self.view.makeToast(message)
            }
        }
    }
    
    func setToBackAPI() {
        showLoader(withMessage: "")
        let setToPreviousMap: SetToPreviousMap = SetToPreviousMap()
        setToPreviousMap.game_session_id = opponentUser.game_session_id
        setToPreviousMap.participant_id =  gameStatus.opponent_status == 1 ? gameStatus.opponents[0].game_opponent_id
            : gameStatus.game_owner_id
        setToPreviousMap.owner_id = gameStatus.opponent_status == 1 ? 0 : 1
        
        flipCoinModel.requestSetPreviousAPI(data: setToPreviousMap) { (status, message, data) in
            self.hideLoader()
            if status {
                self.getGameStatusAPI()
            }
            else {
                self.view.makeToast(message)
            }
        }
    }
    
    //MARK: - Other Methods
    @objc func notificationReceived(notification: NSNotification){
        getGameStatusAPI()
    }
    
    @objc func gameCompletion(notification: NSNotification){
        guard let game_session_id = notification.userInfo!["game_session_id"],
            let winner_id = notification.userInfo!["winner_id"],
            let winner_name = notification.userInfo!["winner_name"],
            let item_name = notification.userInfo!["item_name"],
            let restaurant_url = notification.userInfo!["restaurant_url"]
            else {
                return
        }
        
        winnerUserForOpponent.game_session_id = Int(game_session_id as! String)
        winnerUserForOpponent.user_won_id = Int(winner_id as! String)
        winnerUserForOpponent.winner_name = winner_name as! String
        winnerUserForOpponent.winner_meal_item = item_name as! String
        winnerUserForOpponent.restaurant_url = restaurant_url as! String
        
        if winnerUserForOpponent.user_won_id == UserManager.sharedInstance.currentUser?.user.id {
            animateCoin(coinFace: 1)
        }
        else {
            animateCoin(coinFace: 2)
        }
    }
    
    //MARK: - Button Actions
    @IBAction func backAction(_ sender: Any) {
        if gameStatus.opponent_status == 1 {
            if userStatus == .Meal_Selected {
                self.setToBackAPI()
            }
            else {
                self.view.makeToast(MessageFlipCoin.gameInProgress)
            }
        }
        else {
            if userStatus == .Meal_Selected {
                self.setToBackAPI()
            }
            else if userStatus != .Game_Not_Started {
                self.showAlertWithTitle(title: "", message: MessageFlipCoin.endGame, options: "Yes", "No") { (option) in
                    switch option {
                    case 0:
                        self.exitGameAPI(aAction: kBackAction)
                        break
                        
                    case 1:
                        break
                        
                    default:
                        self.backAction()
                    }
                }
            }
            else {
                let homeVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "HomeVC") as! HomeVC
                self.navigationController?.push(viewController: homeVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
            }
        }
    }
    
    @IBAction func menuAction(_ sender: Any) {
        if gameStatus.opponent_status == 1 {
            self.view.makeToast(MessageFlipCoin.gameInProgress)
        }
        else if userStatus != .Game_Not_Started {
            self.showAlertWithTitle(title: "", message: MessageFlipCoin.endGame, options: "Yes", "No") { (option) in
                switch option {
                case 0:
                    self.exitGameAPI(aAction: kMenuAction)
                    break
                    
                case 1:
                    break
                    
                default:
                    self.menuAction()
                }
            }
        }
        else {
            menuAction()
        }
    }
    
    @IBAction func addOpponentAction(_ sender: Any) {
        let addOpponentVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "AddOpponentVC") as! AddOpponentVC
        addOpponentVC.addOpponentDelegate = self
        addOpponentVC.gameType = GameType.FlipCoin.rawValue
        self.navigationController?.push(viewController: addOpponentVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
    }
    
    @IBAction func skipAction(_ sender: Any) {
        addOpponentAPI()
    }
    
    @IBAction func chooseMealAction(_ sender: Any) {
        userStatus = .Choose_Meal_Tapped
        updateViews()
    }
    
    @IBAction func chooseMeal1Action(_ sender: Any) {
        switch btnChooseMeal1.selectedIndex {
        case 0:
            meal1Type = .EatingOut
            break
            
        case 1:
            let recipeVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "MenuSelectionVC") as! MenuSelectionVC
            recipeVC.game_session_id = opponentUser.game_session_id
            recipeVC.participant_id = gameStatus.opponent_status == 1 ? opponentUser.opponent_participant_id : opponentUser.owner_participant_id
            recipeVC.opponent_participant_id = opponentUser.opponent_participant_id
            recipeVC.self_status = gameStatus.self_status
            recipeVC.gameType = GameType.FlipCoin.rawValue
            
            self.navigationController?.push(viewController: recipeVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
            meal1Type = .Cooking
            
            return
            
        default:
            break
        }
        
        userStatus = .Choose_Meal_1_Tapped
        updateViews()
    }
    
    @IBAction func chooseMeal2Action(_ sender: Any) {
        switch btnChooseMeal2.selectedIndex {
        case 0:
            meal2Type = .IKnowWhatIWant
            break
            
        case 1:
            meal2Type = .TakeChance
            break
            
        default:
            break
        }
        
        if meal1Type == .EatingOut && meal2Type == .IKnowWhatIWant {
            let mealSelectionVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "MealSelectionVC") as! MealSelectionVC
            mealSelectionVC.game_session_id = opponentUser.game_session_id
            mealSelectionVC.participant_id = gameStatus.opponent_status == 1 ? opponentUser.opponent_participant_id : opponentUser.owner_participant_id
            mealSelectionVC.opponent_participant_id = opponentUser.opponent_participant_id
            mealSelectionVC.self_status = gameStatus.self_status
            mealSelectionVC.gameType = GameType.FlipCoin.rawValue
            self.navigationController?.push(viewController: mealSelectionVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
        }
        else if meal1Type == .EatingOut && meal2Type == .TakeChance {
            let questionsVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "QuestionsVC") as! QuestionsVC
            questionsVC.game_session_id = opponentUser.game_session_id
            questionsVC.participant_id = gameStatus.opponent_status == 1 ? opponentUser.opponent_participant_id : opponentUser.owner_participant_id
            questionsVC.opponent_participant_id = opponentUser.opponent_participant_id
            questionsVC.self_status = gameStatus.self_status
            questionsVC.gameType = GameType.FlipCoin.rawValue
            self.navigationController?.push(viewController: questionsVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
        }
    }
    
    @IBAction func flipCoinAction(_ sender: Any) {
        showLoader(withMessage: "")
        let flipCoinMap: FlipCoinMap = FlipCoinMap()
        flipCoinMap.game_session_id = opponentUser.game_session_id
        flipCoinMap.game_id = AppConstants.gameIdFlipCoin
        
        flipCoinModel.requestListenStatusAPI(data: flipCoinMap) { (status, message, data) in
            self.hideLoader()
            if status {
                self.gameStatus = data!
                self.updateViews()
                
                switch self.getUserStatus(status: (data?.opponents[0].user_status)!) {
                case .User_Added_Opponent, .Game_Not_Started:
                    self.view.makeToast(MessageFlipCoin.waitingForOpponentMealSelection)
                
                case .Meal_Selected:
                    self.repeatCount = 0
                    self.viewCoinBg.layer.removeAllAnimations()
                    self.viewCoinBg.layer.contents = #imageLiteral(resourceName: "coin").cgImage
                    self.animateCoin(coinFace: self.flipCoinModel.generateRandomNumber())
                    
                    break
                    
                default:
                    break
                }
            }
            else {
                
            }
            
            self.setStatusIndicator()
        }
    }
}

extension FlipCoinVC: AddOpponentDelegate {
    func opponendAdded(opponent: AddOpponentData) {
        opponentUser = opponent
    }
}

extension FlipCoinVC {
    func updateViews() {
        ///update the view based on the current available details
        lblYouMeal.text = " "
        lblOpponentMeal.text = " "
        lblOpponentName.text = " "
        btnSkip.isHidden = true
        
        btnYou.setTitle(String((UserManager.sharedInstance.currentUser?.user.name)!).getInitials().uppercased(), for: .normal)
        btnYou.borderColor = AppColors.themeRed
        btnYou.backgroundColor = .white
        btnYou.setTitleColor(AppColors.themeRed, for: .normal)
        
        if gameStatus.opponents != nil {
            opponentUser.game_owner_id = gameStatus.game_owner_id
            opponentUser.game_session_id = gameStatus.game_session_id
            opponentUser.opponent_participant_id = gameStatus.opponents[0].opponent_participant_id
            opponentUser.owner_participant_id = gameStatus.owner_participant_id
            opponentUser.self_status = gameStatus.self_status
        }
        
        if gameStatus.opponent_status == 1 {
            ///user is opponent
            
            self.btnAddOpponent.isHidden = true

            if gameStatus.opponents[0].game_opponent_item != nil {
                lblYouMeal.attributedText = gameStatus.opponents[0].game_opponent_item.getUnderLineAttributedText()
            }
            
            if gameStatus.opponents != nil {
                if gameStatus.game_owner_name != nil {
                    btnOpponent.setTitle(String(gameStatus.game_owner_name!).getInitials().uppercased(), for: .normal)
                    btnOpponent.borderColor = AppColors.themeRed
                    btnOpponent.backgroundColor = .white
                    btnOpponent.setTitleColor(AppColors.themeRed, for: .normal)
                    lblOpponentName.text = gameStatus.self_status == 1 ? "You" : gameStatus.game_owner_name
                }
                
                if gameStatus.game_owner_item != nil {
                    lblOpponentMeal.attributedText = gameStatus.game_owner_item.getUnderLineAttributedText()
                }
            }
        }
        else {
            ///user initiated the game
            
            self.btnAddOpponent.isHidden = false
            
            if gameStatus.game_owner_item != nil {
                lblYouMeal.attributedText = gameStatus.game_owner_item.getUnderLineAttributedText()
            }
            
            if gameStatus.opponents != nil {
                if gameStatus.opponents[0].game_opponent_name != nil {
                    if gameStatus.opponents[0].game_opponent_name != "" {
                        btnOpponent.setTitle(String(gameStatus.opponents[0].game_opponent_name!).getInitials().uppercased(), for: .normal)
                        btnOpponent.borderColor = AppColors.themeRed
                        btnOpponent.backgroundColor = .white
                        btnOpponent.setTitleColor(AppColors.themeRed, for: .normal)
                        lblOpponentName.text = gameStatus.self_status == 1 ? "You" : gameStatus.opponents[0].game_opponent_name
                    }
                }
                
                if gameStatus.opponents[0].game_opponent_item != nil {
                    lblOpponentMeal.attributedText = gameStatus.opponents[0].game_opponent_item.getUnderLineAttributedText()
                }
            }
        }
        
        switch userStatus {
        //MARK: Game Not Started
        case .Game_Not_Started:
            btnOpponent.setTitle("?", for: .normal)
            btnOpponent.borderColor = AppColors.themeRed
            btnOpponent.backgroundColor = .white
            btnOpponent.setTitleColor(AppColors.themeRed, for: .normal)
            
            btnAddOpponent.backgroundColor = AppColors.themeRed
            btnAddOpponent.setTitleColor(.white, for: .normal)
            btnAddOpponent.setImage(nil, for: .normal)
            btnAddOpponent.setTitle("STEP 1 : ADD YOUR OPPONENT", for: .normal)
            btnAddOpponent.setImage(nil, for: .normal)
            btnAddOpponent.isUserInteractionEnabled = true
            
            btnSkip.isHidden = false
            
            btnChooseMeal.backgroundColor = .clear
            btnChooseMeal.borderColor = .lightGray
            btnChooseMeal.borderWidth = 1.0
            btnChooseMeal.setTitleColor(.lightGray, for: .normal)
            btnChooseMeal.setImage(nil, for: .normal)
            btnChooseMeal.isUserInteractionEnabled = false
            
            btnChooseMeal1.isHidden = true
            btnChooseMeal2.isHidden = true
            
            btnFlipCoin.backgroundColor = .clear
            btnFlipCoin.borderColor = .lightGray
            btnFlipCoin.borderWidth = 1.0
            btnFlipCoin.setTitleColor(.lightGray, for: .normal)
            btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
            btnFlipCoin.isUserInteractionEnabled = false
            
            break
            
        //MARK: Game Opponent Added
        case .User_Added_Opponent:
            btnAddOpponent.backgroundColor = AppColors.themeRed
            btnAddOpponent.setTitleColor(.white, for: .normal)
            btnAddOpponent.setImage(#imageLiteral(resourceName: "tick"), for: .normal)
            btnAddOpponent.setTitle("STEP 1 : ADD YOUR OPPONENT ", for: .normal)
            btnAddOpponent.isUserInteractionEnabled = false
            
            btnChooseMeal.backgroundColor = AppColors.themeRed
            btnChooseMeal.setTitleColor(.white, for: .normal)
            btnChooseMeal.setImage(nil, for: .normal)
            btnChooseMeal.isUserInteractionEnabled = true
            
            btnChooseMeal1.isHidden = true
            btnChooseMeal2.isHidden = true
            
            btnFlipCoin.backgroundColor = .clear
            btnFlipCoin.borderColor = .lightGray
            btnFlipCoin.borderWidth = 1.0
            btnFlipCoin.setTitleColor(.lightGray, for: .normal)
            btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
            btnFlipCoin.isUserInteractionEnabled = false
            
            break
            
        //MARK: Game Choose Meal Tapped
        case .Choose_Meal_Tapped:
            btnAddOpponent.backgroundColor = AppColors.themeRed
            btnAddOpponent.setTitleColor(.white, for: .normal)
            btnAddOpponent.setImage(#imageLiteral(resourceName: "tick"), for: .normal)
            btnAddOpponent.setTitle("STEP 1 : ADD YOUR OPPONENT ", for: .normal)
            btnAddOpponent.isUserInteractionEnabled = false
            
            btnChooseMeal1.isHidden = false
            btnChooseMeal2.isHidden = true
            
            btnFlipCoin.backgroundColor = .clear
            btnFlipCoin.borderColor = .lightGray
            btnFlipCoin.borderWidth = 1.0
            btnFlipCoin.setTitleColor(.lightGray, for: .normal)
            btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
            btnFlipCoin.isUserInteractionEnabled = false
            
            break
            
        //MARK: Game Choose Meal Step 1
        case .Choose_Meal_1_Tapped:
            btnChooseMeal1.isHidden = true
            btnChooseMeal2.isHidden = false
            
            btnAddOpponent.isUserInteractionEnabled = false
            btnFlipCoin.isUserInteractionEnabled = false
            
            break
            
        //MARK: Game Meal Selected
        case .Meal_Selected:
            btnAddOpponent.backgroundColor = AppColors.themeRed
            btnAddOpponent.setTitleColor(.white, for: .normal)
            btnAddOpponent.setImage(#imageLiteral(resourceName: "tick"), for: .normal)
            btnAddOpponent.setTitle("STEP 1 : ADD YOUR OPPONENT ", for: .normal)
            btnAddOpponent.isUserInteractionEnabled = false
            
            btnChooseMeal.backgroundColor = AppColors.themeRed
            btnChooseMeal.setTitleColor(.white, for: .normal)
            btnChooseMeal.setImage(#imageLiteral(resourceName: "tick").withRenderingMode(.alwaysOriginal), for: .normal)
            btnChooseMeal.setTitle("STEP 2 : CHOOSE YOUR MEAL ", for: .normal)
            btnChooseMeal.isUserInteractionEnabled = false
            
            btnChooseMeal1.isHidden = true
            btnChooseMeal2.isHidden = true
        
            if gameStatus.opponent_status == 1 {
                btnFlipCoin.backgroundColor = AppColors.themeRed
                btnFlipCoin.setTitleColor(.white, for: .normal)
                btnFlipCoin.setTitle("READY TO PLAY", for: .normal)
                btnFlipCoin.isUserInteractionEnabled = false
            }
            else {
                if gameStatus.opponents != nil {
                    if getUserStatus(status: gameStatus.opponents[0].user_status) == .Meal_Selected {
                        btnFlipCoin.backgroundColor = AppColors.themeRed
                        btnFlipCoin.setTitleColor(.white, for: .normal)
                        btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
                        btnFlipCoin.isUserInteractionEnabled = true
                    }
                    else {
                        btnFlipCoin.backgroundColor = .clear
                        btnFlipCoin.borderColor = .lightGray
                        btnFlipCoin.borderWidth = 1.0
                        btnFlipCoin.setTitleColor(.lightGray, for: .normal)
                        btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
                        btnFlipCoin.isUserInteractionEnabled = false
                    }
                }
                else {
                    btnFlipCoin.backgroundColor = .clear
                    btnFlipCoin.borderColor = .lightGray
                    btnFlipCoin.borderWidth = 1.0
                    btnFlipCoin.setTitleColor(.lightGray, for: .normal)
                    btnFlipCoin.setTitle("STEP 3 : FLIP THE COIN", for: .normal)
                    btnFlipCoin.isUserInteractionEnabled = false
                }
            }
            
            break
            
        //MARK: Game Started
        case .Game_Started:
            self.btnAddOpponent.isUserInteractionEnabled = false
            self.btnChooseMeal.isUserInteractionEnabled = false
            btnFlipCoin.isUserInteractionEnabled = false
        }
        
        setStatusIndicator()
    }
    
    func setStatusIndicator() {
        viewAddOpponentIndicator.removeAllSubViews()
        viewMealIndicator.removeAllSubViews()
        viewFlipCoinIndicator.removeAllSubViews()
        
        if gameStatus.opponent_status == 1 {
            ///user is opponent
            
            var arrMeal = [String]()
            var arrFlipCoin = [String]()
            
            switch userStatus {
                
            case .User_Added_Opponent, .Choose_Meal_Tapped, .Choose_Meal_1_Tapped:
                arrMeal.append((btnYou.titleLabel?.text)!)
                
                break
                
            case .Meal_Selected:
                arrFlipCoin.append((btnYou.titleLabel?.text)!)
                
                break
                
            default:
                break
            }
            
            switch gameStatus.user_status {
            case 4:
                arrMeal.append(gameStatus.game_owner_name.getInitials().uppercased())
                
            case 2:
                arrFlipCoin.append(gameStatus.game_owner_name.getInitials().uppercased())
                
                break
                
            default:
                break
            }
            
            if arrMeal.count > 0 {
                setIndicators(viewBg: viewMealIndicator, images: arrMeal)
            }
            
            if arrFlipCoin.count > 0 {
                setIndicators(viewBg: viewFlipCoinIndicator, images: arrFlipCoin)
            }
        }
        else {
            ///user initiated the game
            
            var arrAddOpponent = [String]()
            var arrMeal = [String]()
            var arrFlipCoin = [String]()
            
            switch userStatus {
            //MARK: Game Not Started
            case .Game_Not_Started:
                arrAddOpponent.append((btnYou.titleLabel?.text)!)
                
                break
                
            case .User_Added_Opponent, .Choose_Meal_Tapped, .Choose_Meal_1_Tapped:
                arrMeal.append((btnYou.titleLabel?.text)!)
                
                break
                
            case .Meal_Selected:
                arrFlipCoin.append((btnYou.titleLabel?.text)!)
                
                break
                
            default:
                break
            }
            
            if gameStatus.opponents != nil {
                switch gameStatus.opponents[0].user_status {
                case 0:
                    arrAddOpponent.append(gameStatus.opponents[0].game_opponent_name.getInitials().uppercased())
                    
                    break
                    
                case 1:
                    arrMeal.append(gameStatus.opponents[0].game_opponent_name.getInitials().uppercased())
                    
                case 2:
                    arrFlipCoin.append(gameStatus.opponents[0].game_opponent_name.getInitials().uppercased())
                    
                    break
                    
                default:
                    break
                }
            }
            
            if arrAddOpponent.count > 0 {
                setIndicators(viewBg: viewAddOpponentIndicator, images: arrAddOpponent)
            }
            
            if arrMeal.count > 0 {
                setIndicators(viewBg: viewMealIndicator, images: arrMeal)
            }
            
            if arrFlipCoin.count > 0 {
                setIndicators(viewBg: viewFlipCoinIndicator, images: arrFlipCoin)
            }
        }
    }
    
    func animateCoin(coinFace: Int) {
        if repeatCount > maxReps {
            lblFlipResult.text = (coinFace == 1) ? lblYouMeal.text : lblOpponentMeal.text
            viewCoinBg.layer.contents = (flipCoinModel.generateRandomNumber() == 0) ? #imageLiteral(resourceName: "coin").cgImage : #imageLiteral(resourceName: "coin").cgImage
            viewCoinBg.layer.transform = CATransform3DIdentity
            
            if gameStatus.opponent_status == 1 {
                let winnerVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "WinnerVC") as! WinnerVC
                winnerVC.winnerUser = winnerUserForOpponent
                winnerVC.flagNavigationFrom = "appdelegate"
                self.navigationController?.push(viewController: winnerVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
            }
            else {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    let winnerMap: WinnerMap = WinnerMap()
                    winnerMap.game_session_id = self.opponentUser.game_session_id
                    winnerMap.user_won_id = (coinFace == 1) ? self.opponentUser.owner_participant_id : self.opponentUser.opponent_participant_id
                    winnerMap.winner_name = (coinFace == 1) ? self.gameStatus.game_owner_name : self.gameStatus.opponents[0].game_opponent_name
                    winnerMap.winner_meal_item = (coinFace == 1) ? self.lblYouMeal.text : self.lblOpponentMeal.text
                    
                    let winnerVC = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "WinnerVC") as! WinnerVC
                    winnerVC.winnerUser = winnerMap
                    winnerVC.flagNavigationFrom = "game"
                    self.navigationController?.push(viewController: winnerVC, transitionType: AppConstants.transitionType, duration: AppConstants.transitionDuration)
                }
            }
            
            return
        }
        repeatCount += 1
        
        if repeatCount == 1 {
            let duration = animationDuration * Double((maxReps+1))
            
            let startFrame = viewCoinBg.frame
            UIView.animate(withDuration: duration, delay: 0.0, options: UIViewAnimationOptions.curveEaseOut, animations: {
                var frame = self.viewCoinBg.frame
                
                frame.origin.y = 40.0
                self.viewCoinBg.frame = frame
            }, completion: {
                _ in
                
                UIView.animate(withDuration: duration, delay: 0.0, options: UIViewAnimationOptions.beginFromCurrentState, animations: {
                    self.viewCoinBg.frame = startFrame
                }, completion: nil)
            })
        }
        
        UIView.animate(withDuration: animationDuration, delay: 0.0, options: UIViewAnimationOptions.curveLinear, animations: {
            var rotation = CATransform3DIdentity
            
            rotation = CATransform3DRotate(rotation, 0.5 * CGFloat.pi, 1.0, 0.0, 0.0)
            self.viewCoinBg.layer.transform = rotation
        }, completion: {
            _ in
            
            self.viewCoinBg.layer.contents = #imageLiteral(resourceName: "coin").cgImage
            
            
            UIView.animate(withDuration: self.animationDuration, delay: 0.0, options: UIViewAnimationOptions.curveLinear, animations: {
                
                var rotation = self.viewCoinBg.layer.transform;
                
                rotation = CATransform3DRotate(rotation, 1.0 * CGFloat.pi, 1.0, 0.0, 0.0);
                self.viewCoinBg.layer.transform = rotation;
            }, completion: {
                _ in
                
                self.viewCoinBg.layer.contents = #imageLiteral(resourceName: "coin").cgImage
                self.animateCoin(coinFace: coinFace)
            })
        })
    }
}
