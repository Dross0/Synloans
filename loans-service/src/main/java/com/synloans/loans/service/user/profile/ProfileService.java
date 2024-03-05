package com.synloans.loans.service.user.profile;

import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest;

public interface ProfileService {

    Profile getByUsername(String username);

    void update(String username, ProfileUpdateRequest updateRequest);

}
