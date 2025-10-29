package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Help Requests */
@Tag(name = "HelpRequests")
@RequestMapping("/api/helprequests")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * THis method returns a list of all help requests.
   *
   * @return a list of all help requests
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> requests = helpRequestRepository.findAll();
    return requests;
  }

  /**
   * This method creates a new help request. Accessible only to users with the role "ROLE_ADMIN".
   * creating params based on these:
   *
   * @param requesterEmail email of the requester
   * @param teamId team ID of the requester
   * @param tableOrBreakoutRoom table or breakout room of the requester
   * @param requestTime time of the request
   * @param explanation explanation of the request
   * @param solved whether the request is solved
   * @return the created help request
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(name = "requestTime")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved) {

    log.info("requestTime={}", requestTime);

    HelpRequest request = new HelpRequest();
    request.setRequesterEmail(requesterEmail);
    request.setTeamId(teamId);
    request.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    request.setRequestTime(requestTime);
    request.setExplanation(explanation);
    request.setSolved(solved);

    HelpRequest savedRequest = helpRequestRepository.save(request);

    return savedRequest;
  }

  /**
   * This method returns a single help request.
   *
   * @param id id of the help request
   * @return a single help request
   */
  @Operation(summary = "Get a single help request by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest request =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    return request;
  }

  /**
   * Delete a help request. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id id of the help request
   * @return a message indicating the help request was deleted
   */
  @Operation(summary = "Delete a HelpRequest")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteHelpRequest(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest request =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    helpRequestRepository.delete(request);
    return genericMessage("HelpRequest with id %s deleted".formatted(id));
  }
}
