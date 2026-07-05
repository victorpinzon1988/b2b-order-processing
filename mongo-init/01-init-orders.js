db = db.getSiblingDB('orders_db')

db.createCollection('enriched_orders')

db.enriched_orders.createIndex(
    {orderId: 1},
    {
        unique: true,
        name: 'idx_enriched_orders_order_id_unique',
    }
);

db.enriched_orders.createIndex(
    {status: 1},
    {
        name: 'idx_enriched_orders_status'
    }
);

db.enriched_orders.createIndex(
    {processedAt: -1},
    {
        name: 'idx_enriched_orders_processed_at',
    }
);
