import UIKit

let kAllowConsolePrint = true

class Utility: NSObject {
    //MARK: Console Print
    static func consolePrint(aObject: Any) {
        if kAllowConsolePrint {
            print(aObject)
        }
    }
    
    //MARK: Environment
    ///get value depending on the configuration selected
    static func getConfiguration(key: EnviromentKey) -> String {
        var configuration = Configuration()
        
        switch key {
        case .baseURL:
            return configuration.environment.baseURL
        case .client_secret:
            return configuration.environment.client_secret
        case .bugsnag_key:
            return configuration.environment.bugsnag_key
        }
    }
    
    //MARK: Login Status
    ///get login status of the user saved in user defaults
    static func getLoginStatus() -> Bool {
        guard let loginStatus = UserDefaultsUtility.get(forKey: .loginStatus) else {
            return false
        }
        
        return loginStatus as! Bool
    }
    
    //MARK:- Token
    static func saveToken(object: TokenAPIData) {
        let encoder = JSONEncoder()
        if let encodedData = try? encoder.encode(object) {
            UserDefaultsUtility.set(encodedData, forKey: .token)
        }
    }
    
    static func getToken() -> TokenAPIData? {
        if let savedUser = UserDefaultsUtility.get(forKey: .token) as? Data {
            let decoder = JSONDecoder()
            if let loadedUser = try? decoder.decode(TokenAPIData.self, from: savedUser) {
                return loadedUser
            }
        }
        
        return nil
    }
    
    //MARK:- User
    static func saveUser(object: UserData) {
        let encoder = JSONEncoder()
        if let encodedData = try? encoder.encode(object) {
            UserDefaultsUtility.set(encodedData, forKey: .user)
        }
    }
    
    static func getUser() -> UserData? {
        if let savedUser = UserDefaultsUtility.get(forKey: .user) as? Data {
            let decoder = JSONDecoder()
            if let loadedUser = try? decoder.decode(UserData.self, from: savedUser) {
                return loadedUser
            }
        }
        
        return nil
    }
    
    static func getCurrentWorkspace() -> Workspace? {
        let userDetails = getUser()
        if userDetails == nil {
            return nil
        }
        for workspace in (userDetails?.workspaces)! {
            if workspace.active == true {
                return workspace
            }
        }
        
        return nil
    }
    
    //logout and clear userdefaults
    static func logout() {
        clearUserDefaults()
        clearPushNotification()
        Utility.clearAllFile()
        CoreDataManager.sharedManager.deleteAllData("DownloadCourseDetails")
        AppDelegate.shared.setRootViewController()
    }
    
    static func clearUserDefaults() {
        var savedToken = Data()
        if UserDefaultsUtility.get(forKey: .token) != nil {
            savedToken = UserDefaultsUtility.get(forKey: .token) as! Data
        }
            
        UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
        UserDefaults.standard.synchronize()
    
        if !savedToken.isEmpty {
            UserDefaultsUtility.set(savedToken, forKey: .token)
            UserDefaults.standard.synchronize()
        }
    }
    
    static func clearAllFile() {
        let fileManager = FileManager.default
        
        let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        
        do {
                let fileName = try fileManager.contentsOfDirectory(atPath: paths)
                
                for file in fileName {
                    // For each file in the directory, create full path and delete the file
                    let filePath = URL(fileURLWithPath: paths).appendingPathComponent(file).absoluteURL
                    try fileManager.removeItem(at: filePath)
                }
            }
        catch let error {
            print(error.localizedDescription)
        }
    }
    
    //clear push notifications
    static func clearPushNotification() {
        UIApplication.shared.applicationIconBadgeNumber = 0
        let center = UNUserNotificationCenter.current()
        center.removeAllDeliveredNotifications()
    }
    
    //app version and build number
    static func getAppVersion() -> String {
        return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
    }
    
    static func getAppBuildNumber() -> String {
        return Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? ""
    }
    
    //streaming quality
    static func setStreamingQuality(quality: StreamingQuality) {
        var tag = 0
        switch quality {
        case .None:
            tag = 0
            
        case .Highest:
            tag = 1
            
        case .High:
            tag = 2
            
        case .Standard:
            tag = 3
            
        case .Low:
            tag = 4
        }
        
        UserDefaultsUtility.set(tag, forKey: .streamingQuality)
    }
    
    static func getStreamingQuality() -> (StreamingQuality, String) {
        switch UserDefaultsUtility.get(forKey: .streamingQuality) as? Int {
        case 1:
            return (.Highest, "Highest")
            
        case 2:
            return (.High, "High")
            
        case 3:
            return (.Standard, "Standard")
            
        case 4:
            return (.Low, "Low")
            
        default:
            return (.None, "")
        }
    }
    
    static func getStreamingQualityUrl() -> (String) {
        switch UserDefaultsUtility.get(forKey: .streamingQuality) as? Int {
        case 1:
            return (AppConstants.videoQualityHighest)
            
        case 2:
            return (AppConstants.videoQualityHigh)
            
        case 3:
            return (AppConstants.videoQualityStandard)
            
        case 4:
            return (AppConstants.videoQualityLow)
            
        default:
            return ( "")
        }
    }
  
    //Cellular Download
    static func setCellularDownload(status: Bool) {
        UserDefaultsUtility.set(status, forKey: .cellularDownload)
    }
    
    static func getCellularDownload() -> Bool {
        return UserDefaultsUtility.get(forKey: .cellularDownload) as? Bool ?? false
    }
    
    //File Expiry
    static func deleteExpiredFiles() {
        let urls = FileManager.default.urls(for: .documentDirectory)! as [URL]
        for url in urls {
            let hourDifference = Date().hours(from: getCreatedDate(path: url)!)
            if hourDifference > AppConstants.fileExpiryHour {
                deleteFolder(atPath: url)
            }
        }
    }
    
    static func deleteFolder(atPath: URL) {
        let fileManager = FileManager.default
        
        do {
            try fileManager.removeItem(at: atPath)
            CoreDataManager.sharedManager.deleteDownloadCourseDetail(Int(atPath.lastPathComponent)!)
        }
        catch let error as NSError {
            Utility.consolePrint(aObject: "Error deleting folder: \(error.localizedDescription)")
        }
    }
    
    static func getCreatedDate(path: URL) -> Date? {
        let file: URL = path
        if let attributes = try? FileManager.default.attributesOfItem(atPath: file.path) as [FileAttributeKey: Any],
           let creationDate = attributes[FileAttributeKey.creationDate] as? Date {
            return creationDate
        }
        
        return nil
    }
}
