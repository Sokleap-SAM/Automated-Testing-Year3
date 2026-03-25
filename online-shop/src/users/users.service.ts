import { Injectable } from '@nestjs/common';
export type User = {
  email: string;
  password: string;
};
@Injectable()
export class UsersService {
  private readonly users = [
    {
      userId: 1,
      email: 'john@mail.com',
      password: 'changeme',
    },
    {
      userId: 2,
      email: 'maria@mail.com',
      password: 'guess',
    },
  ];

  async findOne(email: string): Promise<User | undefined> {
    await new Promise((resolve) => setTimeout(resolve, 1000));
    return this.users.find((user) => user.email === email);
  }
}
