package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

  @MockBean RecommendationRequestsRepository repository;

  @MockBean UserRepository userRepository;

  // ---------- AUTHZ: GET /api/recommendationrequests/all ----------

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().isOk());
  }

  // ---------- AUTHZ: POST /api/recommendationrequests/post ----------

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_post_without_csrf() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "no-csrf@ucsb.edu")
                .param("professorEmail", "prof@ucsb.edu")
                .param("explanation", "test")
                .param("dateRequested", "2025-01-01T00:00:00")
                .param("dateNeeded", "2025-01-02T00:00:00")
                .param("done", "false"))
        .andExpect(status().isForbidden()); // 403 due to missing CSRF
  }

  // ---------- BEHAVIOR: GET /all returns repository data ----------

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_all_returns_list_and_calls_repo() throws Exception {
    LocalDateTime r1_req = LocalDateTime.parse("2025-01-01T09:00:00");
    LocalDateTime r1_need = LocalDateTime.parse("2025-02-01T17:00:00");
    RecommendationRequests r1 =
        RecommendationRequests.builder()
            .requesterEmail("alice@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("Grad apps")
            .dateRequested(r1_req)
            .dateNeeded(r1_need)
            .done(false)
            .build();

    LocalDateTime r2_req = LocalDateTime.parse("2025-03-10T10:30:00");
    LocalDateTime r2_need = LocalDateTime.parse("2025-03-25T23:59:59");
    RecommendationRequests r2 =
        RecommendationRequests.builder()
            .requesterEmail("bob@ucsb.edu")
            .professorEmail("prof2@ucsb.edu")
            .explanation("Internship")
            .dateRequested(r2_req)
            .dateNeeded(r2_need)
            .done(true)
            .build();

    var expected = new ArrayList<>(Arrays.asList(r1, r2));
    when(repository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // ---------- BEHAVIOR: POST /post creates rows (ADMIN) ----------

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_request_done_false() throws Exception {
    LocalDateTime dr = LocalDateTime.parse("2025-10-28T13:45:00");
    LocalDateTime dn = LocalDateTime.parse("2025-11-05T17:00:00");

    RecommendationRequests toSave =
        RecommendationRequests.builder()
            .requesterEmail("saqif@ucsb.edu")
            .professorEmail("vigna@ucsb.edu")
            .explanation("PhD applications")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(repository.save(eq(toSave))).thenReturn(toSave);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "saqif@ucsb.edu")
                    .param("professorEmail", "vigna@ucsb.edu")
                    .param("explanation", "PhD applications")
                    .param("dateRequested", "2025-10-28T13:45:00")
                    .param("dateNeeded", "2025-11-05T17:00:00")
                    .param("done", "false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository, times(1)).save(toSave);
    String expectedJson = mapper.writeValueAsString(toSave);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_request_done_true() throws Exception {
    LocalDateTime dr = LocalDateTime.parse("2025-12-01T08:00:00");
    LocalDateTime dn = LocalDateTime.parse("2025-12-15T17:00:00");

    RecommendationRequests toSave =
        RecommendationRequests.builder()
            .requesterEmail("user@ucsb.edu")
            .professorEmail("advisor@ucsb.edu")
            .explanation("Scholarship reference")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(true)
            .build();

    when(repository.save(eq(toSave))).thenReturn(toSave);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "user@ucsb.edu")
                    .param("professorEmail", "advisor@ucsb.edu")
                    .param("explanation", "Scholarship reference")
                    .param("dateRequested", "2025-12-01T08:00:00")
                    .param("dateNeeded", "2025-12-15T17:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository, times(1)).save(toSave);
    String expectedJson = mapper.writeValueAsString(toSave);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
