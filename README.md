# Backend JaxRS – Gestion de Concerts

## Modèle métier

Application de billetterie de concerts. Un **Organisateur** crée des **Concerts**. Un **Client** peut acheter un ou plusieurs **Tickets** pour un concert.

---

## Diagramme de classes

```
         User  (TABLE_PER_CLASS)
        /        \
   Client       Organiser
     |               |
  ticketsAchetes   concerts  (OneToMany, mappedBy)
     |               |
   Ticket  -----> Concert
  (ManyToOne)   (ManyToOne)
```

### Entités JPA

| Classe      | Table       | Description                               |
|-------------|-------------|-------------------------------------------|
| `User`      | –           | Classe de base avec héritage JPA          |
| `Client`    | `client`    | Acheteur de tickets                       |
| `Organiser` | `organiser` | Organisateur de concerts                  |
| `Concert`   | `concert`   | Événement : lieu, date, capacité          |
| `Ticket`    | `ticket`    | Billet associé à un client et un concert  |

**Héritage** : `User → Client` et `User → Organiser` (stratégie `TABLE_PER_CLASS`)

**Relations bidirectionnelles (mappedBy)** :
- `Concert.ticketsVendus` ↔ `Ticket.concert`
- `Client.ticketsAchetes` ↔ `Ticket.client`
- `Organiser.concerts` ↔ `Concert.organiser`

---

## Architecture

```
domain/          → Entités JPA
dao/generic/     → AbstractJpaDao + 1 DAO par entité
jaxr/services/   → Logique métier (ConcertService, TicketService, ...)
jaxr/dto/        → DTOs pour les entrées API
rest/            → Contrôleurs JAX-RS
```

### Types de requêtes JPA

| Type           | Où          | Méthode                                      |
|----------------|-------------|----------------------------------------------|
| JPQL           | `ConcertDao`| `findByOrganiseur(Long id)`                  |
| Named Query    | `ConcertDao`| `findByLieu(String)` → `Concert.findByLieu`  |
| Criteria Query | `ConcertDao`| `findUpcoming()` (concerts à venir triés)    |
| JPQL           | `TicketDao` | `countByConcert`, `existsByConcertAndPlace`  |

---

## API REST – Endpoints

Serveur : `http://localhost:8080`
Documentation OpenAPI JSON : `GET http://localhost:8080/openapi.json`

### Concerts (`/concerts`) — documentation OpenAPI complète

| Méthode | URL                          | Description                                    |
|---------|------------------------------|------------------------------------------------|
| GET     | `/concerts`                  | Tous les concerts                              |
| GET     | `/concerts/{id}`             | Concert par ID                                 |
| POST    | `/concerts`                  | Créer un concert (`ConcertCreateDTO`)          |
| DELETE  | `/concerts/{id}`             | Supprimer un concert                           |
| GET     | `/concerts/upcoming`         | **Métier** : concerts à venir (Criteria Query) |
| GET     | `/concerts/lieu/{lieu}`      | **Métier** : par lieu (Named Query)            |
| GET     | `/concerts/organiseur/{id}`  | **Métier** : par organisateur (JPQL)           |
| GET     | `/concerts/{id}/tickets`     | **Métier** : tickets vendus pour un concert    |

### Tickets (`/tickets`)

| Méthode | URL                     | Description                                    |
|---------|-------------------------|------------------------------------------------|
| GET     | `/tickets`              | Tous les tickets                               |
| GET     | `/tickets/{id}`         | Ticket par ID                                  |
| POST    | `/tickets`              | Acheter un ticket (`TicketCreateDTO`)          |
| DELETE  | `/tickets/{id}/annuler` | **Métier** : annuler un ticket (ACTIF→ANNULÉ)  |

### Clients (`/clients`)

| Méthode | URL             | Description                          |
|---------|-----------------|--------------------------------------|
| GET     | `/clients`      | Tous les clients                     |
| GET     | `/clients/{id}` | Client par ID                        |
| POST    | `/clients`      | Créer un client (`ClientCreateDTO`)  |
| DELETE  | `/clients/{id}` | Supprimer un client                  |

### Organisateurs (`/organiseurs`)

| Méthode | URL                  | Description                                      |
|---------|----------------------|--------------------------------------------------|
| GET     | `/organiseurs`       | Tous les organisateurs                           |
| GET     | `/organiseurs/{id}`  | Organisateur par ID                              |
| POST    | `/organiseurs`       | Créer un organisateur (`OrganiserCreateDTO`)     |
| DELETE  | `/organiseurs/{id}`  | Supprimer un organisateur                        |

