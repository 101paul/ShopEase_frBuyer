# 🚀 Full-Stack Android eCommerce Solution – Buyer & Seller Apps  
*(This is the Buyer version)*  
**Seller Version** 👉 [ShopEase_frSeller](https://github.com/101paul/ShopEase_frSeller)

An end-to-end **native Android eCommerce ecosystem**, designed to deliver a seamless, real-time, and secure experience for both **buyers** and **sellers**.

---

## 📱 Two Apps – One Powerful Ecosystem

<div align="center">
  <img src="images/logo.png" alt="Logo" width="200" style="display: block; margin: auto;" />
</div>

This project includes **two fully functional Android applications**, built from the ground up:

- 🛒 **Buyer App** – For browsing products, managing cart, placing orders, making secure payments, and tracking delivery  
- 🛍️ **Seller App** – For uploading products, managing stock, and processing incoming orders  

---

## 🛠️ Tech Stack & Architecture

<div align="center">
  <img src="images/ShopEaseHomeP.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase3.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase5.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

- **Kotlin + XML** – Modern, fast, and intuitive native UI development  
- **MVVM Architecture** – Clean code separation and lifecycle-aware components  
- **Room Database** – Efficient offline access and persistent cart state  
- **SharedPreferences** – Lightweight local state management  
- **Firebase Realtime Database** – Instant syncing of orders, stock, and product data  
- **Firebase Cloud Storage** – Fast and scalable image uploads & retrieval  

<div align="center">
  <img src="images/shopEase6.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase7.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

---

## 🔐 Authentication & Security

<div align="center">
  <img src="images/security1.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/security2.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

- **OTP Login via Firebase Authentication**
  - Secure, passwordless access  
  - Fast mobile number verification  
  - Seamless onboarding for both buyers and sellers  

<div align="center">
  <img src="images/sendingOtp.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/otpVerification.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

---

## 💳 Payment Gateway Integration

<div align="center">
  <img src="images/shopEase8.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase9.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase10.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

- **Razorpay Payment Gateway**
  - Smooth and secure payment flow  
  - Handles multiple payment methods (UPI, cards, wallets)

- **Custom Java + Spring Boot Backend**
  - Generates secure Razorpay order IDs  
  - Validates and verifies payment transactions  
  - Protects sensitive user credentials  
  - Ensures full-stack transaction integrity  

<div align="center">
  <img src="images/shopEase11.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase12.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

---

## ⚡ Real-Time Buyer-Seller Sync

- Live updates on:
  - New orders  
  - Stock changes  
  - Product additions or edits  
- Instant notification to buyers when orders are packed, shipped, or delivered  

---

## 🧠 Smart Cart & Order Management

<div align="center">
  <img src="images/ShopEaseHomeP.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase13.png" width="200" style="display: inline-block; margin:10px;"/>
  <img src="images/shopEase14.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

- Real-time cart updates using Room + Firebase  
- Auto-clears cart when quantity hits zero  
- Post-order stock deductions reflected immediately in the seller app  

---
## 🔍 Search box 
<div align="center">
  <img src="images/searchFragment.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

---
## 📍Address 
<div align="center">
  <img src="images/addressShopEase.png" width="200" style="display: inline-block; margin:10px;"/>
</div>

---
## ✅ Why This App Stands Out

- 100% **native Android** development  
- Clean, scalable **MVVM** architecture  
- Full-stack: From **UI to backend payment verification**  
- **Secure**, **real-time** and **production-ready**  
- Offline support for smoother experience  
- Ideal for launching a **mobile-first eCommerce solution**  

---

## 📦 Features At A Glance

| Feature                            | Buyer App ✅ | Seller App ✅ |
|-----------------------------------|--------------|----------------|
| OTP Login                         | ✅           | ✅             |
| Realtime Cart                     | ✅           |                |
| Product Upload & Management       |              | ✅             |
| Stock Management                  |              | ✅             |
| Live Order Updates                | ✅           | ✅             |
| Razorpay Payment Gateway          | ✅           |                |
| Order Verification via Backend    | ✅           | ✅             |
| Firebase Sync (Realtime + Images) | ✅           | ✅             |
| Offline Cart Storage (Room)       | ✅           |                |
