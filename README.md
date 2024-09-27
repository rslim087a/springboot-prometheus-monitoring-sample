# Spring Boot Prometheus Monitoring Sample

This project is a sample Spring Boot application with Prometheus monitoring integration.

## Prerequisites

* Java 11 or higher
* Maven
* Postman (optional, for testing API endpoints)

## Setup

1. Clone the repository:

```bash
git clone https://github.com/yourusername/spring-boot-prometheus-monitoring-sample.git

cd spring-boot-prometheus-monitoring-sample
```

2. Build the project:

```bash
mvn clean install
```

## Running the Application

To run the application, use the following command:

```bash
mvn spring-boot:run
```

The application will start and be available at `http://localhost:8080`.

## API Endpoints

The following endpoints are available:

* `GET /`: Root endpoint
* `POST /items`: Create a new item
* `GET /items/{item_id}`: Retrieve an item
* `PUT /items/{item_id}`: Update an item
* `DELETE /items/{item_id}`: Delete an item
* `GET /actuator/prometheus`: Prometheus metrics endpoint

## Testing with Postman

A Postman collection is provided for testing the API endpoints. To use it:

1. Open Postman
2. Click on "Import" and select the `spring-boot-metrics-postman-collection.json` file
3. The collection "Spring Boot Metrics App" will be added to your Postman workspace
4. You can now use the following requests to test each endpoint:
   * Root: GET request to test the root endpoint
   * Create Item: POST request to create a new item
   * Get Item: GET request to retrieve an item by ID
   * Update Item: PUT request to update an existing item
   * Delete Item: DELETE request to remove an item
   * Get Metrics: GET request to fetch Prometheus metrics

## Monitoring

The application exposes Prometheus metrics at the `/actuator/prometheus` endpoint. You can configure your Prometheus server to scrape these metrics for monitoring.

## Development

If you want to make changes to the project:

1. Make your changes to the `MetricsApplication.java` file or other relevant files.
2. Rebuild the project using `mvn clean install`.
3. Run the application to test your changes.

## Troubleshooting

If you encounter any issues:

1. Ensure you're using Java 11 or higher:

```bash
java -version
```

2. Make sure all dependencies are correctly installed:

```bash
mvn dependency:resolve
```

3. Check that you're in the correct directory and running the commands from the project root.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.