import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts';

// Test credentials to display
const TEST_CREDENTIALS = [
  { username: 'user1', password: 'password', label: '👤 Test User 1' },
  { username: 'user2', password: 'password', label: '👤 Test User 2' },
  { username: 'user3', password: 'password', label: '👤 Test User 3' },
  { username: 'user4', password: 'password', label: '👤 Test User 4' },
  { username: 'admin', password: 'password', label: '👑 Admin User' },
];

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login({ username, password });
      
      // Redirect to return URL or home
      const returnUrl = searchParams.get('returnUrl') || '/';
      navigate(returnUrl);
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  const fillCredentials = (cred: typeof TEST_CREDENTIALS[0]) => {
    setUsername(cred.username);
    setPassword(cred.password);
  };

  return (
    <div className="min-h-screen flex">
      {/* LEFT SIDE - Test Credentials */}
      <div className="hidden lg:flex lg:w-2/5 bg-gradient-to-br from-secondary-100 to-secondary-50 p-12 flex-col justify-between">
        <div>
          <h2 className="text-3xl font-bold text-text-primary mb-2">
            Test Accounts
          </h2>
          <p className="text-text-secondary mb-8">
            Click any account to auto-fill the login form
          </p>

          <div className="space-y-3">
            {TEST_CREDENTIALS.map((cred, index) => (
              <button
                key={index}
                onClick={() => fillCredentials(cred)}
                className="w-full text-left p-4 bg-white rounded-lg border border-secondary-200 hover:border-accent-600 hover:shadow-md transition-all group"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <div className="font-semibold text-text-primary group-hover:text-accent-600 transition-colors">
                      {cred.label}
                    </div>
                    <div className="text-sm text-text-secondary mt-1 font-mono">
                      {cred.username} / {cred.password}
                    </div>
                  </div>
                  <div className="text-accent-600 opacity-0 group-hover:opacity-100 transition-opacity">
                    →
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Made by JK */}
        <div className="flex items-center gap-2 text-text-secondary">
          <span className="text-2xl">✨</span>
          <span className="text-lg font-semibold">made by JK</span>
        </div>
      </div>

      {/* RIGHT SIDE - Login Form */}
      <div className="flex-1 flex items-center justify-center p-8 bg-primary-50">
        <div className="w-full max-w-md">
          <div className="text-center mb-8">
            <h1 className="text-4xl font-bold text-text-primary mb-2">
              Welcome Back
            </h1>
            <p className="text-text-secondary">
              Login to reserve limited-stock products
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="p-4 bg-error/10 border border-error rounded-lg text-error text-sm">
                {error}
              </div>
            )}

            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-text-primary mb-2">
                Username
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="w-full px-4 py-3 border border-secondary-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-600 focus:border-transparent transition-all"
                placeholder="Enter your username"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-text-primary mb-2">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 border border-secondary-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-600 focus:border-transparent transition-all"
                placeholder="Enter your password"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 bg-accent-600 text-white rounded-lg font-semibold hover:bg-accent-500 focus:outline-none focus:ring-2 focus:ring-accent-600 focus:ring-offset-2 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          {/* Mobile: Show test credentials hint */}
          <div className="lg:hidden mt-8 p-4 bg-secondary-50 rounded-lg border border-secondary-200">
            <p className="text-sm text-text-secondary mb-2">Test credentials:</p>
            <code className="text-xs font-mono text-text-primary">
              user1/password, user2/password, admin/password
            </code>
          </div>
        </div>
      </div>
    </div>
  );
};
