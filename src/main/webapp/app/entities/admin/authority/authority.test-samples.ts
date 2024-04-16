import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '8d108110-04f6-4b90-9854-1474a21e2de4',
};

export const sampleWithPartialData: IAuthority = {
  name: 'c32e49c8-4c9c-41d0-913f-1b659aada7aa',
};

export const sampleWithFullData: IAuthority = {
  name: '9b70643d-d784-4e9c-b9c6-3a158fd5d601',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
