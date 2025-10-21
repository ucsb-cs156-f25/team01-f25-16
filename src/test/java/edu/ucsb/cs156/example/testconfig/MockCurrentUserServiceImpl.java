package edu.ucsb.cs156.example.testconfig;

import edu.ucsb.cs156.example.entities.User;
import edu.ucsb.cs156.example.services.CurrentUserServiceImpl;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service("testingUser")
public class MockCurrentUserServiceImpl extends CurrentUserServiceImpl {

  public User getMockUser(SecurityContext securityContext, Authentication authentication) {
    Object principal = authentication.getPrincipal();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

    boolean admin = roles.contains("ROLE_ADMIN");
    String username = admin ? "Admin" : "User";

    String googleSub = "fake" + username;
    String email = username + "@example.org";
    String pictureUrl = "https://example.org/" + username + ".jpg";
    String fullName = "Fake " + username;
    String givenName = "Fake";
    String familyName = username;
    boolean emailVerified = true;
    String locale = "";
    String hostedDomain = "example.org";

    org.springframework.security.core.userdetails.User user = null;

    if (principal instanceof org.springframework.security.core.userdetails.User) {
      user = (org.springframework.security.core.userdetails.User) principal;
      googleSub = "fake_" + user.getUsername();
      email = user.getUsername() + "@example.org";
      pictureUrl = "https://example.org/" + user.getUsername() + ".jpg";
      fullName = "Fake " + user.getUsername();
      givenName = "Fake";
      familyName = user.getUsername();
      emailVerified = true;
      locale = "";
      hostedDomain = "example.org";
    }

    User u =
        User.builder()
            .googleSub(googleSub)
            .email(email)
            .pictureUrl(pictureUrl)
            .fullName(fullName)
            .givenName(givenName)
            .familyName(familyName)
            .emailVerified(emailVerified)
            .locale(locale)
            .hostedDomain(hostedDomain)
            .admin(admin)
            .id(1L)
            .build();

    return u;
  }

  public User getUser() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();

    System.out.println("MockCurrentUserServiceImpl.getUser() called");
    System.out.println("authentication = " + authentication);
    System.out.println("securityContext = " + securityContext);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    System.out.println("authorities = " + authorities);

    if ((authentication instanceof AnonymousAuthenticationToken)) {
      return null;
    }

    if (!(authentication instanceof OAuth2AuthenticationToken)) {
      return getMockUser(securityContext, authentication);
    }

    return null;
  }
}
