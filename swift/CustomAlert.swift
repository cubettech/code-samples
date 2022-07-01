public typealias ButtonTapEventHandler = () -> Void
public typealias CheckBoxTapEventHandler = (_ checkBox: CheckBoxWithLabel) -> Void
/// Public class to show full screen alert with icon, title, message and action button.
public class CustomFullScreenAlert: UIViewController, NibLoadable, CheckBoxWithLabelDelegate {
    @IBOutlet private weak var titleLabel: Label!
    @IBOutlet private weak var messageLabel: Label!
    @IBOutlet private weak var iconImageView: UIImageView!
    @IBOutlet private weak var actionButton: PrimaryButton!
    @IBOutlet private weak var secondaryDismissButton: SecondaryButton!
    @IBOutlet private weak var contentView: UIView!
    @IBOutlet private weak var cancelButton: UIButton!
    @IBOutlet private weak var checkBoxWithLabel: CheckBoxWithLabel!
    @IBOutlet private weak var imageHeightConstraint: NSLayoutConstraint!
    @IBOutlet private weak var imageTopConstraint: NSLayoutConstraint!
    /// Represents the icon.
    public var iconImage: UIImage?
    /// Represents the title of the alert.
    public var alertTitle: String?
    /// Represents the message of the alert.
    public var message: String?
    /// Represents the action button title.
    public var actionButtonTitle: String?
    public var secondaryButtonTitle: String?
    public var allowDismiss: Bool = true
    public var checkBoxMessage: String?
    public var actionButtonThemeColor: PrimaryButton.Style?

    public var alertTitleAlignment: NSTextAlignment? = .center {
        didSet {
            titleLabel?.textAlignment = alertTitleAlignment ?? .center
        }
    }
    public var messageAlignment: NSTextAlignment? = .center {
        didSet {
            messageLabel?.textAlignment = messageAlignment ?? .center
        }
    }
    public var titleColor: UIColor? = Color.text.primary {
        didSet {
            self.titleLabel.textColor = titleColor
        }
    }
    public var messageColor: UIColor? = Color.text.primary {
        didSet {
            self.messageLabel.textColor = messageColor
        }
    }

    var actionButtonEventHandler: ButtonTapEventHandler?
    var secondaryButtonEventHandler: ButtonTapEventHandler?
    var checkBoxHandler: CheckBoxTapEventHandler?
    /// Public init method.
    /// - Parameters:
    ///   - icon: Icon to be shown on alert.
    ///   - alertTitle: Title of the alert.
    ///   - alertMessage: Message of the alert.
    ///   - actionButtonTitle: Action button title of the alert.
    ///   - actionButtonEventHandler: Action button event handler.
    ///   - secondaryButtonEventHandler: Secondary button event handler.
    public init(icon: UIImage?, alertTitle: String, alertMessage: String, actionButtonTitle: String, secondaryButtonTitle: String? = nil, checkBoxMessage: String? = nil, actionButtonThemeColor: PrimaryButton.Style? = nil, actionButtonEventHandler: @escaping ButtonTapEventHandler,
                secondaryButtonEventHandler: @escaping ButtonTapEventHandler = {},
                checkBoxEventHandler: @escaping CheckBoxTapEventHandler = {_ in }) {
        let (nibName, bundle) = type(of: self).nibNameAndBundle()
        super.init(nibName: nibName, bundle: bundle)
        self.alertTitle = alertTitle
        self.message = alertMessage
        self.actionButtonTitle = actionButtonTitle
        self.iconImage = icon
        self.secondaryButtonTitle = secondaryButtonTitle
        self.actionButtonEventHandler = actionButtonEventHandler
        self.secondaryButtonEventHandler = secondaryButtonEventHandler
        self.actionButtonThemeColor = actionButtonThemeColor
        self.checkBoxMessage = checkBoxMessage
        self.checkBoxHandler = checkBoxEventHandler
    }
    public func setupSecondaryButtonUI() {
        cancelButton.isHidden = false
        let image = UIImage(bgIcon: .multiply).withRenderingMode(.alwaysTemplate)
        cancelButton.setImage(image, for: .normal)
        cancelButton.tintColor = Color.icon.primary
    }
    public func setupCrossButton() {
        cancelButton.isHidden = false
        let image = UIImage(bgIcon: .multiply).withRenderingMode(.alwaysTemplate)
        cancelButton.setImage(image, for: .normal)
        cancelButton.tintColor = Color.icon.primary
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    /// :nodoc:
    override public func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = Color.Base.overlay
        setUI()
    }
    // MARK: SetUI
    func setUI() {
        self.titleLabel.textColor = Color.text.primary
        self.titleLabel.font = Font.Header1
        self.titleLabel.textAlignment = alertTitleAlignment ?? .center
        self.messageLabel.textColor = Color.text.primary
        self.messageLabel.font = Font.Body1.Regular
        self.messageLabel.textAlignment = messageAlignment ?? .center
        self.actionButton.setTitle(actionButtonTitle, for: .normal)
        if let secondaryButtonTitle = secondaryButtonTitle {
            self.secondaryDismissButton.isHidden = false
            self.secondaryDismissButton.setTitle(secondaryButtonTitle, for: .normal)
        } else {
            self.secondaryDismissButton.isHidden = true
        }
        self.titleLabel.text = self.alertTitle
        self.iconImageView.image = self.iconImage
        if  self.iconImage == nil {
            self.imageHeightConstraint.constant =  0.0
            self.imageTopConstraint.constant =  0.0
        }
        self.messageLabel.text = self.message
        self.contentView.cornerRadius(radius: Size.radius.extraLarge,
                                      maskedCorners: [.layerMinXMinYCorner, .layerMaxXMinYCorner],
                                      color: .clear,
                                      width: 0.0)
        self.contentView.applyShadow(shadowColor: Shadow.low.shadowColor,
                                     opacity: Shadow.low.shadowOpacity,
                                     offset: Shadow.low.shadowOffset,
                                     blur: Shadow.low.shadowBlur,
                                     spread: Shadow.low.shadowSpread)
        self.actionButton.isHidden = actionButtonTitle?.isEmpty ?? false
        self.actionButton.style = actionButtonThemeColor ?? .green
        cancelButton.isHidden = true
        checkBoxWithLabel.isHidden = checkBoxMessage == nil
        checkBoxWithLabel.text = checkBoxMessage
        checkBoxWithLabel.themeColor = Color.icon.primary
        checkBoxWithLabel.delegate = self
    }
    public func didChangeValue(checkBox: CheckBoxWithLabel) {
        checkBoxHandler?(checkBox)
    }
    /// Show the alert.
    public func show(in viewController: UIViewController, withDismissTheme: Bool? = false) {
        self.modalPresentationStyle = .overCurrentContext
        self.modalTransitionStyle = .crossDissolve
        viewController.present(self, animated: true)
        if withDismissTheme ?? false {
            setupSecondaryButtonUI()
        }
    }
    /// Dismiss the alert.
    public func dismiss() {
        self.dismiss(animated: true)
    }
    @IBAction func actionButtonClicked(_ sender: Any) {
        if self.allowDismiss {
            self.dismiss(animated: true, completion: nil)
        }
        if let handler = self.actionButtonEventHandler {
            handler()
        }
    }
    @IBAction func secondaryButtonClicked(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
        if let handler = self.secondaryButtonEventHandler {
            handler()
        }
    }
    @IBAction func cancelButtonClicked(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
}
