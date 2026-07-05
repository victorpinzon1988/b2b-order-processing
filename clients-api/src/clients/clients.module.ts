import { Module } from "@nestjs/common";
import { ClientsController } from "./infraestructure/http/clients.controller";
import { GetClientService } from "./application/services/get-client.service";
import { CLIENTS_REPOSITORY } from "./application/ports/clients.repository";
import { InMemoryClientsRepository } from "./infraestructure/repositories/in-memory-clients.repository";
import { CachedClientsRepository } from "./infraestructure/repositories/cached-clients.repository";
import { redisProvider } from "./infraestructure/cache/redis.provider";

@Module({
    controllers: [ClientsController],
    providers: [
        GetClientService,
        InMemoryClientsRepository,
        CachedClientsRepository,
        redisProvider,
        {
            provide: CLIENTS_REPOSITORY,
            useClass: CachedClientsRepository,
        },
    ],
})

export class ClientsModule {}