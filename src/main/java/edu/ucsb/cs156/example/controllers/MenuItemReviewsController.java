package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReviews;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemsReviewsRepository;
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

import java.time.LocalDateTime;


@Tag(name = "MenuItemReviews")
@RequestMapping("/api/menuitemreviews")
@RestController
@Slf4j
public class MenuItemReviewsController extends ApiController {
    //please take into account id is the primary key of the entity and then the other fields

    @Autowired
    MenuItemsReviewsRepository menuItemsReviewsRepository;

    /**
     * List all MenuItemReviews
     * 
     * @return an iterable of MenuItemReviews
     */
    @Operation(summary= "List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReviews> allMenuItemReviews() {
        Iterable<MenuItemReviews> reviews = menuItemsReviewsRepository.findAll();
        return reviews;
    }

    @Operation(summary= "Create a new menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")

    //Create a new menu item review
    public MenuItemReviews postMenuItemReview(
            @Parameter(name="itemId") @RequestParam long itemId,
            @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
            @Parameter(name="stars") @RequestParam int stars,
            @Parameter(name="dateReviewed", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
            @Parameter(name="comments") @RequestParam String comments)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateReviewed={}", dateReviewed);

        MenuItemReviews menuItemReview = new MenuItemReviews();
        menuItemReview.setItemId(itemId);
        menuItemReview.setReviewerEmail(reviewerEmail);
        menuItemReview.setStars(stars);
        menuItemReview.setDateReviewed(dateReviewed);
        menuItemReview.setComments(comments);

        MenuItemReviews savedMenuItemReview = menuItemsReviewsRepository.save(menuItemReview);

        return savedMenuItemReview;
    }

    @Operation(summary="get a single menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")

    //Get a single menu item review by id
    public MenuItemReviews getById(
            @Parameter(name="id") @RequestParam Long id) {
        MenuItemReviews menuItemReview = menuItemsReviewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));

        return menuItemReview;
    }

    @Operation(summary="Delete a menu item review by id")
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("")

    //Delete a menu item review by id(not itemId)

    public Object deleteMenuItemReview(
            @Parameter(name="id") @RequestParam Long id) {
        MenuItemReviews menuItemReview = menuItemsReviewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));

        menuItemsReviewsRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    @Operation(summary="Update a single menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("")

    //Update a single menu item review by id
    public MenuItemReviews updateMenuItemReview(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReviews incoming) {

        MenuItemReviews menuItemReview = menuItemsReviewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));

        menuItemReview.setItemId(incoming.getItemId());
        menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
        menuItemReview.setStars(incoming.getStars());
        menuItemReview.setDateReviewed(incoming.getDateReviewed());
        menuItemReview.setComments(incoming.getComments());

        menuItemsReviewsRepository.save(menuItemReview);

        return menuItemReview;
    }
}
