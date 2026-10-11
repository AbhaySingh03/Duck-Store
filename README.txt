Duck Store

1. Run it locally (without Docker)

Requirements: JDK 17+, Maven 3.9+, PostgreSQL 14+.

1. Database
psql -U postgres -c "CREATE DATABASE duckstore;"

2. APP (UI + API on http://localhost:8080)
cd backend
export DB_USER=postgres DB_PASSWORD=<your password>     # optional, defaults are postgres/postgres
mvn spring-boot:run


Environment variables: `DB_URL` (default `jdbc:postgresql://localhost:5432/duckstore`), `DB_USER`, `DB_PASSWORD`, `PORT`.

To run the packaged jar instead: `mvn package` then `java -jar target/duck-store-1.0.0.jar`.

2. Run it (Docker)

Requirements: Docker with Compose v2.


cd duck-store
docker compose up --buildß


Open "http://localhost:8080" for the warehouse UI. The API is at "http://localhost:8080/api/ducks" and for orders "http://localhost:8081/api/orders".

The first build takes a few minutes (Maven downloads). Flyway creates the database schema automatically.
Data is kept in the `db-data` volume; `docker compose down -v` wipes it.
	