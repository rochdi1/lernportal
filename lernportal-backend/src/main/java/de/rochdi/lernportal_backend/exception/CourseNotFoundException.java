package de.rochdi.lernportal_backend.exception;


/**
 * CourseNotFoundException
 */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String message) {
        super(message);
    }
    
}
