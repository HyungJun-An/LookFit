export type ProductSummary = {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  description: string;
  productCompany: string;
  productStock: number;
  imageUrl?: string;
  thumbnailUrl?: string;
}

export type Product = {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  description: string;
  productCompany: string;
  productStock: number;
  imageUrl?: string;
  thumbnailUrl?: string;
  images?: string[];
  sizes?: string[];
  colors?: string[];
}
