# Stock Market Simulation

An illustrative implementation of a backend RESTful service using Scala, Finatra and a custom in-memory data structure.

## Structure of Source Code
```
├───data					      |-> Data files (loaded at startup, in-memory data store)
├───src						      |-> Source root for Scala SBT project
│   ├───main					      |-> Main source
│   │   ├───resources			              |-> Resource files
│   │   └───scala				      |-> Root for scala source files
│   │       └───com
│   │           └───komlan
│   │               └───lab
│   │                   └───market		      
│   │                       ├───api		      |-> Rest API (Restfull controller, defining endpoints) 
│   │                       ├───domain		      |-> API resources (i.e. Domain entities): User, Stock, StockPosition, Trade, ...
│   │                       │   └───http	      |-> Http resources: Request object specification and validation
│   │                       ├───modules		      |-> Finagle/Finatra module
│   │                       ├───services	      |-> Contains a SetupService run at startup to seed data store
│   │                       └───utils		      |-> Utility classes
│   └───test
│       ├───resources
│       └───scala
│           └───com
│               └───komlan
│                   └───lab
│                       └───market
│                           ├───api		       |-> Api Test (illustration only, not full test coverage)
│                           └───utils		      |-> Test for utilities

```

## Running

### Running with Docker
A Dockerfile is included to build a single local docker image. All files and data needed are included but can be replace by mounted volume.

    * `docker build -t stock-server .`
    * `docker run -p 5000:80 stock-server`

Then query the service endpoints using, for example, `http://localhost:5000/users`. 

## Features Demonstrated
- REST endpoint using Scala and Finatra (based on Finagle)
- Repository pattern with a concreate implementation of an in-memory data store
- CRUD operations for User, Stock, Trade 

### Endpoints

```
GET     /users
GET     /users/:id
DELETE  /users/:id
POST    /users
GET     /stocks
GET     /stocks/:symbol
GET     /stocks/:symbol/quotes
POST    /stocks
GET     /users/:userId/portfolio/evaluate
GET     /users/:userId/trades
POST    /users/:userId/trades
POST    /users/:userId/trades/upload

```
## Semantic of Domain Entities
- User: A user of the app
- Stock: Designation of a particular stock identified by a ticker symbol
- StockQuote: The pricing of a particular stock on a given date/time
- Trade: The bying or selling of a stock by a user
- StockPosition: A user's ownership of some amount of stock. 
- Portfolio: The totality (or grouping) of a user's currently owned stocks, represented either by a list of StockPosition's or historically by a sequence of past trades.


## Limitations
Due to time and scope limitation, many apsects of this application are illustrative in nature.  
- There is no front-end included. The best way to test the API is by using `Postnam` or similar tools
- API implementation is not fully fleshed out. The emphasis was put on demonstrating various aspects of the implementation as basis for broader conversation.
- Error handling and edge case handling have been included but are not comprehensive.

