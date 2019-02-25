# Profile model manages various features of user profiles
# GAINinBusiness is a Plymouth University initiative.
#------------------------------------------------------------------------------
class Profile < ActiveRecord::Base
  mount_uploader :image, ProfileImageUploader

  before_validation :sanitize_postcode
  after_create :check_if_business_location_is_set

  belongs_to :business_sector
  belongs_to :business_location, foreign_key: 'postcode_id', class_name: 'Postcode'
  belongs_to :user
  has_many :user_interests
  has_many :interest_areas, through: :user_interests

  has_many :category_relationships, as: :content
  has_many :categories, through: :category_relationships

  validates :business_sector_id, presence: true
  validates :number_of_employees, presence: true
  validates :interest_areas, presence: true
  validates :business_postcode, postcode_format: true, allow_blank: false
  validates :years_trading, presence: true
  validates :delivery_point_suffix, presence: true
  validates :business_address_line1, presence: true
  validates :business_post_town, presence: true

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def business_county
    business_location.county if business_location.present?
  end

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def set_as_complete
    mark_as_complete_and_save
  end

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def update_newsletter_subscription!
    return unless self.user.present?

    if self.receives_newsletter?
      NewsletterSubscriptionsWorker.perform_async(:subscribe, self.user.email)
    else
      NewsletterSubscriptionsWorker.perform_async(:unsubscribe, self.user.email)
    end
  end

private

# Creates a new profile.
#----------------------------------------------------------------------------
  def mark_as_complete_and_save
    self.complete = true
    save
  end

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def sanitize_postcode
    if attribute_present?('business_postcode')
      self.business_postcode = business_postcode.gsub(' ', '').upcase
    end
  end

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def check_if_business_location_is_set
    lookup_business_location unless business_location
  end

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def lookup_business_location
    BusinessLocator.new(self).lookup
  end
end
