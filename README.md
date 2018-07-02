# Microsoft Azure CosmosDB Java API

Building on the examples from https://github.com/Azure/azure-cosmosdb-java

Main themes of this project

Java 1.8
Lambdas
Reactive Extensions (rx) and Observables
Concurrency

Being stuck on Java 6 and 7 for a long time, this was an attempt to explore some more advanced features of Java 8 and build a simple set of tools around the Azure Cosmos DB API provided on Github.
 
This API tries to hide as much of the complexity as possible. It supports the more advanced asynchronous reactive calls while easily enabling the developer to switch to more traditional blocking calls if required with a simple switch in the API. There are tradeoffs for simplicity, naturally, but this was an interesting exercise using some tech that was new to me.