package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomepageController {

  @Autowired CurrentUserService currentUserService;

  @GetMapping("/")
  public String index() {
    String HomePageHTMLTemplate =
        """
        <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
        <p>
          <ul>
            %s
            %s
            %s
            <li><a href="/swagger-ui/index.html">Swagger API Links</a></li>
            <li><a href="/h2-console">H2 console (only on localhost)</a></li>
          </ul>
        </p>
        """;
    return String.format(HomePageHTMLTemplate, getLoggedInAs(), getLoginLogoutLink(), getRoles());
  }

  private String getLoginLogoutLink() {

    return currentUserService.isLoggedIn()
        ? """
      <li><a href="/logout">Logout</a></li>"""
        : """
      <li><a href="/oauth2/authorization/google">Login</a></li>""";
  }

  private String getLoggedInAs() {
    return currentUserService.isLoggedIn()
        ? String.format(
            "<li>Currently logged in as %s</li>", currentUserService.getUser().getEmail())
        : "<li>Not logged in</li>";
  }

  private String getRoles() {
    return String.format("<li>Roles: %s</li>", currentUserService.getRolesSorted());
  }
}
