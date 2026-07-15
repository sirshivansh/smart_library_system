const LOGIN_URL = 'http://localhost:8080/api/login';

export async function login(emailOrUsername, password) {
  try {
    const response = await fetch(LOGIN_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: emailOrUsername,
        password: password,
      }),
    });

    if (response.status === 401) {
      return null;
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error || 'Authentication failed');
    }

    const data = await response.json();
    return data.user;
  } catch (err) {
    console.error('Login request failed:', err);
    throw err;
  }
}
