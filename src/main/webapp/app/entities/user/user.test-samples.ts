import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: '9f491594-0585-4e49-9ca3-ce63a8786fe0',
  login: 'X~TBM@yM9\\#S-i\\.tXnn\\a7ht8-F\\H48\\5ffQwQ',
};

export const sampleWithPartialData: IUser = {
  id: '04a22023-99e7-4e3f-bd0b-f148acca1da6',
  login: '.$*|@Y5axJ2',
};

export const sampleWithFullData: IUser = {
  id: 'b704b17f-ae91-47cf-aea6-2381f4daf470',
  login: 'WfXXw@w\\SP\\jP\\+J\\6mAcO\\:A38IU',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
