import { createContext, useState, useEffect } from 'react';
import { initialCategories, initialItems } from '../api/mockData';
import { categoryService } from '../features/staff/services/categoryService';
import { productService } from '../features/catalog/services/productService';

export const AppContext = createContext();

export function AppContextProvider({ children }) {
  const [categories, setCategories] = useState([]);

  const [items, setItems] = useState(() => {
    const saved = localStorage.getItem('auction_items');
    const raw = saved ? JSON.parse(saved) : initialItems;
    return productService.processItems(raw);
  });

  // Fetch categories from Backend via Service
  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await categoryService.getAllCategories();
        setCategories(data);
      } catch (err) {
        console.error("Failed to load categories from Service, falling back to local storage", err);
        const saved = localStorage.getItem('auction_categories');
        const fallback = saved ? JSON.parse(saved) : initialCategories;
        // fallback categories are mapped as well to keep shape consistency
        setCategories(fallback.map(c => ({
          id: c.id,
          name: c.name,
          description: c.description || '',
          parentCategoryId: c.parentCategoryId || null,
          parentCategoryName: c.parentCategoryName || null,
          slug: c.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
          order: c.order || c.sortOrder || 0,
          status: c.status || 'Active'
        })));
      }
    }
    loadCategories();
  }, []);

  useEffect(() => {
    localStorage.setItem('auction_categories', JSON.stringify(categories));
  }, [categories]);

  useEffect(() => {
    localStorage.setItem('auction_items', JSON.stringify(items));
  }, [items]);

  // F09: Approve an item
  const approveItem = (id) => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        return { ...item, status: 'Active', type: 'Current' }; // Set to Active to show up on the live auctions
      }
      return item;
    }));
  };

  // F09: Reject an item
  const rejectItem = (id) => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        return { ...item, status: 'Rejected' };
      }
      return item;
    }));
  };

  // Item CRUD
  const addItem = (item) => {
    const newItem = {
      ...item,
      id: Date.now(),
      views: 0,
      bids: [],
      currentBid: null,
      status: item.status || 'Pending',
      submittedBy: item.submittedBy || 'seller_current',
      type: item.status === 'Active' ? 'Current' : (item.status === 'Approved' ? 'Upcoming' : 'Upcoming')
    };
    const processed = productService.processItems([newItem])[0];
    setItems(prev => [processed, ...prev]);
  };

  const editItem = (id, updated) => {
    setItems(prev => prev.map(item => (item.id === id ? { ...item, ...updated } : item)));
  };

  const deleteItem = (id) => {
    setItems(prev => prev.filter(item => item.id !== id));
  };

  // Category CRUD calling Service Layer
  const addCategory = async (category) => {
    try {
      const created = await categoryService.createCategory(category);
      setCategories(prev => [...prev, created]);
    } catch (err) {
      console.error("Failed to add category via Service", err);
      throw err;
    }
  };

  const editCategory = async (id, updated) => {
    try {
      const edited = await categoryService.updateCategory(id, updated);
      setCategories(prev => prev.map(c => (c.id === id ? edited : c)));
    } catch (err) {
      console.error("Failed to edit category via Service", err);
      throw err;
    }
  };

  const deleteCategory = async (id) => {
    try {
      await categoryService.deleteCategory(id);
      setCategories(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      console.error("Failed to delete category via Service", err);
      throw err;
    }
  };

  // Views & Bids
  const incrementViewCount = (id) => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        return { ...item, views: item.views + 1 };
      }
      return item;
    }));
  };

  const placeBid = (id, amount, bidder = 'current_user') => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        const newBid = { bidder, amount, time: new Date().toISOString() };
        return {
          ...item,
          currentBid: amount,
          bids: [newBid, ...(item.bids || [])]
        };
      }
      return item;
    }));
  };

  return (
    <AppContext.Provider value={{
      categories,
      items,
      approveItem,
      rejectItem,
      addItem,
      editItem,
      deleteItem,
      addCategory,
      editCategory,
      deleteCategory,
      incrementViewCount,
      placeBid
    }}>
      {children}
    </AppContext.Provider>
  );
}
