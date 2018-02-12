# Revolut Test Task #

I have used Play Framework and Scala for this task, H2 database as in-memory datastore and Slick for ORM.

You need SBT (I use version 1.1.0) to run it. Command `sbt run` will start server on localhost:9000. 
Also you can build an executable jar by `sbt assembly`. The jar will be located in `target/scala-<scala-compilier-version>/revolut-assembly-1.0.jar`.
You can run it by the following command: `java -Dplay.http.secret.key=revolut -jar revolut-assembly-1.0.jar`. 
Command `sbt test` will run all test in current project.

It has exposes 6 endpoints (all of them starts with `/api/v1`): 
1. GET `/accounts` - list of all account. 
2. GET `/account/{id}` - information about account with id = `id`
3. POST `/account` - create new account. Example of usage: `curl --include -X POST --header "Content-Type: application/json" --data '{"name": "some name", "amount": 3.80}' http://localhost:9000/api/v1/account`
4. POST `/account/{fromId}/transfer/{toId}` - transfer money from account `fromId` to account `toId`. Example of usage: `curl --include -X POST --header "Content-Type: application/json" --data '{"amount": 2.40}' http://localhost:9000/api/v1/account/1/transaction/2`
5. GET `/transaction/{id}` - information about transaction with id = `id`
6. GET `/transactions{accId}` - list of all transactions for account with id = `accId`

All of endpoints return a json:
1. `{"status": "OK", "entity": {...}}` in case of success
2. `{"status": "FAIL", "error": "<message about error here>"` in case of error

Sql-script `conf/evolutions/1.sql` are executed on the application start. It creates db scheme and populates account table with two entries.  
