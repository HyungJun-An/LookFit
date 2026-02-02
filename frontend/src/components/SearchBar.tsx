import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/SearchBar.css';

interface PopularSearch {
  keyword: string;
  searchCount: number;
  rank: number;
}

interface SearchSuggestion {
  recentSearches: string[];
  popularSearches: PopularSearch[];
}

const SearchBar = () => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState<SearchSuggestion | null>(null);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const navigate = useNavigate();
  const searchBarRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchBarRef.current && !searchBarRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const fetchSuggestions = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const config = token ? { headers: { Authorization: `Bearer ${token}` } } : {};

      const response = await axios.get<SearchSuggestion>(
        'http://localhost:8080/api/v1/search/suggestions',
        config
      );
      setSuggestions(response.data);
    } catch (error) {
      console.error('Failed to fetch suggestions:', error);
    }
  };

  const handleFocus = () => {
    setShowSuggestions(true);
    if (!suggestions) {
      fetchSuggestions();
    }
  };

  const handleSearch = (searchQuery: string) => {
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setShowSuggestions(false);
      setQuery('');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(query);
  };

  const handleSuggestionClick = (keyword: string) => {
    setQuery(keyword);
    handleSearch(keyword);
  };

  return (
    <div className="search-bar" ref={searchBarRef}>
      <form className="search-bar__form" onSubmit={handleSubmit}>
        <input
          type="text"
          className="search-bar__input"
          placeholder="상품을 검색하세요..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={handleFocus}
        />
        <button type="submit" className="search-bar__button">
          🔍
        </button>
      </form>

      {showSuggestions && suggestions && (
        <div className="search-suggestions">
          {suggestions.recentSearches.length > 0 && (
            <div className="search-suggestions__section">
              <h3 className="search-suggestions__title">최근 검색어</h3>
              <ul className="search-suggestions__list">
                {suggestions.recentSearches.map((keyword, index) => (
                  <li
                    key={`recent-${index}`}
                    className="search-suggestions__item"
                    onClick={() => handleSuggestionClick(keyword)}
                  >
                    <span className="search-suggestions__icon">🕒</span>
                    {keyword}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {suggestions.popularSearches.length > 0 && (
            <div className="search-suggestions__section">
              <h3 className="search-suggestions__title">인기 검색어</h3>
              <ul className="search-suggestions__list">
                {suggestions.popularSearches.map((item) => (
                  <li
                    key={`popular-${item.rank}`}
                    className="search-suggestions__item"
                    onClick={() => handleSuggestionClick(item.keyword)}
                  >
                    <span className="search-suggestions__rank">{item.rank}</span>
                    {item.keyword}
                    <span className="search-suggestions__count">({item.searchCount})</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {suggestions.recentSearches.length === 0 && suggestions.popularSearches.length === 0 && (
            <div className="search-suggestions__empty">
              검색어를 입력하세요
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SearchBar;
