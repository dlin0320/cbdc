# CBDC TW on Corda

This is an implementation of CBDC with the Corda framework. 
This is a simplified version with minimum business logic and feature.

## cbdc-application

This is the application layer, clients and Corda core services can be modified here.

## cbdc-contract

This is the contract layer, persistent states and contracts can be modified here.

## cbdc-flow

This is the flow layer, Corda flows can be modified here.

### Integration (REST API) tests in local environment

#### Corda Nodes (CorDapps)
```bash
# Generate Corda node configurations and build CorDapps
./gradlew deployNodes -x test

# Start all Corda nodes
./build/nodes/runnodes
```
#### Spring Boot application

```bash
# Build 
./gradlew :cbdc-application:bootJar

# Start Spring Boot application
java -jar ./cbdc-application/build/libs/<name-of-application>.jar
```

