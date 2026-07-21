import React from 'react';
import '../styles/ContactUsPage.css';

const ContactUsPage = () => {
  return (
    <div className="contact-us-page">
      {/* Hero Section */}
      <div className="contact-hero">
        <div className="contact-title-wrapper">
          <h1 className="contact-title">CONTACT</h1>
        </div>
      </div>

      {/* Map Section */}
      <div className="contact-map">
        <iframe
          title="Google Map"
          src="https://maps.google.com/maps?q=FPT%20University%20Da%20Nang&t=&z=16&ie=UTF8&iwloc=&output=embed"
          width="100%"
          height="100%"
          style={{ border: 0 }}
          allowFullScreen=""
          loading="lazy"
          referrerPolicy="no-referrer-when-downgrade"
        ></iframe>
      </div>

      {/* Contact Info Section */}
      <div className="contact-info-section">
        <div className="contact-info-block">
          <h2>Annexe<br />Auction</h2>
          <p>FPT Urban Area, Ngu Hanh Son Ward, Da Nang City</p>
          <p>p. 0913-745-837</p>
          <p>e. auctionsystem1234@gmail.com</p>
        </div>
        <div className="contact-info-block">
          <h2>ART Agenda<br />Annexe</h2>
          <p>p. +84 888 205 252</p>
          <p>e. doanchauviet@gmail.com</p>
        </div>
      </div>

      {/* Contact Form Section */}
      <div className="contact-form-section">
        <h2 className="form-title">Contact form</h2>
        <form className="contact-form">
          <div className="form-row">
            <input type="text" placeholder="First Name" />
            <input type="text" placeholder="Last Name" />
          </div>
          <div className="form-row">
            <input type="email" placeholder="Email" />
          </div>
          <div className="form-row">
            <input type="text" placeholder="Subject" />
          </div>
          <div className="form-row">
            <textarea placeholder="Message"></textarea>
          </div>
          <div className="form-submit">
            <button type="submit">Submit</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ContactUsPage;