---

## Exemples de corps JSON

### Créer un concert
```json
{
  "organisateurId": 1,
  "lieu": "Zenith Rennes",
  "capacite": 3000,
  "description": "Concert rock",
  "dateTime": "2026-06-15T20:00:00",
  "popularite": 4
}
```

### Acheter un ticket
```json
{
  "utilisateurId": 1,
  "concertId": 1,
  "numeroPlace": 42
}
```

### Créer un client ou organisateur
```json
{
  "name": "Dupont",
  "firstname": "Jean",
  "email": "jean@example.com",
  "password": "motdepasse"
}
```

---

## Lancement

1. Importer le projet dans votre IDE
2. Lancer la classe `RestServer` (main class)
3. Accéder à `http://localhost:8080/openapi.json` pour la doc OpenAPI

La base de données HSQL est créée en mémoire au démarrage (persistence unit `dev`).

---




# Task Open API Integration 

Now, we would like to ensure that our API can be discovered. The OpenAPI Initiative (OAI) was created by a consortium of forward-looking industry experts who recognize the immense value of standardizing on how REST APIs are described. As an open governance structure under the Linux Foundation, the OAI is focused on creating, evolving and promoting a vendor neutral description format. 

APIs form the connecting glue between modern applications. Nearly every application uses APIs to connect with corporate data sources, third party data services or other applications. Creating an open description format for API services that is vendor neutral, portable and open is critical to accelerating the vision of a truly connected world.

To do this integration first, I already add a dependencies to openAPI libraries. 

```xml
		<dependency>
			<groupId>io.swagger.core.v3</groupId>
			<artifactId>swagger-jaxrs2-jakarta</artifactId>
			<version>2.2.15</version>
		</dependency>

		<dependency>
			<groupId>io.swagger.core.v3</groupId>
			<artifactId>swagger-jaxrs2-servlet-initializer-v2</artifactId>
			<version>2.2.15</version>
		</dependency>
```

Next you have to add OpenAPI Resource to your application

Your application could be something like that. 

```java
@ApplicationPath("/")
public class RestApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> resources = new HashSet<>();


		// SWAGGER endpoints
		resources.add(OpenApiResource.class);

        //Your own resources. 
        resources.add(PersonResource.class);
....
		return resources;
	}
}
```

Next start your server, you must have your api description available at [http://localhost:8080/openapi.json](http://localhost:8080/openapi.json)

### Integrate Swagger UI. 

Next we have to integrate Swagger UI. We will first download it.
https://github.com/swagger-api/swagger-ui

Copy dist folder content in src/main/webapp/swagger in your project. 

Edit index.html file to automatically load your openapi.json file. 

At the end of the index.html, your must have something like that.

```js
   // Build a system
      const ui = SwaggerUIBundle({
        url: "http://localhost:8080/openapi.json",
        dom_id: '#swagger-ui',
        
        ...
```

Next add a new resources to create a simple http server when your try to access to http://localhost:8080/api/.

This new resources can be developped as follows

```java
package app.web.rest;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/api")
public class SwaggerResource {

    private static final Logger logger = Logger.getLogger(SwaggerResource.class.getName());

    @GET
    public byte[] Get1() {
        try {
            return Files.readAllBytes(FileSystems.getDefault().getPath("src/main/webapp/swagger/index.html"));
        } catch (IOException e) {
            return null;
        }
    }

    @GET
    @Path("{path:.*}")
    public byte[] Get(@PathParam("path") String path) {
        try {
            return Files.readAllBytes(FileSystems.getDefault().getPath("src/main/webapp/swagger/"+path));
        } catch (IOException e) {
            return null;
        }
    }

}
```

Add this new resources in your application

```java
@ApplicationPath("/")
public class RestApplication extends Application {


	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> resources = new HashSet<>();


		// SWAGGER endpoints
		resources.add(OpenApiResource.class);
		resources.add(PersonResource.class);
        //NEW LINE TO ADD
		resources.add(SwaggerResource.class);

		return resources;
	}
}
```

Restart your server and access to http://localhost:8080/api/, you should access to a swagger ui instance that provides documentation on your api. 

You can follow this guide to show how you can specialise the documentation through annotations.

https://github.com/swagger-api/swagger-samples/blob/2.0/java/java-resteasy-appclasses/src/main/java/io/swagger/sample/resource/PetResource.java