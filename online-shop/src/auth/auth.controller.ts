import { Body, Controller, HttpCode, HttpStatus, Post } from '@nestjs/common';
import { AuthService } from './auth.service';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @HttpCode(HttpStatus.OK)
  @Post('login')
  signIn(@Body() signInDto: Record<string, any>) {
    console.log('signInDto', signInDto);
    if (!signInDto.email || !signInDto.password) {
      throw new Error('Email and password are required');
    }
    return this.authService.signIn(signInDto.email, signInDto.password);
  }
}
