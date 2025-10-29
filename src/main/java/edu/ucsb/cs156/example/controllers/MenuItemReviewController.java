package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreviews")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
    return reviews;
  }

  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments)
      throws JsonProcessingException {

    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setComments(comments);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }
}
