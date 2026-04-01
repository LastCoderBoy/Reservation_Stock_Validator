import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

interface LoginForm {
    username: string;
    password: string;
}

interface UseLoginResult {
    form: LoginForm;
    isLoading: boolean;
    error: string | null;
    setField: (field: keyof LoginForm, value: string) => void;
    submit: () => Promise<void>;
    fillCredentials: (username: string, password: string) => void;
}

export const useLogin = (): UseLoginResult => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();

    const [form, setForm] = useState<LoginForm>({ username: '', password: '' });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const from = (location.state as { from?: string })?.from ?? '/products';

    const setField = (field: keyof LoginForm, value: string) => {
        setForm(prev => ({ ...prev, [field]: value }));
        setError(null);
    };

    const fillCredentials = (username: string, password: string) => {
        setForm({ username, password });
        setError(null);
    };

    const submit = async () => {
        if (!form.username.trim() || !form.password.trim()) {
            setError('Please enter both username and password.');
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const data = await authApi.login(form.username.trim(), form.password);
            if (!data) throw new Error('No response from server');
            login({ username: data.username, role: data.role }, data.accessToken);
            navigate(from, { replace: true });
        } catch (err: unknown) {
            const msg =
                err instanceof Error && err.message
                    ? err.message
                    : 'Invalid username or password.';
            setError(msg);
        } finally {
            setIsLoading(false);
        }
    };

    return { form, isLoading, error, setField, submit, fillCredentials };
};