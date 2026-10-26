package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class TrilhaResourceTest {

    private Integer criar(String caminho, String json) {
        return given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post(caminho)
                .then()
                .statusCode(201)
                .extract().path("id");
    }

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

    @Test
    void definirERemoverDocenteResponsavel() {
        Integer trilha = criar("/trilhas", """
                {"nome": "Cloud", "cargaHoraria": 40}
                """);
        Integer docente = criar("/docentes", """
                {"nome": "Eva Prado", "email": "eva@ada.tech", "especialidade": "AWS"}
                """);

        given().when().put("/trilhas/" + trilha + "/docente/" + docente)
                .then()
                .statusCode(200)
                .body("docente.id", equalTo(docente))
                .body("docente.nome", equalTo("Eva Prado"));

        // docente responsável por uma trilha não pode ser removido
        given().when().delete("/docentes/" + docente).then().statusCode(409);

        given().when().delete("/trilhas/" + trilha + "/docente").then().statusCode(204);
        given().when().get("/trilhas/" + trilha).then().statusCode(200).body("docente", nullValue());

        given().when().delete("/docentes/" + docente).then().statusCode(204);
    }

    @Test
    void docenteInexistenteRetorna404() {
        given().when().put("/trilhas/1/docente/9999").then().statusCode(404);
    }

    @Test
    void matricularEDesmatricularAluno() {
        Integer trilha = criar("/trilhas", """
                {"nome": "Dados", "cargaHoraria": 50}
                """);
        Integer aluno = criar("/alunos", """
                {"nome": "Fabio Reis", "email": "fabio@email.com"}
                """);

        given().when().post("/trilhas/" + trilha + "/alunos/" + aluno)
                .then()
                .statusCode(200)
                .body("alunos.id", hasItem(aluno));

        // matricular de novo é conflito
        given().when().post("/trilhas/" + trilha + "/alunos/" + aluno).then().statusCode(409);

        // aluno matriculado não pode ser removido
        given().when().delete("/alunos/" + aluno).then().statusCode(409);

        given().when().delete("/trilhas/" + trilha + "/alunos/" + aluno).then().statusCode(204);
        given().when().get("/trilhas/" + trilha).then().statusCode(200).body("alunos", empty());

        given().when().delete("/alunos/" + aluno).then().statusCode(204);
    }

    @Test
    void alunoInexistenteRetorna404() {
        given().when().post("/trilhas/1/alunos/9999").then().statusCode(404);
    }

    @Test
    void cargaHorariaAcimaDoLimiteRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Maratona", "cargaHoraria": 1001}
                        """)
                .when().post("/trilhas")
                .then()
                .statusCode(400);
    }

    @Test
    void nomeMuitoLongoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "%s", "cargaHoraria": 10}
                        """.formatted("x".repeat(101)))
                .when().post("/trilhas")
                .then()
                .statusCode(400);
    }
}
