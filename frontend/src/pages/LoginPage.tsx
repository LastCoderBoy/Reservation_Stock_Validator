import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLogin } from '../hooks/useLogin';

interface SampleUser {
  label: string;
  role: 'Admin' | 'User';
  username: string;
  password: string;
  color: string;
  initial: string;
}

const SAMPLE_USERS: SampleUser[] = [
  {
    label: 'John',
    role: 'User',
    username: 'johndoe',
    password: 'John_01secure!',
    color: '#0ea5e9',
    initial: 'J',
  },
  {
    label: 'Jane',
    role: 'User',
    username: 'janesmith',
    password: 'Jane_010203',
    color: '#10b981',
    initial: 'J',
  },
  {
    label: 'Charlie',
    role: 'User',
    username: 'charlieb',
    password: 'Charlie_001',
    color: '#f59e0b',
    initial: 'CH',
  },
];

export const LoginPage = () => {
  const { form, isLoading, error, setField, submit, fillCredentials } = useLogin();
  const [showPassword, setShowPassword] = useState(false);
  const [activeUser, setActiveUser] = useState<string | null>(null);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') submit();
  };

  const handleFill = (user: SampleUser) => {
    fillCredentials(user.username, user.password);
    setActiveUser(user.username);
  };

  return (
      <div style={styles.root}>
        {/* ── Background pattern ─────────────────────────── */}
        <div style={styles.bgPattern} aria-hidden />

        <div style={styles.layout}>

          {/* ── LEFT — Sample credentials ──────────────────── */}
          <aside style={styles.aside}>
            <div style={styles.asideInner}>
              <div style={styles.asideBadge}>Demo accounts</div>
              <h2 style={styles.asideTitle}>Try it out</h2>
              <p style={styles.asideSubtitle}>
                Click any account to auto-fill the login form.
              </p>

              <div style={styles.userList}>
                {SAMPLE_USERS.map(user => (
                    <button
                        key={user.username}
                        onClick={() => handleFill(user)}
                        style={{
                          ...styles.userCard,
                          ...(activeUser === user.username ? styles.userCardActive : {}),
                          borderColor: activeUser === user.username
                              ? user.color
                              : 'rgba(255,255,255,0.08)',
                        }}
                    >
                      <div
                          style={{
                            ...styles.avatar,
                            background: user.color + '22',
                            border: `1.5px solid ${user.color}44`,
                            color: user.color,
                          }}
                      >
                        {user.initial}
                      </div>

                      <div style={styles.userInfo}>
                        <div style={styles.userTopRow}>
                          <span style={styles.userName}>{user.label}</span>
                          <span
                              style={{
                                ...styles.rolePill,
                                background: user.role === 'Admin'
                                    ? '#6366f122' : '#ffffff0f',
                                color: user.role === 'Admin'
                                    ? '#818cf8' : '#94a3b8',
                                border: `1px solid ${user.role === 'Admin' ? '#6366f133' : '#ffffff14'}`,
                              }}
                          >
                        {user.role}
                      </span>
                        </div>
                        <span style={styles.userMeta}>{user.username}</span>
                        <span style={styles.userMeta}>pw: {user.password}</span>
                      </div>

                      {activeUser === user.username && (
                          <div style={{ ...styles.checkmark, color: user.color }}>✓</div>
                      )}
                    </button>
                ))}
              </div>

              <p style={styles.asideNote}>
                Credentials are for demo purposes only.
              </p>
            </div>
          </aside>

          {/* ── RIGHT — Login form ─────────────────────────── */}
          <main style={styles.main}>
            <div style={styles.formCard}>
              {/* Logo / wordmark */}
              <div style={styles.logoRow}>
                <div style={styles.logoDot} />
                <span style={styles.logoText}>LimitedDrop</span>
              </div>

              <h1 style={styles.formTitle}>Welcome back</h1>
              <p style={styles.formSubtitle}>Sign in to reserve limited drops</p>

              <div style={styles.fieldGroup}>
                {/* Username */}
                <div style={styles.field}>
                  <label style={styles.label} htmlFor="username">Username</label>
                  <input
                      id="username"
                      type="text"
                      autoComplete="username"
                      value={form.username}
                      onChange={e => setField('username', e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="your_username"
                      style={{
                        ...styles.input,
                        ...(error ? styles.inputError : {}),
                      }}
                  />
                </div>

                {/* Password */}
                <div style={styles.field}>
                  <label style={styles.label} htmlFor="password">Password</label>
                  <div style={styles.passwordWrapper}>
                    <input
                        id="password"
                        type={showPassword ? 'text' : 'password'}
                        autoComplete="current-password"
                        value={form.password}
                        onChange={e => setField('password', e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="••••••••"
                        style={{
                          ...styles.input,
                          ...(error ? styles.inputError : {}),
                          paddingRight: '44px',
                        }}
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword(v => !v)}
                        style={styles.eyeBtn}
                        tabIndex={-1}
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? '🙈' : '👁'}
                    </button>
                  </div>
                </div>
              </div>

              {/* Error */}
              {error && (
                  <div style={styles.errorBox}>
                    <span style={styles.errorIcon}>!</span>
                    {error}
                  </div>
              )}

              {/* Submit */}
              <button
                  onClick={submit}
                  disabled={isLoading}
                  style={{
                    ...styles.submitBtn,
                    opacity: isLoading ? 0.7 : 1,
                    cursor: isLoading ? 'not-allowed' : 'pointer',
                  }}
              >
                {isLoading ? (
                    <span style={styles.spinner} />
                ) : (
                    'Sign in'
                )}
              </button>

              {/* Footer */}
              <div style={styles.formFooter}>
                <Link to="/products" style={styles.footerLink}>
                  ← Browse without signing in
                </Link>
              </div>
            </div>
          </main>
        </div>
      </div>
  );
};

// ─── Styles ──────────────────────────────────────────────────────
const styles: Record<string, React.CSSProperties> = {
  root: {
    minHeight: '100vh',
    background: '#0a0b0f',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontFamily: "'DM Sans', 'Helvetica Neue', sans-serif",
    padding: '24px 16px',
    position: 'relative',
    overflow: 'hidden',
  },
  bgPattern: {
    position: 'absolute',
    inset: 0,
    backgroundImage: `
      radial-gradient(ellipse 80% 50% at 20% 40%, rgba(99,102,241,0.08) 0%, transparent 60%),
      radial-gradient(ellipse 60% 60% at 80% 60%, rgba(14,165,233,0.06) 0%, transparent 60%)
    `,
    pointerEvents: 'none',
  },
  layout: {
    display: 'flex',
    width: '100%',
    maxWidth: '920px',
    gap: '24px',
    alignItems: 'stretch',
    position: 'relative',
    zIndex: 1,
    flexWrap: 'wrap' as const,
  },

  // ── Aside ────────────────────────────────────────────────
  aside: {
    flex: '1 1 300px',
    minWidth: '280px',
  },
  asideInner: {
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.07)',
    borderRadius: '20px',
    padding: '32px 28px',
    height: '100%',
    boxSizing: 'border-box' as const,
  },
  asideBadge: {
    display: 'inline-block',
    fontSize: '11px',
    fontWeight: 600,
    letterSpacing: '0.08em',
    textTransform: 'uppercase' as const,
    color: '#818cf8',
    background: '#6366f118',
    border: '1px solid #6366f130',
    borderRadius: '6px',
    padding: '3px 10px',
    marginBottom: '16px',
  },
  asideTitle: {
    fontSize: '22px',
    fontWeight: 700,
    color: '#f1f5f9',
    margin: '0 0 6px',
  },
  asideSubtitle: {
    fontSize: '13px',
    color: '#64748b',
    margin: '0 0 24px',
    lineHeight: 1.5,
  },
  userList: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '10px',
  },
  userCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    width: '100%',
    padding: '12px 14px',
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.08)',
    borderRadius: '12px',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
    textAlign: 'left' as const,
    position: 'relative' as const,
  },
  userCardActive: {
    background: 'rgba(99,102,241,0.06)',
  },
  avatar: {
    width: '36px',
    height: '36px',
    borderRadius: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '15px',
    fontWeight: 700,
    flexShrink: 0,
  },
  userInfo: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '2px',
    flex: 1,
    minWidth: 0,
  },
  userTopRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  userName: {
    fontSize: '13px',
    fontWeight: 600,
    color: '#e2e8f0',
  },
  rolePill: {
    fontSize: '10px',
    fontWeight: 600,
    letterSpacing: '0.04em',
    padding: '1px 7px',
    borderRadius: '4px',
  },
  userMeta: {
    fontSize: '11px',
    color: '#475569',
    fontFamily: "'Fira Code', 'Courier New', monospace",
  },
  checkmark: {
    fontSize: '14px',
    fontWeight: 700,
    flexShrink: 0,
  },
  asideNote: {
    fontSize: '11px',
    color: '#334155',
    marginTop: '20px',
    textAlign: 'center' as const,
  },

  // ── Main form ────────────────────────────────────────────
  main: {
    flex: '1 1 340px',
    display: 'flex',
    alignItems: 'center',
  },
  formCard: {
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.07)',
    borderRadius: '20px',
    padding: '40px 36px',
    width: '100%',
    boxSizing: 'border-box' as const,
  },
  logoRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '32px',
  },
  logoDot: {
    width: '10px',
    height: '10px',
    borderRadius: '50%',
    background: '#6366f1',
    boxShadow: '0 0 12px #6366f1',
  },
  logoText: {
    fontSize: '15px',
    fontWeight: 700,
    color: '#e2e8f0',
    letterSpacing: '-0.01em',
  },
  formTitle: {
    fontSize: '28px',
    fontWeight: 700,
    color: '#f8fafc',
    margin: '0 0 6px',
    letterSpacing: '-0.02em',
  },
  formSubtitle: {
    fontSize: '14px',
    color: '#64748b',
    margin: '0 0 32px',
  },
  fieldGroup: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '18px',
    marginBottom: '20px',
  },
  field: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '7px',
  },
  label: {
    fontSize: '12px',
    fontWeight: 600,
    color: '#94a3b8',
    letterSpacing: '0.04em',
    textTransform: 'uppercase' as const,
  },
  input: {
    width: '100%',
    padding: '11px 14px',
    background: 'rgba(255,255,255,0.05)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '10px',
    color: '#f1f5f9',
    fontSize: '14px',
    outline: 'none',
    boxSizing: 'border-box' as const,
    fontFamily: 'inherit',
    transition: 'border-color 0.15s',
  },
  inputError: {
    borderColor: '#ef4444',
  },
  passwordWrapper: {
    position: 'relative' as const,
  },
  eyeBtn: {
    position: 'absolute' as const,
    right: '12px',
    top: '50%',
    transform: 'translateY(-50%)',
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    fontSize: '14px',
    padding: '2px',
    lineHeight: 1,
  },
  errorBox: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    background: 'rgba(239,68,68,0.08)',
    border: '1px solid rgba(239,68,68,0.2)',
    borderRadius: '10px',
    padding: '10px 14px',
    fontSize: '13px',
    color: '#fca5a5',
    marginBottom: '16px',
  },
  errorIcon: {
    width: '18px',
    height: '18px',
    borderRadius: '50%',
    background: '#ef4444',
    color: '#fff',
    fontSize: '11px',
    fontWeight: 700,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  submitBtn: {
    width: '100%',
    padding: '13px',
    background: '#6366f1',
    color: '#fff',
    border: 'none',
    borderRadius: '12px',
    fontSize: '15px',
    fontWeight: 600,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    letterSpacing: '-0.01em',
    transition: 'opacity 0.15s',
    marginBottom: '20px',
    fontFamily: 'inherit',
  },
  spinner: {
    width: '18px',
    height: '18px',
    border: '2px solid rgba(255,255,255,0.3)',
    borderTopColor: '#fff',
    borderRadius: '50%',
    animation: 'spin 0.7s linear infinite',
    display: 'inline-block',
  },
  formFooter: {
    textAlign: 'center' as const,
  },
  footerLink: {
    fontSize: '13px',
    color: '#475569',
    textDecoration: 'none',
  },
};