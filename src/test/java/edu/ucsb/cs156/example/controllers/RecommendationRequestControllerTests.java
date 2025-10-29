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

  // ===== AUTHZ for GET /api/recommendationrequests/all =====

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().isOk());
  }

  // ===== AUTHZ for POST /api/recommendationrequests/post =====

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
  }

  // ===== Behavior: GET /all returns repository data =====

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_all_recommendationrequests() throws Exception {
    // arrange
    LocalDateTime dt1_req = LocalDateTime.parse("2025-01-01T09:00:00");
    LocalDateTime dt1_need = LocalDateTime.parse("2025-02-01T17:00:00");

    RecommendationRequests r1 =
        RecommendationRequests.builder()
            .requesterEmail("alice@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("Graduate school application")
            .dateRequested(dt1_req)
            .dateNeeded(dt1_need)
            .done(false)
            .build();

    LocalDateTime dt2_req = LocalDateTime.parse("2025-03-10T10:30:00");
    LocalDateTime dt2_need = LocalDateTime.parse("2025-03-25T23:59:59");

    RecommendationRequests r2 =
        RecommendationRequests.builder()
            .requesterEmail("bob@ucsb.edu")
            .professorEmail("prof2@ucsb.edu")
            .explanation("Internship recommendation")
            .dateRequested(dt2_req)
            .dateNeeded(dt2_need)
            .done(true)
            .build();

    ArrayList<RecommendationRequests> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(r1, r2));

    when(repository.findAll()).thenReturn(expected);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // ===== Behavior: POST /post creates a new row (ADMIN only) =====

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_recommendationrequest() throws Exception {
    // arrange
    LocalDateTime dt_req = LocalDateTime.parse("2025-10-28T13:45:00");
    LocalDateTime dt_need = LocalDateTime.parse("2025-11-05T17:00:00");

    RecommendationRequests toSave =
        RecommendationRequests.builder()
            .requesterEmail("saqif@ucsb.edu")
            .professorEmail("vigna@ucsb.edu")
            .explanation("PhD applications")
            .dateRequested(dt_req)
            .dateNeeded(dt_need)
            .done(false)
            .build();

    when(repository.save(eq(toSave))).thenReturn(toSave);

    // act
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

    // assert
    verify(repository, times(1)).save(toSave);
    String expectedJson = mapper.writeValueAsString(toSave);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
