package http

import (
	"net/http"
	"net/http/httptest"
	"products-api/internal/product/application"
	"products-api/internal/product/infraestructure/repository"
	"strings"
	"testing"
)

func TestGetProductReturnsProduct(t *testing.T) {
	repo := repository.NewInMemoryProductRepository()
	service := application.NewGetProductService(repo)
	handler := NewProductHandler(service)

	request := httptest.NewRequest(http.MethodGet, "/products/PRD-001", nil)
	response := httptest.NewRecorder()

	handler.GetProduct(response, request)

	if response.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", response.Code)
	}

	body := response.Body.String()

	if !strings.Contains(body, `"productId":"PRD-001"`) {
		t.Fatalf("expected response body to contain productId PRD-001, got %s", body)
	}

	if !strings.Contains(body, `"taxCategory":"GRAVADO"`) {
		t.Fatalf("expected response body to contain taxCategory GRAVADO, got %s", body)
	}
}

func TestGetProductReturnsNotFound(t *testing.T) {
	repo := repository.NewInMemoryProductRepository()
	service := application.NewGetProductService(repo)
	handler := NewProductHandler(service)

	request := httptest.NewRequest(http.MethodGet, "/products/PRD-404", nil)
	response := httptest.NewRecorder()

	handler.GetProduct(response, request)

	if response.Code != http.StatusNotFound {
		t.Fatalf("expected status 404, got %d", response.Code)
	}

	body := response.Body.String()

	if !strings.Contains(body, "product PRD-404 not found") {
		t.Fatalf("expected not found message, got %s", body)
	}
}
