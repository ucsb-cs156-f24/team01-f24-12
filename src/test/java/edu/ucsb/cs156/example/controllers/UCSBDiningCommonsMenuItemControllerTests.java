package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

    @MockBean
        UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
        UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        // Arrange: Create some sample menu items to be returned by the mock repository
        UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("DLG")
                .name("DeLaGuerra")
                .station("Grill")
                .build();
    
        UCSBDiningCommonsMenuItem item2 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("Portola")
                .name("Pasta")
                .station("Italian")
                .build();
    
        ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>(Arrays.asList(item1, item2));
    
        when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);
    
        // Act: Perform the GET request
        MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                .andExpect(status().isOk())
                .andReturn();
    
        // Assert: Check that the returned data matches the expected items
        String expectedJson = mapper.writeValueAsString(expectedItems);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Authorization tests for /api/ucsbdates/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenuitem() throws Exception {
            // arrange

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            UCSBDiningCommonsMenuItem ucsbMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                            .diningCommonsCode("DLG")
                            .name("DeLaGuerra")
                            .station("Grill")
                            .build();

            when(ucsbDiningCommonsMenuItemRepository.save(eq(ucsbMenuItem1))).thenReturn(ucsbMenuItem1);

            // act 
            MvcResult response = mockMvc.perform(
                            post("/api/ucsbdiningcommonsmenuitem/post?diningCommonsCode=DLG&name=DeLaGuerra&station=Grill&localDateTime=2022-01-03T00:00:00")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbMenuItem1);
            String expectedJson = mapper.writeValueAsString(ucsbMenuItem1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }
}
