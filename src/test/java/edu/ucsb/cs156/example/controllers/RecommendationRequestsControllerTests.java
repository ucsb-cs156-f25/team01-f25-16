package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

  // ---------- AUTHZ: GET /api/recommendationrequests?id=... ----------
  // pls workrk
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests").param("id", "123"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_found_returns_record() throws Exception {
    // arrange
    LocalDateTime dr = LocalDateTime.parse("2025-10-28T13:45:00");
    LocalDateTime dn = LocalDateTime.parse("2025-11-05T17:00:00");

    RecommendationRequests rr =
        RecommendationRequests.builder()
            .requesterEmail("alice@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad apps")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(repository.findById(123L)).thenReturn(java.util.Optional.of(rr));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "123"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(123L);
    String expectedJson = mapper.writeValueAsString(rr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_not_found_returns_404() throws Exception {
    // arrange
    when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "999"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(999L);
    String body = response.getResponse().getContentAsString();
    // message format varies by template; assert on key parts to be robust
    org.assertj.core.api.Assertions.assertThat(body)
        .containsIgnoringCase("RecommendationRequests")
        .contains("999");
  }

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(put("/api/recommendationrequests").param("id", "123"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(put("/api/recommendationrequests").param("id", "123"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_put_without_csrf() throws Exception {
    var body =
        mapper.writeValueAsString(
            RecommendationRequests.builder()
                .requesterEmail("new@ucsb.edu")
                .professorEmail("prof@ucsb.edu")
                .explanation("updated")
                .dateRequested(LocalDateTime.parse("2025-12-01T08:00:00"))
                .dateNeeded(LocalDateTime.parse("2025-12-15T17:00:00"))
                .done(true)
                .build());

    mockMvc
        .perform(
            put("/api/recommendationrequests")
                .param("id", "123")
                .contentType(APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden()); // missing CSRF
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_put_updates_existing_record() throws Exception {
    // existing in DB
    RecommendationRequests existing =
        RecommendationRequests.builder()
            .requesterEmail("old@ucsb.edu")
            .professorEmail("oldprof@ucsb.edu")
            .explanation("old explanation")
            .dateRequested(LocalDateTime.parse("2025-10-01T09:00:00"))
            .dateNeeded(LocalDateTime.parse("2025-10-20T17:00:00"))
            .done(false)
            .build();
    existing.setId(123L);

    when(repository.findById(123L)).thenReturn(java.util.Optional.of(existing));

    // incoming update (id in body ignored if present)
    RecommendationRequests incoming =
        RecommendationRequests.builder()
            .requesterEmail("new@ucsb.edu")
            .professorEmail("newprof@ucsb.edu")
            .explanation("new explanation")
            .dateRequested(LocalDateTime.parse("2025-11-01T08:15:00"))
            .dateNeeded(LocalDateTime.parse("2025-11-30T17:00:00"))
            .done(true)
            .build();

    // expected saved (same id as existing, updated fields)
    RecommendationRequests expectedSaved =
        RecommendationRequests.builder()
            .requesterEmail("new@ucsb.edu")
            .professorEmail("newprof@ucsb.edu")
            .explanation("new explanation")
            .dateRequested(LocalDateTime.parse("2025-11-01T08:15:00"))
            .dateNeeded(LocalDateTime.parse("2025-11-30T17:00:00"))
            .done(true)
            .build();
    expectedSaved.setId(123L);

    when(repository.save(eq(expectedSaved))).thenReturn(expectedSaved);

    String jsonBody = mapper.writeValueAsString(incoming);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "123")
                    .contentType(APPLICATION_JSON)
                    .content(jsonBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(repository, times(1)).findById(123L);
    verify(repository, times(1)).save(eq(expectedSaved));

    String expectedJson = mapper.writeValueAsString(expectedSaved);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // ---------- BEHAVIOR: PUT not found -> 404 ----------

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_put_nonexistent_id_returns_404() throws Exception {
    when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

    RecommendationRequests incoming =
        RecommendationRequests.builder()
            .requesterEmail("any@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("whatever")
            .dateRequested(LocalDateTime.parse("2025-12-01T08:00:00"))
            .dateNeeded(LocalDateTime.parse("2025-12-15T17:00:00"))
            .done(false)
            .build();

    String jsonBody = mapper.writeValueAsString(incoming);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "999")
                    .contentType(APPLICATION_JSON)
                    .content(jsonBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(repository, times(1)).findById(999L);
    String body = response.getResponse().getContentAsString();
    // robust substring checks (exact template message can vary)
    org.assertj.core.api.Assertions.assertThat(body)
        .containsIgnoringCase("RecommendationRequests")
        .contains("999");
  }

  // ---------- AUTHZ: DELETE /api/recommendationrequests?id=... ----------

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests").param("id", "15"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests").param("id", "15"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_without_csrf() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests").param("id", "15"))
        .andExpect(status().isForbidden()); // CSRF required
  }

  // ---------- BEHAVIOR: DELETE deletes existing row (ADMIN) ----------

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_existing_request() throws Exception {
    // arrange
    LocalDateTime dr = LocalDateTime.parse("2025-01-01T09:00:00");
    LocalDateTime dn = LocalDateTime.parse("2025-02-01T17:00:00");

    RecommendationRequests row =
        RecommendationRequests.builder()
            .requesterEmail("alice@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Grad apps")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(repository.findById(15L)).thenReturn(java.util.Optional.of(row));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(15L);
    verify(repository, times(1)).delete(row);

    var json = responseToJson(response);
    assertEquals("RecommendationRequests with id 15 deleted", json.get("message"));
  }

  // ---------- BEHAVIOR: DELETE non-existent row returns 404 (ADMIN) ----------

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_delete_nonexistent_returns_404() throws Exception {
    // arrange
    when(repository.findById(99L)).thenReturn(java.util.Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "99").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(99L);
    String body = response.getResponse().getContentAsString();
    org.assertj.core.api.Assertions.assertThat(body)
        .contains("RecommendationRequests with id 99 not found");
  }
}
