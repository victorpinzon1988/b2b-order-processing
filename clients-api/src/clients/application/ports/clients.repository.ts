import { Client } from "src/clients/domain/client.entity";

export const CLIENTS_REPOSITORY = Symbol('CLIENTS_REPOSITORY');

export interface ClientsRepository {
    findById(clientId: string): Promise<Client | null>;
}