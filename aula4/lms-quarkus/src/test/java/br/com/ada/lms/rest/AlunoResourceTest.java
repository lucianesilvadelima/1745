package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Teste de integração: sobe a aplicação e faz requisições HTTP de verdade,
 * conferindo o status (2xx/4xx) e o JSON de resposta.
 */
@QuarkusTest
class AlunoResourceTest {

    private Integer criarAluno(String nome, String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "%s", "email": "%s"}
                        """.formatted(nome, email))
                .when().post("/alunos")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void getListaRetorna200() {
        given()
                .when().get("/alunos")
                .then()
                .statusCode(200)
                .body("email", hasItem("ana@email.com"));
    }

    @Test
    void getPorIdInexistenteRetorna404() {
        given()
                .when().get("/alunos/9999")
                .then()
                .statusCode(404)
                .body("erro", containsString("9999"));
    }

    @Test
    void postRetorna201ComLocation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Carlos", "email": "carlos@email.com"}
                        """)
                .when().post("/alunos")
                .then()
                .statusCode(201)
                .header("Location", containsString("/alunos/"))
                .body("id", notNullValue())
                .body("nome", equalTo("Carlos"));
    }

    @Test
    void postInvalidoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "", "email": "nao-e-email"}
                        """)
                .when().post("/alunos")
                .then()
                .statusCode(400);
    }

    @Test
    void postSemCorpoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/alunos")
                .then()
                .statusCode(400);
    }

    @Test
    void postComEmailRepetidoRetorna409() {
        criarAluno("Duplicado", "duplicado@email.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Outro", "email": "duplicado@email.com"}
                        """)
                .when().post("/alunos")
                .then()
                .statusCode(409);
    }

    @Test
    void postComContentTypeErradoRetorna415() {
        given()
                .contentType(ContentType.TEXT)
                .body("nome=Ana")
                .when().post("/alunos")
                .then()
                .statusCode(415);
    }

    @Test
    void putSubstituiTodosOsCampos() {
        Integer id = criarAluno("Para PUT", "put@email.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Nome Novo", "email": "put-novo@email.com"}
                        """)
                .when().put("/alunos/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Nome Novo"))
                .body("email", equalTo("put-novo@email.com"));
    }

    @Test
    void putIncompletoRetorna400() {
        Integer id = criarAluno("Put Incompleto", "put-incompleto@email.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Só o nome"}
                        """)
                .when().put("/alunos/" + id)
                .then()
                .statusCode(400);
    }

    @Test
    void patchAlteraSoOsCamposEnviados() {
        Integer id = criarAluno("Para PATCH", "patch@email.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Só o nome mudou"}
                        """)
                .when().patch("/alunos/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Só o nome mudou"))
                .body("email", equalTo("patch@email.com"));
    }

    @Test
    void patchComEmailInvalidoRetorna400() {
        Integer id = criarAluno("Patch Inválido", "patch-invalido@email.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email": "sem-arroba"}
                        """)
                .when().patch("/alunos/" + id)
                .then()
                .statusCode(400)
                .body("erro", equalTo("email inválido"));
    }

    @Test
    void patchInexistenteRetorna404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Ninguém"}
                        """)
                .when().patch("/alunos/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteRetorna204EDepois404() {
        Integer id = criarAluno("Para DELETE", "delete@email.com");

        given().when().delete("/alunos/" + id).then().statusCode(204);
        given().when().get("/alunos/" + id).then().statusCode(404);
        given().when().delete("/alunos/" + id).then().statusCode(404);
    }
}
