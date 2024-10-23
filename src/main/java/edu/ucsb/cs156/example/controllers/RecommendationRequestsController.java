package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import edu.ucsb.cs156.example.entities.RecommendationRequest;

import java.time.LocalDateTime;
@Tag(name = "recommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {
    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    @Operation(summary= "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> reqs = recommendationRequestRepository.findAll();
        return reqs;
    }

    @Operation(summary= "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="professorEmail") @RequestParam String professorEmail,
            @Parameter(name="explanation") @RequestParam String explanation,
            
            @Parameter(name="dateRequested", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
            @RequestParam("dateRequested") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime dateRequested, 
            
            @Parameter(name="dateNeeded", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
            @RequestParam("dateNeeded") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime dateNeeded,

            @Parameter(name="doneBool") @RequestParam boolean doneBool
            )

            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("localDateTime1={}", dateRequested);
        log.info("localDateTime2={}", dateNeeded);

        RecommendationRequest req = new RecommendationRequest();
        req.setRequesterEmail(requesterEmail);
        req.setProfessorEmail(professorEmail);
        req.setExplanation(explanation);
        req.setDateRequested(dateRequested);
        req.setDateNeeded(dateNeeded);
        req.setDone(doneBool);
        RecommendationRequest savedReq = recommendationRequestRepository.save(req);

        return savedReq;
    }

    /**
     * Get a single request by id
     * 
     * @param id the id of the request
     * @return a RecommendationRequest
     */
    @Operation(summary= "Get a single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest req = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        return req;
    }

    /**
     * Delete a RecommendationRequest
     * 
     * @param id the id of the req
     * @return a message indicating the req was deleted
     */
    @Operation(summary= "Delete a RecommendationRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest req = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequestRepository.delete(req);
        return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
    }

    /**
     * Update a single recommendation request
     * 
     * @param id       id of the req to update
     * @param incoming the new req
     * @return the updated req object
     */
    @Operation(summary= "Update a single RecommendationRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequest updateRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest incoming) {

        RecommendationRequest req = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        req.setDateNeeded(incoming.getDateNeeded());
        req.setDateRequested(incoming.getDateRequested());
        req.setDone(incoming.getDone());
        req.setProfessorEmail(incoming.getProfessorEmail());
        req.setRequesterEmail(incoming.getRequesterEmail());
        req.setExplanation(incoming.getExplanation());

        recommendationRequestRepository.save(req);

        return req;
    }
    
}
