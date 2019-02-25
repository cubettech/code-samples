# ProfilesHealper supports various features of user profiles
# GAINinBusiness is a Plymouth University initiative.
#------------------------------------------------------------------------------
module ProfilesHelper
  # presets options for number of employes
  #----------------------------------------------------------------------------
  def options_for_number_of_employees
    [
      ["0-9 employees", "0-9"],
      ["10-49 employees", "10-49"],
      ["50-249 employees", "50-249"],
      ["250+", "250+"],
      ["Rather not say", "RNS"]
    ]
  end

  # presets options for years of trading
  #----------------------------------------------------------------------------
  def options_for_years_trading
    [
      ["0 years", "0 years"],
      ["1-3 years", "1-3 years"],
      ["3-10 years", "3-10 years"],
      ["10 years", "10+ years"],
      ["Rather not say", "RNS"]
    ]
  end

  # sets resource name
  #----------------------------------------------------------------------------
  def resource_name
    :user
  end

  # defining resources
  #----------------------------------------------------------------------------
  def resource
    @resource ||= User.new
  end

  # mapping devices to user accounts
  #----------------------------------------------------------------------------
  def devise_mapping
    @devise_mapping ||= Devise.mappings[:user]
  end
end
