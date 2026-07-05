package repository

import "products-api/internal/product/domain"

type InMemoryProductRepository struct {
	products map[string]domain.Product
}

func NewInMemoryProductRepository() *InMemoryProductRepository {
	products := map[string]domain.Product{
		"PRD-001": {
			ProductID:     "PRD-001",
			Name:          "Gaseosa 600ml",
			SKU:           "GAS-600-PET",
			Category:      "Bebidas azucaradas",
			TaxCategory:   domain.TaxCategoryGravado,
			UnitOfMeasure: "UN",
		},
		"PRD-002": {
			ProductID:     "PRD-002",
			Name:          "Chocolate de mesa 250g",
			SKU:           "CHO-250-TAB",
			Category:      "Confiteria",
			TaxCategory:   domain.TaxCategoryGravado,
			UnitOfMeasure: "UN",
		},
		"PRD-003": {
			ProductID:     "PRD-003",
			Name:          "Arroz blanco 1kg",
			SKU:           "ARR-001-KG",
			Category:      "Alimentos basicos",
			TaxCategory:   domain.TaxCategoryReducido,
			UnitOfMeasure: "KG",
		},
		"PRD-004": {
			ProductID:     "PRD-004",
			Name:          "Frijol rojo 500g",
			SKU:           "FRJ-500-BOL",
			Category:      "Alimentos basicos",
			TaxCategory:   domain.TaxCategoryReducido,
			UnitOfMeasure: "UN",
		},
		"PRD-005": {
			ProductID:     "PRD-005",
			Name:          "Agua potable 1L",
			SKU:           "AGU-001-BOT",
			Category:      "Bebidas",
			TaxCategory:   domain.TaxCategoryExento,
			UnitOfMeasure: "UN",
		},
		"PRD-006": {
			ProductID:     "PRD-006",
			Name:          "Medicamento analgesico 10 tabletas",
			SKU:           "MED-ANA-010",
			Category:      "Medicamentos",
			TaxCategory:   domain.TaxCategoryExento,
			UnitOfMeasure: "UN",
		},
		"PRD-007": {
			ProductID:     "PRD-007",
			Name:          "Detergente liquido 1L",
			SKU:           "DET-001-LIQ",
			Category:      "Productos de aseo",
			TaxCategory:   domain.TaxCategoryGravado,
			UnitOfMeasure: "UN",
		},
		"PRD-008": {
			ProductID:     "PRD-008",
			Name:          "Aceite vegetal 900ml",
			SKU:           "ACE-900-BOT",
			Category:      "Alimentos basicos",
			TaxCategory:   domain.TaxCategoryReducido,
			UnitOfMeasure: "UN",
		},
		"PRD-009": {
			ProductID:     "PRD-009",
			Name:          "Fertilizante agricola 5kg",
			SKU:           "FER-005-SAC",
			Category:      "Insumos agricolas",
			TaxCategory:   domain.TaxCategoryReducido,
			UnitOfMeasure: "KG",
		},
		"PRD-010": {
			ProductID:     "PRD-010",
			Name:          "Panela 500g",
			SKU:           "PAN-500-BLO",
			Category:      "Canasta basica exenta",
			TaxCategory:   domain.TaxCategoryExento,
			UnitOfMeasure: "UN",
		},
	}

	return &InMemoryProductRepository{products: products}
}

func (r *InMemoryProductRepository) FindByID(productID string) (*domain.Product, error) {
	product, exists := r.products[productID]

	if !exists {
		return nil, nil
	}

	return &product, nil
}
