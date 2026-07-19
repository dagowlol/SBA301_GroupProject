import { PackageOpen, ShieldCheck } from 'lucide-react';
import AuctionItemsTab from './AuctionItemsTab';
import './myItems.css';

export default function MyItemsPage() {
  return (
    <main className="my-items-page">
      <section className="my-items-hero">
        <div className="my-items-hero__glow" />
        <div className="my-items-page__container my-items-hero__content">
          <div>
            <span className="my-items-hero__eyebrow">
              <ShieldCheck size={14} /> Personal collection
            </span>
            <h1>My Items</h1>
            <p>Track submitted collectibles, auction progress, wins, and payment status in one place.</p>
          </div>
          <div className="my-items-hero__mark" aria-hidden="true">
            <PackageOpen size={42} />
          </div>
        </div>
      </section>

      <div className="my-items-page__container my-items-page__panel">
        <AuctionItemsTab />
      </div>
    </main>
  );
}
