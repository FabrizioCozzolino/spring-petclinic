package org.springframework.samples.petclinic.owner;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PetValidator implements Validator {

	private static final String REQUIRED = "required";

	private static final int MAX_NAME_LENGTH = 30;

	// @ private invariant MAX_NAME_LENGTH == 30;

	// @ also
	// @ public behavior
	// @ requires obj instanceof Pet;
	// @ requires errors != null;
	// @ assignable \everything;
	@Override
	public void validate(Object obj, Errors errors) {
		// @ assume obj instanceof Pet;
		Pet pet = (Pet) obj;
		String name = pet.getName();
		// name validation
		if (!StringUtils.hasText(name)) {
			errors.rejectValue("name", REQUIRED, REQUIRED);
		}
		else if (name.length() > MAX_NAME_LENGTH) {
			errors.rejectValue("name", "size", "Name must be no more than 30 characters");
		}

		// type validation
		if (pet.isNew() && pet.getType() == null) {
			errors.rejectValue("type", REQUIRED, REQUIRED);
		}

		// birth date validation
		if (pet.getBirthDate() == null) {
			errors.rejectValue("birthDate", REQUIRED, REQUIRED);
		}
	}

	/**
	 * This Validator validates *just* Pet instances
	 */
	// @ also
	// @ public normal_behavior
	// @ requires clazz != null;
	// @ assignable \nothing;
	// @ ensures \result == Pet.class.isAssignableFrom(clazz);
	@Override
	public boolean supports(Class<?> clazz) {
		return Pet.class.isAssignableFrom(clazz);
	}

}