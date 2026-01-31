export type ProductSummary = {
  pid: string;
  pname: string;
  pprice: number;
  pcategory: string;
  description: string;
  pcompany: string;
  pstock: number;
  imageUrl?: string;
  thumbnailUrl?: string;
}

export type Product = {
  pid: string;
  pname: string;
  pprice: number;
  pcategory: string;
  description: string;
  pcompany: string;
  pstock: number;
  images?: string[];
  sizes?: string[];
  colors?: string[];
}
