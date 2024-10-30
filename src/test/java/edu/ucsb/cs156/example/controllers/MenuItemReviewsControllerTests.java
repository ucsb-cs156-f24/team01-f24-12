package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReviews;
import edu.ucsb.cs156.example.repositories.MenuItemsReviewsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MenuItemReviewsController.class)
@Import(TestConfig.class)
public class MenuItemReviewsControllerTests extends ControllerTestCase {

    @MockBean
    MenuItemsReviewsRepository menuItemsReviewsRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/menuitemreviews/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/menuitemreviews/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/menuitemreviews/all"))
                .andExpect(status().isOk()); // logged in users can get all
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/menuitemreviews?id=7"))
                .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/menuitemreviews/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/menuitemreviews/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/menuitemreviews/post"))
                .andExpect(status().is(403));    
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2023-10-29T12:00:00");
        MenuItemReviews menuItemReview = MenuItemReviews.builder()
                                        .itemId(1L)
                                        .reviewerEmail("s@mail.com")
                                        .stars(5)
                                        .dateReviewed(ldt)
                                        .comments("Awesome!")
                                        .build();

        when(menuItemsReviewsRepository.save(any())).thenReturn(menuItemReview);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/menuitemreviews/post?itemId=1&reviewerEmail=s@mail.com&stars=5&dateReviewed=2023-10-29T12:00:00&comments=Awesome!")
                                .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).save(eq(menuItemReview));
        String expectedJson = mapper.writeValueAsString(menuItemReview);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }





    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all_menuitemreviews() throws Exception {
        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2023-10-29T12:00:00");
        MenuItemReviews review1 = MenuItemReviews.builder()
                                        .itemId(1L)
                                        .reviewerEmail("j@mail.com")
                                        .stars(4)
                                        .dateReviewed(ldt1)
                                        .comments("Good item.")
                                        .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2023-10-30T13:00:00");
        MenuItemReviews review2 = MenuItemReviews.builder()
                                        .itemId(2L)
                                        .reviewerEmail("e@mail.com")
                                        .stars(5)
                                        .dateReviewed(ldt2)
                                        .comments("Excellent!")
                                        .build();

        ArrayList<MenuItemReviews> expectedReviews = new ArrayList<>();
        expectedReviews.addAll(Arrays.asList(review1, review2));

        when(menuItemsReviewsRepository.findAll()).thenReturn(expectedReviews);

        // act
        MvcResult response = mockMvc.perform(get("/api/menuitemreviews/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedReviews);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = {"USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2023-10-29T12:00:00");
        MenuItemReviews review = MenuItemReviews.builder()
                                    .itemId(1L)
                                    .reviewerEmail("user@mail.com")
                                    .stars(5)
                                    .dateReviewed(ldt)
                                    .comments("Awesome!")
                                    .build();

        when(menuItemsReviewsRepository.findById(eq(7L))).thenReturn(Optional.of(review));

        // act
        MvcResult response = mockMvc.perform(get("/api/menuitemreviews?id=7"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(review);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        // arrange
        when(menuItemsReviewsRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/menuitemreviews?id=7"))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReviews with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_menuitemreview() throws Exception {
        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2023-10-29T12:00:00");
        MenuItemReviews review = MenuItemReviews.builder()
                                    .id(15L)
                                    .itemId(1L)
                                    .reviewerEmail("admin@mail.com")
                                    .stars(5)
                                    .dateReviewed(ldt)
                                    .comments("Perfect!")
                                    .build();

        when(menuItemsReviewsRepository.findById(eq(15L))).thenReturn(Optional.of(review));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/menuitemreviews?id=15")
                                .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(15L);
        verify(menuItemsReviewsRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReview with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_menuitemreview_and_gets_right_error_message()
        throws Exception {
        // arrange
        when(menuItemsReviewsRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/menuitemreviews?id=15")
                                .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReviews with id 15 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_menuitemreview() throws Exception {
        // arrange
        LocalDateTime originalLdt = LocalDateTime.parse("2023-10-29T12:00:00");
        LocalDateTime editedLdt = LocalDateTime.parse("2024-01-01T00:00:00");

        MenuItemReviews originalReview = MenuItemReviews.builder()
                                        .id(54L)
                                        .itemId(1L)
                                        .reviewerEmail("svt@mail.com")
                                        .stars(3)
                                        .dateReviewed(originalLdt)
                                        .comments("Average.")
                                        .build();

        MenuItemReviews editedReview = MenuItemReviews.builder()
                                        .id(54L)
                                        .itemId(2L)
                                        .reviewerEmail("svsv@mail.com")
                                        .stars(4)
                                        .dateReviewed(editedLdt)
                                        .comments("Good.")
                                        .build();

        String requestBody = mapper.writeValueAsString(editedReview);

        when(menuItemsReviewsRepository.findById(eq(54L))).thenReturn(Optional.of(originalReview));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/menuitemreviews?id=67")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(54L);
        verify(menuItemsReviewsRepository, times(1)).save(editedReview);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2023-10-29T12:00:00");
        MenuItemReviews editedReview = MenuItemReviews.builder()
                                        .id(54L)
                                        .itemId(2L)
                                        .reviewerEmail("nonexistent@mail.com")
                                        .stars(2)
                                        .dateReviewed(ldt)
                                        .comments("Bad.")
                                        .build();

        String requestBody = mapper.writeValueAsString(editedReview);

        when(menuItemsReviewsRepository.findById(eq(54L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/menuitemreviews?id=67")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(menuItemsReviewsRepository, times(1)).findById(54L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReviews with id 67 not found", json.get("message"));
    }
}