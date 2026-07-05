package main

import (
	"log"
	"net/http"
	"os"
	"products-api/internal/product/application"
	"products-api/internal/product/infraestructure/cache"
	producthttp "products-api/internal/product/infraestructure/http"
	"products-api/internal/product/infraestructure/repository"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	redisClient := cache.NewRedisClient()
	inMemoryRepository := repository.NewInMemoryProductRepository()
	productRepository := repository.NewCachedProductRepository(redisClient, inMemoryRepository)
	getProductService := application.NewGetProductService(productRepository)
	ProductHandler := producthttp.NewProductHandler(getProductService)

	mux := http.NewServeMux()

	mux.HandleFunc("/products/", ProductHandler.GetProduct)
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("OK"))
	})

	log.Printf("products-api listening on port %s", port)

	if err := http.ListenAndServe(":"+port, mux); err != nil {
		log.Fatal(err)
	}
}
