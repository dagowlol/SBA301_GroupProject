/**
 * Mapper utility to convert Product shapes between Frontend Models and Backend DTOs.
 */
export const productMapper = {
  /**
   * Transforms raw data or API response (AuctionItem DTO) into Frontend ProductModel
   * @param {Object} item - Raw item DTO or backend response
   * @returns {Object} Mapped product model
   */
  toFrontendModel: (item) => {
    if (!item) return null;

    // Detect if this matches the JPA Entity structure (uses 'name' instead of 'title')
    const isBackendEntity = 'name' in item;

    let title = item.title || '';
    let artist = item.artist || '';
    let category = item.category || 'General';
    let reserve = parseFloat(item.reserve) || 0;
    let submittedBy = item.submittedBy || 'seller_current';
    let image = item.image || '';

    if (isBackendEntity) {
      title = item.name;
      artist = item.seller?.name || item.seller?.username || 'Unknown Artist';
      category = typeof item.category === 'object' ? item.category.name : item.category;
      reserve = parseFloat(item.reservePrice) || parseFloat(item.startingPrice) || 0;
      submittedBy = item.seller?.username || 'seller_current';
      
      if (Array.isArray(item.images) && item.images.length > 0) {
        // Look for main image, fallback to first
        const mainImg = item.images.find(img => img.isMain) || item.images[0];
        image = mainImg.url;
      }
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
      reserve: reserve,
      currentBid: item.currentBid ? parseFloat(item.currentBid) : null,
      status: status,
      submittedBy: submittedBy,
      image: image,
      description: item.description || '',
      views: parseInt(item.views) || 0,
      type: type,
      startTime: item.startTime,
      endTime: item.endTime,
      bids: Array.isArray(item.bids) ? item.bids.map(b => ({
        bidder: b.bidder,
        amount: parseFloat(b.amount) || 0,
        time: b.time
      })) : []
    };
  },

  /**
   * Maps frontend model to backend update / create request body payload DTO
   * @param {Object} model - Product UI state model
   * @returns {Object} DTO payload aligning with Java entity fields
   */
  toRequestDto: (model) => {
    if (!model) return null;
    return {
      name: model.title,
      description: model.description || '',
      startingPrice: parseFloat(model.reserve) * 0.8 || 100, // Starts at 80% of reserve
      reservePrice: parseFloat(model.reserve) || 0,
      status: model.status ? model.status.toUpperCase() : 'PENDING'
    };
  }
};

