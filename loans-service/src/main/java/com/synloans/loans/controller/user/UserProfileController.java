package com.synloans.loans.controller.user;

import com.synloans.loans.model.dto.Profile;
import com.synloans.loans.service.user.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Profile getProfile(Authentication authentication){
        return profileService.getProfile(authentication.getName());
    }

    @PutMapping(value = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProfile(@RequestBody @Valid Profile newProfile, Authentication authentication){
        profileService.editProfile(authentication.getName(), newProfile);
    }
}
