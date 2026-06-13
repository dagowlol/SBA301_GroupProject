import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AdminLayout from '../layouts/AdminLayout';

// Feature components
import CatalogPage from '../features/catalog/pages/CatalogPage';
import ProductDetailPage from '../features/catalog/pages/ProductDetailPage';
import CategoryManagement from '../features/staff/pages/CategoryManagement';
import ItemApproval from '../features/staff/pages/ItemApproval';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public User Routes */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<CatalogPage />} />
          <Route path="product/:id" element={<ProductDetailPage />} />
        </Route>

        {/* Staff Dashboard Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/items" replace />} />
          <Route path="categories" element={<CategoryManagement />} />
          <Route path="items" element={<ItemApproval />} />
        </Route>

        {/* Fallback Catch-all Route */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
