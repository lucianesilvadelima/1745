package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class DocenteResourceTest {

    @Test
    void cicloCompletoDoCrud() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Elisa", "email": "elisa@ada.tech", "especialidade": "Front-end"}
                        """)
                .when().post("/docentes")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Elisa Martins", "email": "elisa@ada.tech", "especialidade": "React"}
                        """)
                .when().put("/docentes/" + id)
                .then()
                .statusCode(200)
                .body("especialidade", equalTo("React"));

        given().when().delete("/docentes/" + id).then().statusCode(204);
        given().when().get("/docentes/" + id).then().statusCode(404);
    }

    @Test
    void putSemEspecialidadeRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Carla", "email": "carla@ada.tech"}
                        """)
                .when().put("/docentes/1")
                .then()
                .statusCode(400);
    }
}
