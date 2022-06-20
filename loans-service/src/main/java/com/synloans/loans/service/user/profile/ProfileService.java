package com.synloans.loans.service.user.profile;

import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest;

public interface ProfileService {

    Profile getProfile(String username);

    void editProfile(String username, ProfileUpdateRequest updateRequest);

}
