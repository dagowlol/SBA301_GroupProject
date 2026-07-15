// Helper to generate beautiful Unsplash artwork images based on item name and category
const getMockImage = (title, categoryName) => {
  const t = (title || '').toLowerCase();
  const c = (categoryName || '').toLowerCase();
  if (t.includes('mona lisa')) return 'https://images.unsplash.com/photo-1580136579312-94651dfd596d?w=500&auto=format&fit=crop&q=60';
  if (t.includes('pots') || c.includes('ceramics')) return 'https://images.unsplash.com/photo-1612196808214-b8e1d6145a8c?w=500&auto=format&fit=crop&q=60';
  if (t.includes('bird') || t.includes('forest')) return 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=500&auto=format&fit=crop&q=60';
  if (t.includes('apollo') || c.includes('sculptures')) return 'https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?w=500&auto=format&fit=crop&q=60';
  if (t.includes('blue') || c.includes('paintings')) return 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&auto=format&fit=crop&q=60';
  if (t.includes('woman') || t.includes('summer')) return 'https://images.unsplash.com/photo-1501472312651-726afd116ff1?w=500&auto=format&fit=crop&q=60';
  return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=60'; // general art placeholder
};

/**
 * Mapper utility to convert Product shapes between Frontend Models and Backend DTOs.
 */
export const productMapper = {
  /**
   * Transforms ItemResponse DTO from Spring Boot into Frontend ProductModel
   * @param {Object} item - ItemResponse DTO
   * @returns {Object} Frontend Product Model
   */
  toFrontendModel: (item) => {
    if (!item) return null;

    // Detect if this matches the Spring Boot DTO (uses startingPrice/reservePrice)
    const isBackendDto = 'startingPrice' in item;

    let title = item.title || '';
    let artist = item.artist || '';
    let category = item.category || 'General';
    let reserve = parseFloat(item.reserve) || 0;
    let submittedBy = item.submittedBy || 'seller_current';
    let image = item.image || '';
    let categoryId = item.categoryId || null;

    if (isBackendDto) {
      title = item.name;
      artist = item.sellerName || 'Unknown Artist';
      category = item.categoryName || 'General';
      reserve = parseFloat(item.reservePrice) || parseFloat(item.startingPrice) || 0;
      submittedBy = item.sellerName || 'seller_current';
      image = getMockImage(item.name, item.categoryName);
      categoryId = item.categoryId;
    } else {
      // Mock local items
      image = item.image || getMockImage(title, category);
      categoryId = item.categoryId || null;
    }

    // Map backend uppercase status enum back to frontend title-case badge state
    const rawStatus = item.status || 'PENDING';
    const statusMap = {
      'PENDING': 'Pending',
      'APPROVED': 'Approved',
      'ACTIVE': 'Active',
      'REJECTED': 'Rejected',
      'Pending': 'Pending',
      'Approved': 'Approved',
      'Active': 'Active',
      'Rejected': 'Rejected'
    };
    const status = statusMap[rawStatus] || 'Pending';
    const type = status === 'Active' ? 'Current' : 'Upcoming';

    return {
      id: item.id,
      title: title,
      artist: artist,
      category: category,
      categoryId: categoryId,
      reserve: reserve,
      startingPrice: isBackendDto ? parseFloat(item.startingPrice) : reserve * 0.8,
      currentBid: item.currentBid ? parseFloat(item.currentBid) : null,
      status: status,
      submittedBy: submittedBy,
      image: image,
      description: item.description || '',
      views: parseInt(item.views) || 0,
      type: type,
      startTime: item.startTime || item.createdAt || new Date().toISOString(),
      endTime: item.endTime || item.updatedAt || new Date().toISOString(),
      minIncrement: item.minIncrement ? parseFloat(item.minIncrement) : 0,
      condition: item.condition || 'NEW',
      rejectionReason: item.rejectionReason || '',
      bids: Array.isArray(item.bids) ? item.bids.map(b => ({
        bidder: b.bidder,
        amount: parseFloat(b.amount) || 0,
        time: b.time
      })) : []
    };
  },

  /**
   * Maps frontend model to backend create ItemRequest body DTO
   * @param {Object} model - Product UI state model
   * @returns {Object} DTO payload matching ItemRequest
   */
  toRequestDto: (model) => {
    if (!model) return null;
    return {
      itemName: model.itemName || model.title,
      description: model.description || '',
      categoryId: parseInt(model.categoryId) || 1,
      startingPrice: parseFloat(model.startingPrice) || parseFloat(model.reserve) * 0.8 || 100,
      reservePrice: parseFloat(model.reservePrice) || parseFloat(model.reserve) || 0,
      minIncrement: parseFloat(model.minIncrement) || 1000,
      condition: model.condition || 'NEW',
      status: model.status ? model.status.toUpperCase() : undefined
    };
  }
};
