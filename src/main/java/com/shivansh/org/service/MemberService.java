package com.shivansh.org.service;

import com.shivansh.org.dto.Member;
import java.util.List;

public interface MemberService {
    boolean registerMember(Member member);
    boolean updateMember(Member member);
    boolean deleteMember(int memberId);
    Member getMemberById(int memberId);
    Member getMemberByEmail(String email);
    List<Member> getAllMembers();
    Member login(String email, String password);
}
