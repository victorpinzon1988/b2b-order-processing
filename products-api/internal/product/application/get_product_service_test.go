package application

import (
	"errors"
	"products-api/internal/product/domain"
	"testing"
)

type fakeProductRepository struct {
	product *domain.Product
	err     error
}

func (r fakeProductRepository) FindByID(productID string) (*domain.Product, error) {
	return r.product, r.err
}

func TestGetProductServiceExecuteReturnsProduct(t *testing.T) {
	repository := fakeProductRepository{
		product: &domain.Product{
			ProductID:   "PRD-001",
			Name:        "Gaseosa 600ml",
			SKU:         "GAS-600-PET",
			Category:    "Bebidas azucaradas",
			TaxCategory: domain.TaxCategoryGravado,
		},
	}

	service := NewGetProductService(repository)

	product, err := service.Execute("PRD-001")

	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}

	if product.ProductID != "PRD-001" {
		t.Fatalf("expected product PRD-001, got %s", product.ProductID)
	}
}

func TestGetProductServiceExecuteReturnsNotFound(t *testing.T) {
	service := NewGetProductService(fakeProductRepository{})

	product, err := service.Execute("PRD-404")

	if product != nil {
		t.Fatalf("expected nil product, got %v", product)
	}

	if !errors.Is(err, ErrProductNotFound) {
		t.Fatalf("expected ErrProductNotFound, got %v", err)
	}
}
