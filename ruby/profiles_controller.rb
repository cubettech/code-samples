# ProfilesController manages various features of user profiles
# GAINinBusiness is a Plymouth University initiative.
#------------------------------------------------------------------------------
class ProfilesController < ApplicationController
  layout "alt"
  respond_to :html

  # Creates a new profile.
  #----------------------------------------------------------------------------
  def new
    @profile = build_user_profile

    respond_with @profile
  end

  # Create a complete user profile.
  #----------------------------------------------------------------------------
  def create
    @profile = build_profile_from_params

    if @profile.save
      @profile.set_as_complete
      @profile.update_newsletter_subscription!

      redirect_to dashboard_url, notice: 'Your profile is now completed.'
    else
      render 'new'
    end
  end

  # Private methods.
  #----------------------------------------------------------------------------

  private

  def build_user_profile
    unless user.has_completed_profile?
      @_profile ||= user.build_profile

      # Extract profile information from LinkedIn if the omniauth data is present in the session

      if user.linkedin_token.present?
        begin
          client = LinkedIn::Client.new(Settings.linked_in.api_key, Settings.linked_in.api_secret, user.linkedin_token)
          positions = client.profile(fields: %w(positions))
          position = positions["positions"]["all"].select { |p| p["isCurrent"] == true }.first

          if position.present?
            company_id = position["company"]["id"]
            company = client.company(id: company_id, fields: %w(website-url description))

            if company.present?
              @_profile.website_url = company["website-url"]
              @_profile.business_description = company["description"]
            end
          end
        rescue => e
          # Pass
          logger.warn "#{e}"
        end
      end
    end

    @_profile
  end

  # gets users profile data
  #----------------------------------------------------------------------------
  def user
    @_user ||= User.find current_user
  end

  # building user profile.
  #----------------------------------------------------------------------------
  def build_profile_from_params
    user.build_profile profile_params
  end

  # Sets profile parameters.
  #----------------------------------------------------------------------------
  def profile_params
    params.require(:profile).permit(
      :can_accomodate_intern,
      :business_telephone_number,
      :business_email,
      :business_description,
      :delivery_point_suffix,
      :business_address_line1,
      :business_address_line2,
      :business_address_line3,
      :business_post_town,
      :business_post_county,
      :business_postcode,
      :business_sector_id,
      :Work_Placement,
      :Graduate_Internship,
      :graduate_jobs,
      :None_of_the_above,
      :number_of_employees,
      :website_url,
      :years_trading,
      :image,
      :image_cache,
      :remove_image,
      :receives_newsletter,
      :receives_notifications,
      { :category_ids => [] },
      { :interest_area_ids => [] }
    )
  end
end
