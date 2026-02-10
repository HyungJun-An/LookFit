import { useState } from 'react';
import '../styles/StarRating.css';

interface StarRatingProps {
  rating: number;
  maxStars?: number;
  size?: 'sm' | 'md' | 'lg';
  interactive?: boolean;
  onChange?: (rating: number) => void;
}

const StarRating = ({
  rating,
  maxStars = 5,
  size = 'md',
  interactive = false,
  onChange,
}: StarRatingProps) => {
  const [hoverRating, setHoverRating] = useState(0);

  const handleClick = (star: number) => {
    if (interactive && onChange) {
      onChange(star);
    }
  };

  const handleMouseEnter = (star: number) => {
    if (interactive) {
      setHoverRating(star);
    }
  };

  const handleMouseLeave = () => {
    if (interactive) {
      setHoverRating(0);
    }
  };

  const displayRating = hoverRating || rating;

  return (
    <div className={`star-rating star-rating--${size} ${interactive ? 'star-rating--interactive' : ''}`}>
      {Array.from({ length: maxStars }, (_, index) => {
        const starValue = index + 1;
        const isFilled = starValue <= displayRating;
        const isHalfFilled = !isFilled && starValue - 0.5 <= displayRating;

        return (
          <span
            key={index}
            className={`star ${isFilled ? 'star--filled' : ''} ${isHalfFilled ? 'star--half' : ''}`}
            onClick={() => handleClick(starValue)}
            onMouseEnter={() => handleMouseEnter(starValue)}
            onMouseLeave={handleMouseLeave}
            role={interactive ? 'button' : 'presentation'}
            aria-label={interactive ? `${starValue}점` : undefined}
            tabIndex={interactive ? 0 : -1}
            onKeyDown={(e) => {
              if (interactive && (e.key === 'Enter' || e.key === ' ')) {
                handleClick(starValue);
              }
            }}
          >
            <svg viewBox="0 0 24 24" fill={isFilled ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth={1.5}>
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z"
              />
            </svg>
          </span>
        );
      })}
    </div>
  );
};

export default StarRating;
