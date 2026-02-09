import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import ProductList from './components/ProductList';
import ProductDetail from './components/ProductDetail';
import Cart from './components/Cart';
import Login from './components/Login';
import LoginSuccess from './components/LoginSuccess';
import VirtualFitting from './components/VirtualFitting';
import SearchResults from './pages/SearchResults';
import Wishlist from './pages/Wishlist';
import Signup from './pages/Signup';
import './App.css';

function App() {
  return (
    <div className="App">
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<ProductList />} />
          <Route path="/products/:productId" element={<ProductDetail />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/wishlist" element={<Wishlist />} />
          <Route path="/fitting" element={<VirtualFitting />} />
          <Route path="/search" element={<SearchResults />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/login/success" element={<LoginSuccess />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
