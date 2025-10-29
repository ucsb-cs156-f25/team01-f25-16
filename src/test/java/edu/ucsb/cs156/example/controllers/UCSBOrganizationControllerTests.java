package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganizations/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/ucsborganizations/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganizations/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganizations/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // Authorization tests for GET /api/ucsborganizations?orgCode=...

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations?orgCode=GSAC"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for PUT /api/ucsborganizations...

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc.perform(put("/api/ucsborganizations?orgCode=GSAC")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    mockMvc.perform(put("/api/ucsborganizations?orgCode=GSAC")).andExpect(status().is(403));
  }

  // Authorization tests for DELETE /api/ucsborganizations...

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/ucsborganizations?orgCode=GSAC")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/ucsborganizations?orgCode=GSAC")).andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  // Tests for GET /api/ucsborganizations/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    // arrange

    UCSBOrganization fencing =
        UCSBOrganization.builder()
            .orgCode("FCSB")
            .orgTranslationShort("Fencing Club")
            .orgTranslation("UCSB Fencing Club")
            .inactive(true)
            .build();

    UCSBOrganization gsac =
        UCSBOrganization.builder()
            .orgCode("GSAC")
            .orgTranslationShort("Gaucho Sports Analytics")
            .orgTranslation("Gaucho Sports Analytics Club")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(fencing, gsac));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for POST /api/ucsborganizations...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization fencing =
        UCSBOrganization.builder()
            .orgCode("FCSB")
            .orgTranslationShort("Fencing Club")
            .orgTranslation("UCSB Fencing Club")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(fencing))).thenReturn(fencing);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganizations/post?orgCode=FCSB&orgTranslationShort=Fencing Club&orgTranslation=UCSB Fencing Club&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(fencing);
    String expectedJson = mapper.writeValueAsString(fencing);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /api/ucsborganizations?orgCode=...

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    // arrange

    UCSBOrganization gsac_ucsb =
        UCSBOrganization.builder()
            .orgCode("GSAC")
            .orgTranslationShort("Gaucho Sports Analytics")
            .orgTranslation("Gaucho Sports Analytics Club")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("GSAC"))).thenReturn(Optional.of(gsac_ucsb));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganizations?orgCode=GSAC"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("GSAC");
    String expectedJson = mapper.writeValueAsString(gsac_ucsb);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("NOTEXIST"))).thenReturn(Optional.empty());

    // act
    mockMvc
        .perform(get("/api/ucsborganizations?orgCode=NOTEXIST"))
        .andExpect(status().isNotFound());
  }

  // Tests for PUT /api/ucsborganizations...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization orgOrig =
        UCSBOrganization.builder()
            .orgCode("GSAC")
            .orgTranslationShort("Gaucho Sports Analytics")
            .orgTranslation("Gaucho Sports Analytics Club")
            .inactive(false)
            .build();

    UCSBOrganization orgEdited =
        UCSBOrganization.builder()
            .orgCode("GSAC")
            .orgTranslationShort("Gaucho Sports Analytics Updated")
            .orgTranslation("Gaucho Sports Analytics Club Updated")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(orgEdited);

    when(ucsbOrganizationRepository.findById(eq("GSAC"))).thenReturn(Optional.of(orgOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganizations?orgCode=GSAC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("GSAC");
    verify(ucsbOrganizationRepository, times(1)).save(orgEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrg =
        UCSBOrganization.builder()
            .orgCode("NOTEXIST")
            .orgTranslationShort("Nonexistent Club")
            .orgTranslation("Nonexistent Club")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrg);

    when(ucsbOrganizationRepository.findById(eq("NOTEXIST"))).thenReturn(Optional.empty());

    // act
    mockMvc
        .perform(
            put("/api/ucsborganizations?orgCode=NOTEXIST")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(requestBody)
                .with(csrf()))
        .andExpect(status().isNotFound());
  }

  // Tests for DELETE /api/ucsborganizations...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization gsac =
        UCSBOrganization.builder()
            .orgCode("GSAC")
            .orgTranslationShort("Gaucho Sports Analytics")
            .orgTranslation("Gaucho Sports Analytics Club")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("GSAC"))).thenReturn(Optional.of(gsac));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganizations?orgCode=GSAC").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("GSAC");
    verify(ucsbOrganizationRepository, times(1)).delete(gsac);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id GSAC deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_organization_that_does_not_exist() throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("NOTEXIST"))).thenReturn(Optional.empty());

    // act
    mockMvc
        .perform(delete("/api/ucsborganizations?orgCode=NOTEXIST").with(csrf()))
        .andExpect(status().isNotFound());
  }
}
