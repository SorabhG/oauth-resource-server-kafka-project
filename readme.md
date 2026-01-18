                +----------------------+
                |   Auth Server (9000) |
                | issues JWT tokens    |
                +----------+-----------+
                           |
                           v
REST Call with Bearer Token (JWT)
|
+-------------v-------------------+
|    Resource Server (8081)       |
|  - Protected endpoints          |
|  - Kafka Producer (send events) |
+-------------+-------------------+
|
Kafka Cluster (localhost:9092)
|
+------------------+--------------------+
|                                       |
+-------v----------+                +-----------v----------+
| Inventory Service|                | Email Service        |
| Kafka Consumer   |                | Kafka Consumer       |
+------------------+                +----------------------+



How to Test End-to-End

Start Zookeeper and Kafka.

Start your Auth Server (port 9000).

Start your Resource Server (port 8081).

Obtain an access token (via /oauth2/token).

Call your endpoint:

curl -X POST http://localhost:8081/orders/123 \
-H "Authorization: Bearer <ACCESS_TOKEN>"

(1) Client (Postman / cURL)
│
▼
(2) Auth Server  (localhost:9000)
│ Issues JWT Access Token
▼
(3) Resource Server  (localhost:8081)
│ Publishes order event to Kafka topic: order-events
▼
(4) Kafka Broker  (localhost:9092)
│
├── InventoryConsumer  → just logs
└── BillingConsumer    → generates invoice & publishes invoice-events
▼
Kafka topic: invoice-events
▼
(5) Notification Service  (localhost:8083)
│ Consumes invoice-events
│ Calls (secured) User Service
▼
(6) User Service  (localhost:8084)
→ Prints “📩 User notified with invoice...


| Service                 | Secured Endpoint | Acts As                    | Needs Token?                 |
| ----------------------- | ---------------- | -------------------------- | ---------------------------- |
| Auth Server             | `/oauth2/*`      | Authorization Server       | N/A                          |
| Resource Server (Order) | `/orders/**`     | Resource Server + Producer | ✅ (user token)               |
| Billing Service         | —                | Consumer + Producer        | ❌                            |
| Notification Service    | —                | Consumer + Client          | ✅ (client_credentials token) |
| User Service            | `/notify-user`   | Resource Server            | ✅ (validates machine token)  |

2. Why This Design Is Actually Cleaner

This is closer to what many enterprise architectures look like:
| Layer                    | Responsibility                                                   | OAuth2?                 | Transport     |
| ------------------------ | ---------------------------------------------------------------- | ----------------------- | ------------- |
| **Order Service**        | Handles REST API requests from UI/app, emits domain events       | ✅ Yes (User tokens)     | HTTP → Kafka  |
| **Billing Service**      | Processes order events, generates invoices, emits invoice events | ❌ No                    | Kafka → Kafka |
| **Notification Service** | Processes invoice events, notifies users via REST call           | ✅ Yes (machine token)   | Kafka → HTTP  |
| **User Service**         | Receives notification requests, logs/sends email/SMS             | ✅ Yes (Resource Server) | HTTP          |

So only public HTTP services (Order, Notification, User) need OAuth2.
Internal Kafka processors (Billing, Analytics, Inventory, etc.) do not — unless you use Kafka ACLs.


| Concept               | Meaning                                                              | In our Example                                        |
| --------------------- | -------------------------------------------------------------------- | ----------------------------------------------------- |
| **Topic**             | Stream of related messages                                           | `order-events`                                        |
| **Partition**         | Parallelism unit — multiple consumers in same group split partitions | `3 partitions`                                        |
| **Consumer Group**    | A group of consumers that together consume a topic                   | `inventory-group`, `email-group`                      |
| **Replayability**     | Kafka retains messages — new consumers can re-read old messages      | If Inventory restarts, it can replay all order events |
| **Publish/Subscribe** | Multiple independent groups can each consume all events              | Email + Analytics + Inventory each get all events     |
