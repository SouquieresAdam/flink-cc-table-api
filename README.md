# Confluent Cloud Flink Table API Demo

This project demonstrates advanced Confluent Cloud Flink Table API operations including managed table creation, schema management, and metadata operations using the official Confluent Flink plugin.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Confluent Cloud account with:
  - Flink compute pools enabled
  - A Kafka cluster for table backing
  - Schema Registry enabled
  - Appropriate permissions for table creation

## Project Structure

```
flink-cc-table-api/
├── .gitignore                                 # Git ignore rules (excludes credentials)
├── pom.xml                                    # Maven configuration with Confluent plugin
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── io/confluent/flink/examples/
        │       └── FlinkCCTableApiDemo.java        # Managed table operations demo
        └── resources/
            ├── cloud.properties                    # Your Confluent Cloud config (git-ignored)
            ├── cloud.properties.template           # Configuration template
            └── log4j2.xml                         # Logging configuration
```

## Setup

1. **Clone and build the project:**
   ```bash
   mvn clean compile
   ```

2. **Configure Confluent Cloud:**
   - Copy `src/main/resources/cloud.properties.template` to `src/main/resources/cloud.properties`
   - Update the demo constants in `FlinkCCTableApiDemo.java`:
     ```java
     static final String TARGET_CATALOG = "your-catalog-name";
     static final String TARGET_DATABASE = "your-kafka-cluster-name";
     ```
   - Fill in your Confluent Cloud credentials in `cloud.properties`:
     ```properties
     client.cloud=azure                    # your cloud provider
     client.region=northeurope              # your region
     client.organization-id=your-org-id
     client.environment-id=env-xxxxx
     client.compute-pool-id=lfcp-xxxxxxxxxx
     client.flink-api-key=your-api-key
     client.flink-api-secret=your-api-secret
     ```

## Running the Demo

### Managed Table Operations Demo
```bash
mvn exec:java -Dexec.mainClass="io.confluent.flink.examples.FlinkCCTableApiDemo"
```

### Build and Run JAR
```bash
mvn clean package
java -jar target/flink-cc-table-api-1.0.0.jar
```

## Key Features

### 1. Confluent Cloud Integration
- Uses `ConfluentSettings.fromResource()` for cloud configuration
- Connects directly to Confluent Cloud Flink compute pools
- Proper authentication and catalog/database management
- Conditional Table creation/update

## What the Demo Does

### FlinkCCTableApiDemo
1. **Environment Setup**: Connects to Confluent Cloud and selects catalog/database
2. **Table Discovery**: Lists all available tables in the environment
3. **Managed Table Creation**: Creates two tables (`flink_in`, `flink_out`) with:
   - User schema (user_id, name, email)
   - 4-partition distribution by user_id
   - JSON format with Schema Registry
   - Delete retention policy
4. **Schema Evolution**: Adds Kafka headers metadata column if not present
5. **Error Handling**: Comprehensive exception handling for cloud operations

### Table Configuration Details
- **Backing Storage**: Each table backed by a Kafka topic with same name
- **Partitioning**: 4 partitions distributed by `user_id` field
- **Formats**: JSON with Schema Registry for both key and value
- **Retention**: Delete retention (for streaming use cases)
- **Metadata**: Kafka headers accessible as MAP<BYTES, BYTES>

## Configuration

The `cloud.properties` file uses Confluent Cloud specific parameters:
- **client.cloud**: Cloud provider (aws, gcp, azure)
- **client.region**: Cloud region
- **client.organization-id**: Your Confluent organization ID
- **client.environment-id**: Environment ID
- **client.compute-pool-id**: Flink compute pool ID
- **client.flink-api-key**: Flink API key
- **client.flink-api-secret**: Flink API secret

## Key Dependencies

- **Apache Flink Table API**: `flink-table-api-java:1.20.1`
- **Confluent Plugin**: `confluent-flink-table-api-java-plugin:1.20-52`
- **Confluent Repository**: `https://packages.confluent.io/maven/`

## Security Notes

- `cloud.properties` is git-ignored to prevent credential exposure
- Use environment variables or secure credential management in production
- API keys should have minimal required permissions

## Troubleshooting

- **Authentication errors**: Verify your API keys and secrets in `cloud.properties`
- **Catalog/Database not found**: Update `TARGET_CATALOG` and `TARGET_DATABASE` constants
- **Permission errors**: Ensure your API key has table creation permissions
- **Schema Registry errors**: Verify Schema Registry is enabled in your environment
- **Network issues**: Check compute pool accessibility and region settings
- **Compilation errors**: Ensure you're using Java 11+ and Maven 3.6+