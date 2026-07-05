import Redis from "ioredis"

export const REDIS_CLIENT = Symbol('REDIS_CLIENT')

export const redisProvider = {
    provide: REDIS_CLIENT,
    useFactory: () => {
        const host = process.env.REDIS_HOST ?? 'localhost';
        const port = Number(process.env.REDIS_PORT ?? 6379);

        return new Redis({
            host,
            port,
            lazyConnect: false,
            maxRetriesPerRequest: 2,
        });
    }
}