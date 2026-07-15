package com.shivansh.org.controller;

import com.shivansh.org.dto.Member;
import com.shivansh.org.exception.MemberNotFoundException;
import com.shivansh.org.exception.ValidationException;
import com.shivansh.org.service.MemberService;
import com.shivansh.org.service.impl.MemberServiceImpl;
import com.shivansh.org.util.InputValidator;

import java.util.List;

/**
 * Controller class for Member-related operations.
 * Handles member registration, authentication, profile management,
 * and admin-level member lookups. Bridges the View and Service layers.
 * 
 * @author Shivansh
 * @version 1.0
 */
public class MemberController {

    private final MemberService memberService;

    public MemberController() {
        this.memberService = new MemberServiceImpl();
    }

    /**
     * Authenticates an admin user with hardcoded credentials.
     * In a production system, this would be backed by a database or LDAP.
     * 
     * @param username the admin username
     * @param password the admin password
     * @return true if credentials match
     */
    public boolean authenticateAdmin(String username, String password) {
        return "admin".equals(username) && "123".equals(password);
    }

    /**
     * Authenticates a library member by email and password.
     * 
     * @param email    the member's email
     * @param password the member's password
     * @return the authenticated Member object, or null if invalid
     * @throws ValidationException if email format is invalid
     */
    public Member authenticateMember(String email, String password) throws ValidationException {
        if (!InputValidator.isValidEmail(email)) {
            throw new ValidationException("Invalid email format.");
        }
        return memberService.login(email, password);
    }

    /**
     * Registers a new library member.
     * 
     * @param firstName      the first name
     * @param lastName       the last name
     * @param email          the email address
     * @param phone          the phone number (optional, can be null)
     * @param password       the password (min 6 characters)
     * @param membershipType the membership type (REGULAR, STUDENT, FACULTY)
     * @return true if registration succeeded
     * @throws ValidationException if any input fails validation
     */
    public boolean registerMember(String firstName, String lastName, String email,
                                  String phone, String password, String membershipType) throws ValidationException {
        if (!InputValidator.isValidName(firstName)) {
            throw new ValidationException("First name must contain only letters and spaces (2-50 chars).");
        }
        if (!InputValidator.isValidName(lastName)) {
            throw new ValidationException("Last name must contain only letters and spaces (2-50 chars).");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new ValidationException("Invalid email address format.");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        Member member = new Member(firstName.trim(), lastName.trim(), email.trim(), password, membershipType);
        if (phone != null && !phone.trim().isEmpty()) {
            member.setPhone(phone.trim());
        }
        return memberService.registerMember(member);
    }

    /**
     * Updates a member's profile information.
     * 
     * @param member the member with updated fields
     * @return true if update succeeded
     * @throws ValidationException if validation fails
     */
    public boolean updateMemberProfile(Member member) throws ValidationException {
        if (member == null) {
            throw new ValidationException("Member data cannot be null.");
        }
        return memberService.updateMember(member);
    }

    /**
     * Retrieves all registered library members.
     * @return list of all members
     */
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    /**
     * Searches for a member by their database ID.
     * @param memberId the member ID
     * @return the Member object
     * @throws MemberNotFoundException if not found
     */
    public Member getMemberById(int memberId) throws MemberNotFoundException {
        Member m = memberService.getMemberById(memberId);
        if (m == null) {
            throw new MemberNotFoundException(memberId);
        }
        return m;
    }

    /**
     * Searches for a member by their email address.
     * @param email the email
     * @return the Member object
     * @throws MemberNotFoundException if not found
     */
    public Member getMemberByEmail(String email) throws MemberNotFoundException {
        Member m = memberService.getMemberByEmail(email);
        if (m == null) {
            throw new MemberNotFoundException("No member found with email: " + email);
        }
        return m;
    }

    /**
     * Deletes a member from the system (admin operation).
     * @param memberId the member ID to delete
     * @return true if deleted successfully
     * @throws MemberNotFoundException if not found
     */
    public boolean deleteMember(int memberId) throws MemberNotFoundException {
        Member m = memberService.getMemberById(memberId);
        if (m == null) {
            throw new MemberNotFoundException(memberId);
        }
        return memberService.deleteMember(memberId);
    }

    /**
     * Refreshes member data from the database (for session updates).
     * @param memberId the member ID
     * @return refreshed Member object, or null
     */
    public Member refreshMember(int memberId) {
        return memberService.getMemberById(memberId);
    }
}
