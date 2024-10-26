package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * This is a REST controller for UCSBOrganizations
 */

@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

    @Autowired
    UCSBOrganizationsRepository ucsbOrganizationsRepository;

    /**
     * THis method returns a list of all ucsborganizations.
     * @return a list of all ucsborganizations
     */
    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganizations> allOrganizations() {
        Iterable<UCSBOrganizations> organization = ucsbOrganizationsRepository.findAll();
        return organization;
    }

    /**
     * This method returns a single UCSBOrganizations.
     * @param orgcode orgcode of the UCSBOrganizations
     * @return a single UCSBOrganizations
     */
    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganizations getById(
            @Parameter(name="orgcode") @RequestParam String orgcode) {
        UCSBOrganizations organization = ucsbOrganizationsRepository.findById(orgcode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgcode));

        return organization;
    }

    /**
     * This method creates a new UCSBOrganizations. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgcode orgcode of the UCSBOrganizations
     * @param orgTranslationShort orgTranslationShort of the UCSBOrganizations
     * @param orgTranslation orgTranslation of the UCSBOrganizations
     * @param inactive whether or not the UCSBOrganizations are inactive
     */
    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganizations postOrganizations(
        @Parameter(name="orgcode") @RequestParam String orgcode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        {
        UCSBOrganizations organization = new UCSBOrganizations();
        organization.setOrgcode(orgcode);
        organization.setOrgTranslationShort(orgTranslationShort);
        organization.setOrgTranslation(orgTranslation);
        organization.setInactive(inactive);

        UCSBOrganizations saveOrganization = ucsbOrganizationsRepository.save(organization);

        return saveOrganization;
    }
    /**
     * Delete a diningcommons. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgcode code of the commons
     * @return a message indiciating the commons was deleted
     */
    @Operation(summary= "Delete a UCSBOrganizations")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrganizations(
            @Parameter(name="orgcode") @RequestParam String orgcode) {
        UCSBOrganizations organization = ucsbOrganizationsRepository.findById(orgcode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgcode));

        ucsbOrganizationsRepository.delete(organization);
        return genericMessage("UCSBOrganizations with id %s deleted".formatted(orgcode));
    }
    /**
     * Update a single diningcommons. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgcode code of the diningcommons
     * @param incoming the new commons contents
     * @return the updated commons object
     */
    @Operation(summary= "Update a single organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganizations updateOrganizations(
            @Parameter(name="orgcode") @RequestParam String orgcode,
            @RequestBody @Valid UCSBOrganizations incoming) {

        UCSBOrganizations organizations = ucsbOrganizationsRepository.findById(orgcode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgcode));


        organizations.setOrgcode(incoming.getOrgcode());  
        organizations.setOrgTranslationShort(incoming.getOrgTranslationShort());
        organizations.setOrgTranslation(incoming.getOrgTranslation());
        organizations.setInactive(incoming.getInactive());

        ucsbOrganizationsRepository.save(organizations);

        return organizations;
    }
}
