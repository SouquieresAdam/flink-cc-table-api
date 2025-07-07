package io.confluent.flink.examples;

import io.confluent.flink.plugin.ConfluentSettings;
import io.confluent.flink.plugin.ConfluentTableDescriptor;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableEnvironment;

import java.util.Arrays;

public class FlinkCCTableApiDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Confluent Cloud Flink Table API Demo ===\n");

        runKafkaTableExample();
    }

    static final String TARGET_CATALOG = "Adam";
    // Fill this with a Kafka cluster you have write access to
    static final String TARGET_DATABASE = "asouquieres-standard-azure-flink-playground";

    static final String TARGET_TABLE1 = "flink_in";
    static final String TARGET_TABLE2 = "flink_out";

    static final String NON_EXISTANT_TABLE = "NON_EXISTANT_TABLE";


    private static void runKafkaTableExample() throws Exception {
        System.out.println("1. Kafka Table Integration Example...");

        EnvironmentSettings settings = ConfluentSettings.fromResource("/cloud.properties");
        TableEnvironment env = TableEnvironment.create(settings);

        env.useCatalog(TARGET_CATALOG);
        env.useDatabase(TARGET_DATABASE);

        try {
            var tables = env.listTables();

            System.out.println("Available table list :");
            Arrays.stream(tables).forEach(System.out::println);



            var table1Exists = Arrays.asList(tables).contains(TARGET_TABLE1);
            var table2Exists = Arrays.asList(tables).contains(TARGET_TABLE2);


            if(!table1Exists) {

                // Create a table programmatically:
                // The table...
                //   - is backed by an equally named Kafka topic (delete retention)
                //   - stores its payload in JSON
                //   - will reference two Schema Registry subjects for Kafka message key and value
                //   - is distributed across 4 Kafka partitions based on the Kafka message key "user_id"
                env.createTable(
                        TARGET_TABLE1,
                        ConfluentTableDescriptor.forManaged()
                                .schema(
                                        Schema.newBuilder()
                                                .column("user_id", DataTypes.STRING())
                                                .column("name", DataTypes.STRING())
                                                .column("email", DataTypes.STRING())
                                                .build())
                                .distributedBy(4, "user_id")
                                .option("kafka.retention.time", "0")
                                .option("key.format", "json-registry")
                                .option("value.format", "json-registry")
                                .build());

                System.out.println("Creating table... " + TARGET_TABLE2);
            }

            if(!table2Exists) {

                env.createTable(
                        TARGET_TABLE2,
                        ConfluentTableDescriptor.forManaged()
                                .schema(
                                        Schema.newBuilder()
                                                .column("user_id", DataTypes.STRING())
                                                .column("name", DataTypes.STRING())
                                                .column("email", DataTypes.STRING())
                                                .build())
                                .distributedBy(4, "user_id")
                                .option("kafka.retention.time", "0")
                                .option("key.format", "json-registry")
                                .option("value.format", "json-registry")
                                .build());
            }


            var newColExists = env.from(TARGET_TABLE1).getResolvedSchema().getColumns()
                    .stream().filter(col -> "headers".equals(col.getName())).findAny();
            if(newColExists.isEmpty()){
                env.executeSql("ALTER TABLE "+TARGET_TABLE1+" ADD headers MAP<BYTES, BYTES> METADATA").await();
                System.out.println("Header column added to table "+TARGET_TABLE1);
            }



        } catch (Exception e) {
            System.out.println("exception : "+e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✓ Kafka table example completed\n");
    }
}
