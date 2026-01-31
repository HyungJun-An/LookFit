export type CartItem = {
  pID: string;
  pname: string;
  amount: number;
  pprice: number;
  subtotal: number;
  imageUrl: string;
}

export type Cart = {
  items: CartItem[];
  totalAmount: number;
  totalPrice: number;
}
