import { Injectable, Logger, Inject } from "@nestjs/common";
import { ClientsRepository } from "src/clients/application/ports/clients.repository";
import { InMemoryClientsRepository } from "./in-memory-clients.repository";
import { REDIS_CLIENT } from "../cache/redis.provider";
import Redis from "ioredis";
import { Client } from "src/clients/domain/client.entity";

@Injectable()
export class CachedClientsRepository implements ClientsRepository{
    private readonly logger = new Logger(CachedClientsRepository.name)

    constructor(
        private readonly inMemoryClientsRepository: InMemoryClientsRepository,
        @Inject(REDIS_CLIENT)
        private readonly redis: Redis,
    ){}

    async findById(clientId: string): Promise<Client | null> {
        const cacheKey = this.buildCacheKey(clientId)
        
        this.logger.log(`Cache key ${cacheKey} for ${clientId}`)

        const cachedClient = await this.getFromCache(cacheKey);

        if (cachedClient) {
            this.logger.log(`Cache hit ${JSON.stringify(cachedClient)}`)
            return cachedClient;
        }

        this.logger.log(`Cache miss. It will retrieve ${clientId} from database`)

        const client = await this.inMemoryClientsRepository.findById(clientId);

        if(!client){
            return null;
        }
        
        this.logger.log(`Saving ${cacheKey} and ${JSON.stringify(client)} in redis`)

        await this.saveInCache(cacheKey, client)

        return client;
    }

    private buildCacheKey(clientId: string): string{
        const namespace = process.env.CLIENTS_CACHE_NAMESPACE ?? 'clients:v1';

        return `${namespace}:${clientId}`
    }

    private async getFromCache(cacheKey: string): Promise<Client | null>{
        try{
            
            const value = await this.redis.get(cacheKey);

            if(!value){
                return null;
            }

            return JSON.parse(value) as Client;
        }catch(error){
            this.logger.warn(`Redis could not retrieve ${cacheKey}`)
            return null;
        }
    }

    private async saveInCache(cacheKey: string, client: Client): Promise<void>{
        try{
            const ttlSeconds = Number(process.env.CLIENTS_CACHE_TTL_SECONDS ?? 300);

            await this.redis.set(
                cacheKey,
                JSON.stringify(client),
                'EX',
                ttlSeconds
            );

        }catch(error){
            this.logger.warn(`Redis write failed for key ${cacheKey}`)
        }
    }
}