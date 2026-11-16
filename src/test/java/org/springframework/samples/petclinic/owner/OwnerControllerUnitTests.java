package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Isolated unit tests for {@link OwnerController} using Mockito 5.
 */
@ExtendWith(MockitoExtension.class)
class OwnerControllerUnitTests {

	@Mock
	private OwnerRepository owners;

	@Mock
	private BindingResult bindingResult;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@InjectMocks
	private OwnerController controller;

	private Owner sampleOwner;

	@BeforeEach
	void setUp() {
		this.sampleOwner = new Owner();
		this.sampleOwner.setId(1);
		this.sampleOwner.setFirstName("George");
		this.sampleOwner.setLastName("Franklin");
		this.sampleOwner.setAddress("110 W. Liberty St.");
		this.sampleOwner.setCity("Madison");
		this.sampleOwner.setTelephone("6085551023");
	}

	@Test
	void findOwnerShouldReturnNewOwnerWhenIdIsNull() {
		Owner result = this.controller.findOwner(null);
		assertNotNull(result);
		assertNull(result.getId());
		verify(this.owners, never()).findById(any());
	}

	@Test
	void findOwnerShouldThrowExceptionWhenOwnerNotFound() {
		when(this.owners.findById(42)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.controller.findOwner(42));
	}

	@Test
	void processCreationFormShouldReturnFormViewWhenValidationFails() {
		when(this.bindingResult.hasErrors()).thenReturn(true);

		String viewName = this.controller.processCreationForm(this.sampleOwner, this.bindingResult,
				this.redirectAttributes);

		assertEquals("owners/createOrUpdateOwnerForm", viewName);
		verify(this.owners, never()).save(any());
		verify(this.redirectAttributes).addFlashAttribute(eq("error"), any());
	}

	@Test
	void processCreationFormShouldSaveAndRedirectWhenValid() {
		when(this.bindingResult.hasErrors()).thenReturn(false);

		String viewName = this.controller.processCreationForm(this.sampleOwner, this.bindingResult,
				this.redirectAttributes);

		assertEquals("redirect:/owners/1", viewName);
		verify(this.owners).save(this.sampleOwner);
		verify(this.redirectAttributes).addFlashAttribute("message", "New Owner Created");
	}

	@Test
	void processFindFormShouldRejectValueWhenNoOwnersFound() {
		Owner searchCriteria = new Owner();
		searchCriteria.setLastName("  Unknown  ");
		Page<Owner> emptyPage = new PageImpl<>(Collections.emptyList());

		when(this.owners.findByLastNameStartingWith(eq("Unknown"), any(Pageable.class))).thenReturn(emptyPage);

		String viewName = this.controller.processFindForm(1, searchCriteria, this.bindingResult, this.model,
				this.redirectAttributes);

		assertEquals("owners/findOwners", viewName);
		verify(this.bindingResult).rejectValue("lastName", "notFound", "not found");
	}

	@Test
	void processFindFormShouldRedirectDirectlyWhenSingleOwnerFound() {
		Owner searchCriteria = new Owner();
		searchCriteria.setLastName(null);
		Page<Owner> singleResult = new PageImpl<>(List.of(this.sampleOwner));

		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(singleResult);

		String viewName = this.controller.processFindForm(1, searchCriteria, this.bindingResult, this.model,
				this.redirectAttributes);

		assertEquals("redirect:/owners/1", viewName);
	}

	@Test
	void processFindFormShouldPopulatePaginationWhenMultipleOwnersFound() {
		Owner secondOwner = new Owner();
		secondOwner.setId(2);
		secondOwner.setLastName("Davis");

		Owner searchCriteria = new Owner();
		Page<Owner> multipleResults = new PageImpl<>(List.of(this.sampleOwner, secondOwner));

		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(multipleResults);

		String viewName = this.controller.processFindForm(1, searchCriteria, this.bindingResult, this.model,
				this.redirectAttributes);

		assertEquals("owners/ownersList", viewName);
		verify(this.model).addAttribute("currentPage", 1);
		verify(this.model).addAttribute("totalPages", 1);
		verify(this.model).addAttribute("totalItems", 2L);
	}

	@Test
	void processFindFormShouldRedirectToFirstPageWhenPageIndexOutOfBounds() {
		Owner searchCriteria = new Owner();
		searchCriteria.setLastName("Franklin");
		Page<Owner> singlePage = new PageImpl<>(List.of(this.sampleOwner));

		when(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).thenReturn(singlePage);

		String viewName = this.controller.processFindForm(5, searchCriteria, this.bindingResult, this.model,
				this.redirectAttributes);

		assertEquals("redirect:/owners", viewName);
		verify(this.redirectAttributes).addAttribute("page", 1);
		verify(this.redirectAttributes).addAttribute("lastName", "Franklin");
	}

	@Test
	void processUpdateOwnerFormShouldRejectWhenPathIdMismatchesFormId() {
		when(this.bindingResult.hasErrors()).thenReturn(false);
		this.sampleOwner.setId(99);

		String viewName = this.controller.processUpdateOwnerForm(this.sampleOwner, this.bindingResult, 1,
				this.redirectAttributes);

		assertEquals("redirect:/owners/{ownerId}/edit", viewName);
		verify(this.bindingResult).rejectValue(eq("id"), eq("mismatch"), any());
		verify(this.owners, never()).save(any());
	}

	@Test
	void processUpdateOwnerFormShouldSaveAndRedirectWhenValid() {
		when(this.bindingResult.hasErrors()).thenReturn(false);

		String viewName = this.controller.processUpdateOwnerForm(this.sampleOwner, this.bindingResult, 1,
				this.redirectAttributes);

		assertEquals("redirect:/owners/{ownerId}", viewName);
		verify(this.owners).save(this.sampleOwner);
		verify(this.redirectAttributes).addFlashAttribute("message", "Owner Values Updated");
	}

	@Test
	void showOwnerShouldReturnModelAndViewWhenOwnerExists() {
		when(this.owners.findById(1)).thenReturn(Optional.of(this.sampleOwner));

		ModelAndView mav = this.controller.showOwner(1);

		assertEquals("owners/ownerDetails", mav.getViewName());
		assertEquals(this.sampleOwner, mav.getModel().get("owner"));
	}

	@Test
	void showOwnerShouldThrowExceptionWhenOwnerDoesNotExist() {
		when(this.owners.findById(99)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.controller.showOwner(99));
	}

}