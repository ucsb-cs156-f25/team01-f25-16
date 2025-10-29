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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {
  @MockBean UCSBDiningCommonsMenuItemRepository repository;

  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
        .andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitems/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsbdiningcommonsmenuitems/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitems() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("tikkamasla")
            .name("Tikka Masala")
            .station("International")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
    expectedItems.add(item1);

    when(repository.findAll()).thenReturn(expectedItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(repository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("chicken")
            .name("Chicken")
            .station("Protein")
            .build();

    when(repository.save(eq(item))).thenReturn(item);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitems/post?diningCommonsCode=chicken&name=Chicken&station=Protein")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).save(item);
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitems?id=1"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("chicken")
            .name("Chicken")
            .station("Protein")
            .build();

    when(repository.findById(eq(1L))).thenReturn(Optional.of(item));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems?id=1"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(repository, times(1)).findById(eq(1L));
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(repository.findById(eq(2L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitems?id=2"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(repository, times(1)).findById(eq(2L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 2 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem chickenOrig =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .name("Chicken")
            .diningCommonsCode("chicken")
            .station("Protein")
            .build();

    UCSBDiningCommonsMenuItem chickenEdited =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .name("Grilled Chicken")
            .diningCommonsCode("grilledchicken")
            .station("Proteins")
            .build();

    String requestBody = mapper.writeValueAsString(chickenEdited);

    when(repository.findById(eq(1L))).thenReturn(Optional.of(chickenOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitems?id=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(1L);
    verify(repository, times(1)).save(chickenEdited); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menuitem_that_does_not_exist() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem chickenEdited =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .name("Grilled Chicken")
            .diningCommonsCode("grilledchicken")
            .station("Proteins")
            .build();

    String requestBody = mapper.writeValueAsString(chickenEdited);

    when(repository.findById(eq(1L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitems?id=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(1L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_date() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem chicken =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("chicken")
            .name("Chicken")
            .station("Protein")
            .build();

    when(repository.findById(eq(1L))).thenReturn(Optional.of(chicken));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitems?id=1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(1L);
    verify(repository, times(1)).delete(chicken);

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(repository.findById(eq(2L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitems?id=2").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(repository, times(1)).findById(2L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 2 not found", json.get("message"));
  }
}
