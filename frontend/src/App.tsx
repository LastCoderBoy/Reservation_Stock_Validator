import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'sonner';
import { AuthProvider } from './contexts';
import { ProtectedRoute } from './routes';
import { HomePage, LoginPage, ProductDetailPage, ReservationsPage, ComponentShowcasePage } from './pages';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        {/* Sonner Toast Notifications */}
        <Toaster 
          position="top-right" 
          richColors 
          closeButton
          toastOptions={{
            style: {
              fontFamily: 'Inter, system-ui, sans-serif',
            },
          }}
        />

        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          
          {/* Component Showcase (dev only) */}
          <Route path="/components" element={<ComponentShowcasePage />} />

          {/* Protected Routes */}
          <Route
            path="/reservations"
            element={
              <ProtectedRoute>
                <ReservationsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
