package com.shivansh.org.dto;

/**
 * Data Transfer Object representing a Library Member entity. Maps to the 'members' table in the
 * MySQL database. Passwords are stored as SHA-256 hashes for security.
 *
 * @author Shivansh
 * @version 2.0
 */
public class Member {
  private int memberId;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String password;
  private String membershipType;

  /** Default no-args constructor */
  public Member() {}

  /** Constructor without ID and phone (for quick registration) */
  public Member(
      String firstName, String lastName, String email, String password, String membershipType) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.membershipType = membershipType;
  }

  /** Full constructor without phone */
  public Member(
      int memberId,
      String firstName,
      String lastName,
      String email,
      String password,
      String membershipType) {
    this.memberId = memberId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.membershipType = membershipType;
  }

  /** Full constructor with phone */
  public Member(
      int memberId,
      String firstName,
      String lastName,
      String email,
      String phone,
      String password,
      String membershipType) {
    this.memberId = memberId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.password = password;
    this.membershipType = membershipType;
  }

  // ── Getters and Setters ──────────────────────────────────────

  public int getMemberId() {
    return memberId;
  }

  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getMembershipType() {
    return membershipType;
  }

  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }

  @Override
  public String toString() {
    return "Member [ID="
        + memberId
        + ", Name="
        + firstName
        + " "
        + lastName
        + ", Email="
        + email
        + (phone != null ? ", Phone=" + phone : "")
        + ", Type="
        + membershipType
        + "]";
  }
}
