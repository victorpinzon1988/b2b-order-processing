import { Inject, Injectable, NotFoundException } from "@nestjs/common";
import { CLIENTS_REPOSITORY} from "../ports/clients.repository";
import type { ClientsRepository } from "../ports/clients.repository";
import { Client } from "src/clients/domain/client.entity";


@Injectable()
export class GetClientService{

    constructor(
        @Inject(CLIENTS_REPOSITORY)
        private readonly clientsRepository: ClientsRepository,
    ){}

    async execute(clientId: string): Promise<Client>{
        const client = await this.clientsRepository.findById(clientId);

        if(!client){
            throw new NotFoundException(`Client ${clientId} not found`);
        }

        return client;
    }
}