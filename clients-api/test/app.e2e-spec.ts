import { INestApplication } from "@nestjs/common"
import { TestingModule, Test } from "@nestjs/testing"
import request from 'supertest'
import { AppModule } from "../src/app.module"


describe('Clients API E2E', () => {
  let app: INestApplication

  beforeAll(async() => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    await app.init();
  });

  afterAll(async() => {
    await app.close();
  });

  it('GET /clients/:clientId should return an existing client', async () => {
    const response = await request(app.getHttpServer())
      .get('/clients/CLI-99821')
      .expect(200);

    expect(response.body).toEqual({
        clientId: 'CLI-99821',
        name: 'Distribuidora Andina S.A.S',
        segment: 'MAYORISTA',
        taxRegime: 'RESPONSABLE_IVA',
        region: 'Valle del Cauca',
    });  
  });

  it('GET /clients/:clientId should return 404 when client does not exist', async () => {
    const response = await request(app.getHttpServer())
      .get('/clients/CLI-404')
      .expect(404);

    
    expect(response.body.message).toBe('Client CLI-404 not found');

  });


});
