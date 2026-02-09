export type CartItem = {
  productId: string;
  productName: string;
  amount: number;
  productPrice: number;
  subtotal: number;
  imageUrl: string;
}

export type Cart = {
  items: CartItem[];
  totalAmount: number;
  totalPrice: number;
}
