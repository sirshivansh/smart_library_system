package com.shivansh.org.dao;

import com.shivansh.org.dto.Member;
import java.util.List;

public interface MemberDao {
    boolean addMember(Member member);
    boolean updateMember(Member member);
    boolean deleteMember(int memberId);
    Member getMemberById(int memberId);
    Member getMemberByEmail(String email);
    List<Member> getAllMembers();
    Member authenticateMember(String email, String password);
}
