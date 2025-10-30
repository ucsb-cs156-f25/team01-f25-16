package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

  @Autowired RecommendationRequestsRepository repository;

  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequests> all() {
    return repository.findAll();
  }

  // pls work
  @Operation(summary = "Get a single RecommendationRequest by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequests getById(@Parameter(name = "id") @RequestParam Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));
  }

  @Operation(summary = "Update a RecommendationRequest by id")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequests updateById(
      @RequestParam Long id, @RequestBody RecommendationRequests incoming) {
    repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

    // Use the incoming payload directly
    incoming.setId(id);
    return repository.save(incoming);
  }

  @Operation(
      summary = "Delete a single RecommendationRequests row by id",
      description =
          "Requires ADMIN. If the id exists, delete it and return a confirmation message; otherwise 404.")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("")
  public Map<String, String> deleteRecommendationRequestById(
      @Parameter(name = "id", description = "Primary key of the RecommendationRequests row")
          @RequestParam
          Long id) {

    RecommendationRequests existing =
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

    repository.delete(existing);

    return Map.of("message", String.format("RecommendationRequests with id %d deleted", id));
  }

  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequests post(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested", description = "ISO date-time, e.g. 2025-10-28T12:34:56")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(name = "dateNeeded", description = "ISO date-time, e.g. 2025-11-05T17:00:00")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done) {
    RecommendationRequests rr =
        RecommendationRequests.builder()
            .requesterEmail(requesterEmail)
            .professorEmail(professorEmail)
            .explanation(explanation)
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(done)
            .build();

    return repository.save(rr);
  }
}
