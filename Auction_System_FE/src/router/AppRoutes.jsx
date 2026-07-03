import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AdminLayout from '../layouts/AdminLayout';
import GuestRoute from './GuestRoute';

// Feature components
import CatalogPage from '../features/catalog/pages/CatalogPage';
import ProductDetailPage from '../features/catalog/pages/ProductDetailPage';
import CategoryManagement from '../features/staff/pages/CategoryManagement';
import ItemApproval from '../features/staff/pages/ItemApproval';
import VerifyEmailPage from '../features/auth/pages/VerifyEmailPage';
import RegisterPage from '../features/auth/pages/RegisterPage';
import AuctionRoom from '../features/auction/pages/AuctionRoom';
import CreateItemRequest from '../features/user/pages/CreateItemRequest';
import SessionManagement from '../features/staff/pages/SessionManagement';
import BidMonitoring from '../features/staff/pages/BidMonitoring';
// import AutoBid from '../features/staff/pages/AutoBid';
import UserManagement from '../features/staff/pages/UserManagement';

import HomePage from '../features/catalog/pages/HomePage';
import MyAccountPage from '../features/user/pages/MyAccountPage';

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public User Routes */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route path="auction" element={<CatalogPage />} />
          <Route path="product/:id" element={<ProductDetailPage />} />
          <Route path="auction/:sessionId" element={<AuctionRoom />} />
          <Route path="my-account" element={<MyAccountPage />} />
          <Route path="user/items/create" element={<CreateItemRequest />} />
          <Route path="user/items" element={<div>My Items Page - <a href="/user/items/create">Create New</a></div>} />
          <Route element={<GuestRoute />}>
            <Route path="register" element={<RegisterPage />} />
            <Route path="verify-email" element={<VerifyEmailPage />} />
          </Route>
        </Route>

        {/* Staff Dashboard Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/items" replace />} />
          <Route path="categories" element={<CategoryManagement />} />
          <Route path="items" element={<ItemApproval />} />
          <Route path="sessions" element={<SessionManagement />} />
          <Route path="bid-monitoring" element={<BidMonitoring />} />
          {/* <Route path="autobid" element={<AutoBid />} /> */}
          <Route path="users" element={<UserManagement />} />
        </Route>

        {/* Fallback Catch-all Route */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
