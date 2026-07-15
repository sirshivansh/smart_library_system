import { supabase } from './supabase'

// Admin credentials are hardcoded (matching the original Java app)
const ADMIN_USER = 'admin'
const ADMIN_PASS = '123'

export async function login(emailOrUsername, password) {
  // Check admin first
  if (emailOrUsername === ADMIN_USER && password === ADMIN_PASS) {
    return {
      role: 'ADMIN',
      username: 'admin',
      name: 'Administrator',
    }
  }

  // Otherwise check members via email
  const { data: member, error } = await supabase
    .from('members')
    .select('*')
    .eq('email', emailOrUsername)
    .maybeSingle()

  if (error) throw error
  if (!member) return null

  // For the demo, we accept the plaintext password 'password123' for seeded members
  // (the original Java app hashes with SHA-256, but for this web demo we do a simple check)
  if (password !== 'password123') return null

  return {
    role: 'MEMBER',
    memberId: member.member_id,
    firstName: member.first_name,
    lastName: member.last_name,
    email: member.email,
    phone: member.phone,
    membershipType: member.membership_type,
    name: `${member.first_name} ${member.last_name}`,
  }
}
