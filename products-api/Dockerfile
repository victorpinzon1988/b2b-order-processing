FROM golang:1.24-alpine AS build

WORKDIR /app

COPY go.mod ./
RUN go mod download

COPY . .
RUN go build -o products-api ./cmd/api

FROM alpine:3.20

WORKDIR /app

COPY --from=build /app/products-api ./products-api

EXPOSE 8081

CMD [ "./products-api" ]