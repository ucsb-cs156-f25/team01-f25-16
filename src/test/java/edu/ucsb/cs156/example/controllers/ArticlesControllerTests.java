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
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockBean ArticlesRepository articlesRepository;

  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/articles?id=21")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/articles/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/articles/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-02-03T00:00:00");

    Articles article1 =
        Articles.builder()
            .title("Article 1")
            .url("https://example.com/1")
            .explanation("Explains 1")
            .submitterEmail("one@example.com")
            .dateAdded(ldt1)
            .build();

    Articles article2 =
        Articles.builder()
            .title("Article 2")
            .url("https://example.com/2")
            .explanation("Explains 2")
            .submitterEmail("two@example.com")
            .dateAdded(ldt2)
            .build();

    List<Articles> expectedArticles = new ArrayList<>(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {

    LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles article =
        Articles.builder()
            .title("Article 1")
            .url("https://example.com/1")
            .explanation("Explains 1")
            .submitterEmail("one@example.com")
            .dateAdded(ldt)
            .build();

    when(articlesRepository.save(
            Articles.builder()
                .title("Article 1")
                .url("https://example.com/1")
                .explanation("Explains 1")
                .submitterEmail("one@example.com")
                .dateAdded(ldt)
                .build()))
        .thenReturn(article);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post?title=Article 1&url=https://example.com/1&explanation=Explains 1&submitterEmail=one@example.com&dateAdded=2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1))
        .save(
            Articles.builder()
                .title("Article 1")
                .url("https://example.com/1")
                .explanation("Explains 1")
                .submitterEmail("one@example.com")
                .dateAdded(ldt)
                .build());

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_article_by_id() throws Exception {

    LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

    Articles article =
        Articles.builder()
            .title("Article 1")
            .url("https://example.com/1")
            .explanation("Explains 1")
            .submitterEmail("one@example.com")
            .dateAdded(ldt)
            .build();

    when(articlesRepository.findById(eq(21L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc.perform(get("/api/articles?id=21")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findById(eq(21L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_when_article_not_found() throws Exception {

    when(articlesRepository.findById(eq(21L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc.perform(get("/api/articles?id=21")).andExpect(status().isNotFound()).andReturn();

    verify(articlesRepository, times(1)).findById(eq(21L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 21 not found", json.get("message"));
  }
}
