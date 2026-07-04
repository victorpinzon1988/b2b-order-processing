import { Module } from "@nestjs/common";
import { ClientsController } from "./infraestructure/http/clients.controller";
import { GetClientService } from "./application/services/get-client.service";
import { CLIENTS_REPOSITORY } from "./application/ports/clients.repository";
import { InMemoryClientsRepository } from "./infraestructure/repositories/in-memory-clients.repository";

@Module({
    controllers: [ClientsController],
    providers: [
        GetClientService,
        {
            provide: CLIENTS_REPOSITORY,
            useClass: InMemoryClientsRepository
        },
    ],
})

export class ClientsModule {}