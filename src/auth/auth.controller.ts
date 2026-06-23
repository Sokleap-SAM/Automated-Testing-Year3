import { Controller, Post, Body, UnauthorizedException } from '@nestjs/common';

@Controller('auth')
export class AuthController {
  @Post('login')
  login(@Body() body: { email: string; password: string }) {
    if (body.email === 'admin@orderzone.net' && body.password === 'secret') {
      return { access_token: 'test-token-admin' };
    }
    throw new UnauthorizedException();
  }
}
