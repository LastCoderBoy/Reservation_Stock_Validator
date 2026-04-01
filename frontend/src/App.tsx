import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './contexts/AuthContext';
import { Navbar } from './components/Navbar';
import { ProductsPage } from './pages/ProductsPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { LoginPage } from './pages/LoginPage';
import { ReservationsPage } from './pages/ReservationsPage';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            staleTime: 30_000,
        },
    },
});

// Navbar is hidden on the login page — it has its own full-screen layout
const Layout = ({ children }: { children: React.ReactNode }) => {
    const { pathname } = useLocation();
    const hideNavbar = pathname === '/login';
    return (
        <>
            {!hideNavbar && <Navbar />}
            {children}
        </>
    );
};

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <BrowserRouter>
                    <Layout>
                        <Routes>
                            <Route path="/"                element={<Navigate to="/products" replace />} />
                            <Route path="/products"        element={<ProductsPage />} />
                            <Route path="/products/:id"    element={<ProductDetailPage />} />
                            <Route path="/login"           element={<LoginPage />} />
                            <Route path="/reservations"    element={<ReservationsPage />} />
                            <Route path="*"               element={<Navigate to="/products" replace />} />
                        </Routes>
                    </Layout>
                </BrowserRouter>
            </AuthProvider>
        </QueryClientProvider>
    );
}