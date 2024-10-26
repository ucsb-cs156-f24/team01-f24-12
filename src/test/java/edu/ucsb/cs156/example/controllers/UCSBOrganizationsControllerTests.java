package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationsRepository ucsbOrganizationsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdiningcommons/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganizations?orgcode=ZPR"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsborganizations/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganizations/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganizations organizations = UCSBOrganizations.builder()
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(organizations));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgcode=ZPR"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("ZPR"));
                String expectedJson = mapper.writeValueAsString(organizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgcode=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganizations ZPR = UCSBOrganizations.builder()
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                UCSBOrganizations SKY = UCSBOrganizations.builder()
                                .orgcode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganizations> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(ZPR, SKY));

                when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrganizations);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organizations() throws Exception {
                // arrange

                UCSBOrganizations OSLI = UCSBOrganizations.builder()
                                .orgcode("OSLI")
                                .orgTranslationShort("STUDENT LIFE")
                                .orgTranslation("OFFICE OF STUDENT LIFE")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationsRepository.save(eq(OSLI))).thenReturn(OSLI);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganizations/post?orgcode=OSLI&orgTranslationShort=STUDENT LIFE&orgTranslation=OFFICE OF STUDENT LIFE&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).save(OSLI);
                String expectedJson = mapper.writeValueAsString(OSLI);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                UCSBOrganizations KRC = UCSBOrganizations.builder()
                                .orgcode("KRC")
                                .orgTranslationShort("KOREAN RADIO CL")
                                .orgTranslation("KOREAN RADIO CLUB")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("KRC"))).thenReturn(Optional.of(KRC));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgcode=KRC")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("KRC");
                verify(ucsbOrganizationsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id KRC deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_organizations_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganizations?orgcode=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_organizations() throws Exception {
                // arrange

                UCSBOrganizations ZPR = UCSBOrganizations.builder()
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                UCSBOrganizations ZPREdited = UCSBOrganizations.builder()
                                .orgcode("ZPRR")
                                .orgTranslationShort("ZETA PHI RHOO")
                                .orgTranslation("ZETA PHI RHOO")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(ZPREdited);

                when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(ZPR));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgcode=ZPR")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("ZPR");
                verify(ucsbOrganizationsRepository, times(1)).save(ZPREdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_organizations_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganizations editedOrganizations = UCSBOrganizations.builder()
                                .orgcode("Munger-hall")
                                .orgTranslationShort("AAAZETA PHI RHO")
                                .orgTranslation("AAAAAAZETA PHI RHO")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedOrganizations);

                when(ucsbOrganizationsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganizations?orgcode=munger-hall")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id munger-hall not found", json.get("message"));

        }
}
