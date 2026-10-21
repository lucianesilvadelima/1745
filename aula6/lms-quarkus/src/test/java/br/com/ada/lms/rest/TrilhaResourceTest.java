package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class TrilhaResourceTest {

    @Test
    void cicloCompletoDoCrud() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "DevOps", "descricao": "CI/CD e containers", "cargaHoraria": 60}
                        """)
                .when().post("/trilhas")
                .then()
                .statusCode(201)
                .extract().path("id");

        given().when().get("/trilhas").then().statusCode(200).body("nome", hasItem("DevOps"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"cargaHoraria": 90}
                        """)
                .when().patch("/trilhas/" + id)
                .then()
                .statusCode(200)
                .body("cargaHoraria", equalTo(90))
                .body("nome", equalTo("DevOps"));

        given().when().delete("/trilhas/" + id).then().statusCode(204);
        given().when().get("/trilhas/" + id).then().statusCode(404);
    }

    @Test
    void cargaHorariaNegativaRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Inválida", "cargaHoraria": -10}
                        """)
                .when().post("/trilhas")
                .then()
                .statusCode(400);
    }

    @Test
    void nomeRepetidoRetorna409() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Back-end Java", "cargaHoraria": 10}
                        """)
                .when().post("/trilhas")
                .then()
                .statusCode(409);
    }
}
