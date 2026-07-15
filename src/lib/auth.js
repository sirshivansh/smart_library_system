import { supabase } from './supabase'
import * as localDb from './localStorageDb'

// Check if Supabase env credentials are configured and not placeholders
const isSupabaseConfigured = (() => {
  try {
    const url = import.meta.env.VITE_SUPABASE_URL;
    const key = import.meta.env.VITE_SUPABASE_ANON_KEY;
    return url && url !== 'YOUR_SUPABASE_URL' && url.trim() !== '' &&
           key && key !== 'YOUR_SUPABASE_ANON_KEY' && key.trim() !== '';
  } catch (e) {
    return false;
  }
})();

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

  let member = null;

  if (!isSupabaseConfigured) {
    member = localDb.dbLogin(emailOrUsername, password);
  } else {
    // Otherwise check members via email
    const { data, error } = await supabase
      .from('members')
      .select('*')
      .eq('email', emailOrUsername)
      .maybeSingle()

    if (error) throw error
    member = data;
  }

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
