package edu.ucsb.cs156.example.controllers;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.entities.RecommendationRequest;


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

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {
    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/recommendationrequests/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all_recommendation_requests() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendation_requests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                .professorEmail("javin")
                                .requesterEmail("alsojavin")
                                .explanation("JUST DO IT ALREADY!")
                                .dateNeeded(ldt1)
                                .dateRequested(ldt1)
                                .done(false)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2024-01-03T00:00:00");

                RecommendationRequest req2 = RecommendationRequest.builder()
                                .professorEmail("john doe")
                                .requesterEmail("jane doe")
                                .explanation("pwease?!")
                                .dateNeeded(ldt2)
                                .dateRequested(ldt2)
                                .done(false)
                                .build();

                ArrayList<RecommendationRequest> expectedReqs = new ArrayList<>();
                expectedReqs.addAll(Arrays.asList(req1, req2));

                when(recommendationRequestRepository.findAll()).thenReturn(expectedReqs);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedReqs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

    // Authorization tests for /api/recommendationrequests/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                .professorEmail("javin")
                                .requesterEmail("alsojavin")
                                .explanation("JUSTDOITALREADY")
                                .dateNeeded(ldt1)
                                .dateRequested(ldt1)
                                .done(false)
                                .build();

                when(recommendationRequestRepository.save(eq(req1))).thenReturn(req1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/recommendationrequests/post?professorEmail=javin&requesterEmail=alsojavin&explanation=JUSTDOITALREADY&dateNeeded=2022-01-03T00:00:00&dateRequested=2022-01-03T00:00:00&doneBool=false")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).save(req1);
                String expectedJson = mapper.writeValueAsString(req1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}
