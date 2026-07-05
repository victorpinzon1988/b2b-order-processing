package application

import (
	"errors"
	"products-api/internal/product/domain"
)

var ErrProductNotFound = errors.New("product not found")

type ProductRepository interface {
	FindByID(productID string) (*domain.Product, error)
}

type GetProductService struct {
	repository ProductRepository
}

func NewGetProductService(repository ProductRepository) *GetProductService {
	return &GetProductService{repository: repository}
}

func (s *GetProductService) Execute(ProductID string) (*domain.Product, error) {
	product, err := s.repository.FindByID(ProductID)

	if err != nil {
		return nil, err
	}

	if product == nil {
		return nil, ErrProductNotFound
	}

	return product, nil
}
