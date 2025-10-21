package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HomepageController.class)
@Import(TestConfig.class)
public class HomepageControllerTests extends ControllerTestCase {

  @MockitoBean UserRepository userRepository;

  @Test
  public void test_homepage_not_logged_in() throws Exception {
    String expectedResponse =
        """
        <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
        <p>
          <ul>
            <li>Not logged in</li>
            <li><a href="/oauth2/authorization/google">Login</a></li>
            <li>Roles: [ROLE_ANONYMOUS]</li>
            <li><a href="/swagger-ui/index.html">Swagger API Links</a></li>
            <li><a href="/h2-console">H2 console (only on localhost)</a></li>
          </ul>
        </p>
        """;

    MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    assertEquals(expectedResponse, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"USER"})
  public void test_homepage_logged_in() throws Exception {
    String expectedResponse =
        """
        <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
        <p>
          <ul>
            <li>Currently logged in as user@example.org</li>
            <li><a href="/logout">Logout</a></li>
            <li>Roles: [ROLE_USER]</li>
            <li><a href="/swagger-ui/index.html">Swagger API Links</a></li>
            <li><a href="/h2-console">H2 console (only on localhost)</a></li>
          </ul>
        </p>
        """;

    MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    assertEquals(expectedResponse, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(
      roles = {"ADMIN"},
      username = "foo")
  public void test_homepage_admin_logged_in() throws Exception {
    String expectedResponse =
        """
        <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
        <p>
          <ul>
            <li>Currently logged in as foo@example.org</li>
            <li><a href="/logout">Logout</a></li>
            <li>Roles: [ROLE_ADMIN]</li>
            <li><a href="/swagger-ui/index.html">Swagger API Links</a></li>
            <li><a href="/h2-console">H2 console (only on localhost)</a></li>
          </ul>
        </p>
        """;

    MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    assertEquals(expectedResponse, response.getResponse().getContentAsString());
  }
}
