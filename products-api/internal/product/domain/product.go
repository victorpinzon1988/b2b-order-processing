package domain

type TaxCategory string

const (
	TaxCategoryGravado  TaxCategory = "GRAVADO"
	TaxCategoryReducido TaxCategory = "REDUCIDO"
	TaxCategoryExento   TaxCategory = "EXENTO"
)

type Product struct {
	ProductID     string      `json:"productId"`
	Name          string      `json:"name"`
	SKU           string      `json:"sku"`
	Category      string      `json:"category"`
	TaxCategory   TaxCategory `json:"taxCategory"`
	UnitOfMeasure string      `json:"unitOfMeasure"`
}
