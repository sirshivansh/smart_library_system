package com.shivansh.org.service.impl;

import com.shivansh.org.dao.MemberDao;
import com.shivansh.org.dao.impl.MemberDaoImpl;
import com.shivansh.org.dto.Member;
import com.shivansh.org.service.MemberService;
import com.shivansh.org.util.InputValidator;
import com.shivansh.org.util.PasswordUtil;

import java.util.List;

public class MemberServiceImpl implements MemberService {
    private final MemberDao memberDao = new MemberDaoImpl();

    @Override
    public boolean registerMember(Member member) {
        if (member.getEmail() == null || !InputValidator.isValidEmail(member.getEmail())) {
            System.out.println("Invalid email address format!");
            return false;
        }
        if (member.getPassword() == null || !InputValidator.isValidPassword(member.getPassword())) {
            System.out.println("Password must be at least 6 characters long!");
            return false;
        }
        if (!InputValidator.isValidName(member.getFirstName()) || !InputValidator.isValidName(member.getLastName())) {
            System.out.println("First name and Last name must contain only letters and spaces (2-50 chars)!");
            return false;
        }
        
        // Check if email already registered
        if (memberDao.getMemberByEmail(member.getEmail()) != null) {
            System.out.println("Email is already registered!");
            return false;
        }

        // Hash password before saving to DB
        member.setPassword(PasswordUtil.hashPassword(member.getPassword()));
        return memberDao.addMember(member);
    }

    @Override
    public boolean updateMember(Member member) {
        Member existing = memberDao.getMemberById(member.getMemberId());
        if (existing != null) {
            // Check if password has been changed (if different from current hash)
            if (!member.getPassword().equals(existing.getPassword())) {
                if (!InputValidator.isValidPassword(member.getPassword())) {
                    System.out.println("Password must be at least 6 characters long!");
                    return false;
                }
                member.setPassword(PasswordUtil.hashPassword(member.getPassword()));
            }
        }

        if (!InputValidator.isValidName(member.getFirstName()) || !InputValidator.isValidName(member.getLastName())) {
            System.out.println("First name and Last name must contain only letters and spaces (2-50 chars)!");
            return false;
        }

        if (member.getEmail() == null || !InputValidator.isValidEmail(member.getEmail())) {
            System.out.println("Invalid email address format!");
            return false;
        }

        return memberDao.updateMember(member);
    }

    @Override
    public boolean deleteMember(int memberId) {
        return memberDao.deleteMember(memberId);
    }

    @Override
    public Member getMemberById(int memberId) {
        return memberDao.getMemberById(memberId);
    }

    @Override
    public Member getMemberByEmail(String email) {
        return memberDao.getMemberByEmail(email);
    }

    @Override
    public List<Member> getAllMembers() {
        return memberDao.getAllMembers();
    }

    @Override
    public Member login(String email, String password) {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return memberDao.authenticateMember(email, hashedPassword);
    }
}
