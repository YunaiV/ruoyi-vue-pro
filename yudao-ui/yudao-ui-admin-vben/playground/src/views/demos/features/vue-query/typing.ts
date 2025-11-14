export interface IProducts {
  limit: number;
  products: {
    brand: string;
    category: string;
    description: string;
    discountPercentage: string;
    id: string;
    images: string[];
    price: string;
    rating: string;
    stock: string;
    thumbnail: string;
    title: string;
  }[];
  skip: number;
  total: number;
}
