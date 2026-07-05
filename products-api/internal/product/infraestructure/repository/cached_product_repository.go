package repository

import (
	"context"
	"encoding/json"
	"log"
	"os"
	"products-api/internal/product/domain"
	"strconv"
	"time"

	"github.com/redis/go-redis/v9"
)

type CachedProductRepository struct {
	redisClient *redis.Client
	source      *InMemoryProductRepository
}

func NewCachedProductRepository(redisClient *redis.Client,
	source *InMemoryProductRepository) *CachedProductRepository {

	return &CachedProductRepository{
		redisClient: redisClient,
		source:      source,
	}
}

func (r *CachedProductRepository) FindByID(productID string) (*domain.Product, error) {

	ctx := context.Background()
	cacheKey := r.buildCacheKey(productID)

	log.Printf("Cache key %s for %s", cacheKey, productID)

	cachedProduct, err := r.getFromCache(ctx, cacheKey)
	if err == nil && cachedProduct != nil {
		log.Printf("Cache hit %s", cachedProduct.ProductID)
		return cachedProduct, nil
	}

	log.Printf("Cache miss. It will retrieve %s from database", productID)

	product, err := r.source.FindByID(productID)
	if err != nil {
		return nil, err
	}

	if product == nil {
		return nil, nil
	}

	log.Printf("Saving %s and %s in redis", cacheKey, product.ProductID)

	if err := r.saveInCache(ctx, cacheKey, product); err != nil {
		log.Printf("Redis write failed for key %s: %v", cacheKey, err)
	}

	return product, nil
}

func (r *CachedProductRepository) buildCacheKey(productID string) string {
	namespace := os.Getenv("PRODUCTS_CACHE_NAMESPACE")
	if namespace == "" {
		namespace = "products:v1"
	}

	return namespace + ":" + productID
}

func (r *CachedProductRepository) getFromCache(
	ctx context.Context,
	cacheKey string,
) (*domain.Product, error) {
	value, err := r.redisClient.Get(ctx, cacheKey).Result()

	if err == redis.Nil {
		return nil, nil
	}

	if err != nil {
		log.Printf("redis read failed for key %s: %v", cacheKey, err)
		return nil, err
	}

	var product domain.Product
	if err := json.Unmarshal([]byte(value), &product); err != nil {
		log.Printf("Redis value could not be parsed for key %s: %v", cacheKey, err)
		return nil, err
	}

	return &product, nil
}

func (r *CachedProductRepository) saveInCache(
	ctx context.Context,
	cacheKey string,
	product *domain.Product,
) error {
	payload, err := json.Marshal(product)
	if err != nil {
		return err
	}

	ttl := cacheTTL()

	return r.redisClient.Set(ctx, cacheKey, payload, ttl).Err()

}

func cacheTTL() time.Duration {
	value := os.Getenv("PRODUCTS_CACHE_TTL_SECONDS")
	if value == "" {
		return 300 * time.Second
	}

	seconds, err := strconv.Atoi(value)
	if err != nil || seconds <= 0 {
		return 300 * time.Second
	}

	return time.Duration(seconds) * time.Second
}
