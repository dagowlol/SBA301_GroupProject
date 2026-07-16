import { createContext, useState, useEffect } from 'react';
import { initialCategories, initialItems } from '../api/mockData';
import { categoryService } from '../features/staff/services/categoryService';
import { productService } from '../features/catalog/services/productService';

export const AppContext = createContext();

export function AppContextProvider({ children }) {
  const [categories, setCategories] = useState([]);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function loadAll() {
      setLoading(true);
      try {
        const [catData, pageResult] = await Promise.all([
          categoryService.getAllCategories(),
          productService.getAllItems({ size: 100 }),
        ]);
        if (!cancelled) {
          setCategories(catData);
          setItems(pageResult.content);
        }
      } catch (err) {
        console.error("Failed to load data from Service, falling back to local storage", err);
        if (!cancelled) {
          const savedCats = localStorage.getItem('auction_categories');
          const fallbackCats = savedCats ? JSON.parse(savedCats) : initialCategories;
          setCategories(fallbackCats.map(c => ({
            id: c.id,
            name: c.name,
            description: c.description || '',
            parentCategoryId: c.parentCategoryId || null,
            parentCategoryName: c.parentCategoryName || null,
            slug: c.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
            order: c.order || c.sortOrder || 0,
            status: c.status || 'Active'
          })));

          const savedItems = localStorage.getItem('auction_items');
          const fallbackItems = savedItems ? JSON.parse(savedItems) : initialItems;
          setItems(productService.processItems(fallbackItems));
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    loadAll();
    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    localStorage.setItem('auction_categories', JSON.stringify(categories));
  }, [categories]);

  useEffect(() => {
    localStorage.setItem('auction_items', JSON.stringify(items));
  }, [items]);

  const refetch = async () => {
    setLoading(true);
    try {
      const [catData, pageResult] = await Promise.all([
        categoryService.getAllCategories(),
        productService.getAllItems({ size: 100 }),
      ]);
      setCategories(catData);
      setItems(pageResult.content);
    } catch (err) {
      console.error("Failed to refetch data:", err);
    } finally {
      setLoading(false);
    }
  };

  // F09: Approve an item on backend
  const approveItem = async (id) => {
    try {
      const updated = await productService.approveItem(id);
      setItems(prev => prev.map(item => (item.id === id ? updated : item)));
    } catch (err) {
      console.error("Failed to approve item via Service", err);
      throw err;
    }
  };

  // F09: Reject an item on backend
  const rejectItem = async (id, reason) => {
    try {
      const updated = await productService.rejectItem(id, reason);
      setItems(prev => prev.map(item => (item.id === id ? updated : item)));
    } catch (err) {
      console.error("Failed to reject item via Service", err);
      throw err;
    }
  };

  // Item CRUD
  const addItem = async (item) => {
    try {
      const created = await productService.createItem(item);
      setItems(prev => [created, ...prev]);
    } catch (err) {
      console.error("Failed to submit item via Service", err);
      throw err;
    }
  };

  const editItem = async (id, updated) => {
    try {
      const edited = await productService.updateItem(id, updated);
      setItems(prev => prev.map(item => (item.id === id ? edited : item)));
    } catch (err) {
      console.error("Failed to edit item via Service", err);
      throw err;
    }
  };

  const deleteItem = async (id) => {
    try {
      await productService.deleteItem(id);
      setItems(prev => prev.filter(item => item.id !== id));
    } catch (err) {
      console.error("Failed to delete item via Service", err);
      throw err;
    }
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
        const newBid = { bidder, amount, time: new Date().toISOString };
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
      loading,
      refetch,
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
