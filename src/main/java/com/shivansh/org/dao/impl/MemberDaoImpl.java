package com.shivansh.org.dao.impl;

import com.shivansh.org.dao.MemberDao;
import com.shivansh.org.dto.Member;
import com.shivansh.org.util.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDaoImpl implements MemberDao {

  @Override
  public boolean addMember(Member member) {
    String sql =
        "INSERT INTO members (first_name, last_name, email, phone, password, membership_type) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, member.getFirstName());
      ps.setString(2, member.getLastName());
      ps.setString(3, member.getEmail());
      ps.setString(4, member.getPhone());
      ps.setString(5, member.getPassword());
      ps.setString(6, member.getMembershipType());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error adding member: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean updateMember(Member member) {
    String sql =
        "UPDATE members SET first_name = ?, last_name = ?, email = ?, phone = ?, password = ?, membership_type = ? WHERE member_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, member.getFirstName());
      ps.setString(2, member.getLastName());
      ps.setString(3, member.getEmail());
      ps.setString(4, member.getPhone());
      ps.setString(5, member.getPassword());
      ps.setString(6, member.getMembershipType());
      ps.setInt(7, member.getMemberId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error updating member: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean deleteMember(int memberId) {
    String sql = "DELETE FROM members WHERE member_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, memberId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error deleting member: " + e.getMessage());
      return false;
    }
  }

  @Override
  public Member getMemberById(int memberId) {
    String sql = "SELECT * FROM members WHERE member_id = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, memberId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Member(
              rs.getInt("member_id"),
              rs.getString("first_name"),
              rs.getString("last_name"),
              rs.getString("email"),
              rs.getString("phone"),
              rs.getString("password"),
              rs.getString("membership_type"));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching member by ID: " + e.getMessage());
    }
    return null;
  }

  @Override
  public Member getMemberByEmail(String email) {
    String sql = "SELECT * FROM members WHERE email = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Member(
              rs.getInt("member_id"),
              rs.getString("first_name"),
              rs.getString("last_name"),
              rs.getString("email"),
              rs.getString("phone"),
              rs.getString("password"),
              rs.getString("membership_type"));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching member by email: " + e.getMessage());
    }
    return null;
  }

  @Override
  public List<Member> getAllMembers() {
    List<Member> members = new ArrayList<>();
    String sql = "SELECT * FROM members";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        members.add(
            new Member(
                rs.getInt("member_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getString("membership_type")));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching all members: " + e.getMessage());
    }
    return members;
  }

  @Override
  public Member authenticateMember(String email, String password) {
    String sql = "SELECT * FROM members WHERE email = ? AND password = ?";
    try (Connection conn = DbConnection.getMysqlConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      ps.setString(2, password);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Member(
              rs.getInt("member_id"),
              rs.getString("first_name"),
              rs.getString("last_name"),
              rs.getString("email"),
              rs.getString("phone"),
              rs.getString("password"),
              rs.getString("membership_type"));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error authenticating member: " + e.getMessage());
    }
    return null;
  }
}
