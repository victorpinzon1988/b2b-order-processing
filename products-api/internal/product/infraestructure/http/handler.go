package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"products-api/internal/product/application"
	"strings"
)

type ProductHandler struct {
	getProductService *application.GetProductService
}

func NewProductHandler(getProductService *application.GetProductService) *ProductHandler {
	return &ProductHandler{getProductService: getProductService}
}

func (h *ProductHandler) GetProduct(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodGet {
		productID := strings.TrimPrefix(r.URL.Path, "/products/")
		if productID == "" || productID == r.URL.Path {
			writeJSON(w, http.StatusNotFound, map[string]string{
				"message": "route not found",
			})
			return
		}

		product, err := h.getProductService.Execute(productID)
		if errors.Is(err, application.ErrProductNotFound) {
			writeJSON(w, http.StatusNotFound, map[string]string{
				"message": "product " + productID + " not found",
			})
			return
		}

		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{
				"message": "internal server error",
			})
			return
		}

		writeJSON(w, http.StatusOK, product)
	} else {
		writeJSON(w, http.StatusMethodNotAllowed, map[string]string{
			"message": "Method not allowed",
		})
		return
	}

}

func writeJSON(w http.ResponseWriter, StatusCode int, body any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(StatusCode)
	_ = json.NewEncoder(w).Encode(body)
}
