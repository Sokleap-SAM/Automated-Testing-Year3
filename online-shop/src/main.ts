import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import expressListRoutes from 'express-list-routes';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  await app.init();
  console.log('Available routes:');
  expressListRoutes(app.getHttpAdapter().getInstance());
  await app.listen(process.env.PORT ?? 3000);
}
bootstrap();
