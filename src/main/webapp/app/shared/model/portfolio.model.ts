import { IUser } from 'app/shared/model/user.model';

export interface IPortfolio {
  id?: number;
  projectName?: string;
  description?: string;
  imageUrl?: string;
  link?: string;
  user?: IUser;
}

export const defaultValue: Readonly<IPortfolio> = {};
