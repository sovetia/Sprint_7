import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierTest {
    Response response;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    // Тест 1. Курьера можно создать
    @Test
    @DisplayName("Яндекс Самокат. Курьера можно создать")
    @Description("Успешный запрос возвращает ok: true; и статус код 201")
    public void CheckCreateCourier(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13","1234","Courier");
        response = createCourier(courier);
        //запрос возвращает правильный код ответа
        checkResponseStatusCode(response, 201);
        //успешный запрос возвращает ok: true;
        checkResponseCreateCourier(response);
        //удаляем курьера
        deleteCourier(courier);
    }

    // Тест 2. Нельзя создать двух одинаковых курьеров
    @Test
    @DisplayName("Яндекс Самокат. Нельзя создать двух одинаковых курьеров")
    @Description("При попытке создать двух одинаковых курьеров запрос возвращает ошибку 409")
    public void CheckCreateIdenticalCouriers(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13","1234","Courier");
        response = createCourier(courier);
        //запрос возвращает правильный код ответа
        checkResponseStatusCode(response, 201);
        //успешный запрос возвращает ok: true;
        checkResponseCreateCourier(response);
        //повторное создание курьера, запрос возвращает ошибку 409
        response = createCourier(courier);
        checkResponseStatusCode(response, 409);
        //удаляем курьера
        deleteCourier(courier);
    }

    // Тест 3. Нельзя создать курьера без логина или пароля
    @Test
    @DisplayName("Яндекс Самокат. Нельзя создать курьера без логина или пароля")
    @Description("Если одного из полей нет, запрос возвращает ошибку 400")
    public void CheckCreateCourierWithoutPassword(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13");
        response = createCourier(courier);
        //если одного из полей нет, запрос возвращает ошибку 400
        checkResponseStatusCode(response, 400);
    }

    // Тест 4. Курьер может авторизоваться
    @Test
    @DisplayName("Яндекс Самокат. Курьер может авторизоваться")
    @Description("Успешный запрос возвращает id")
    public void CheckLoginCourier(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13","1234","Courier");
        response = createCourier(courier);
        //запрос возвращает правильный код ответа
        checkResponseStatusCode(response, 201);
        //успешный запрос возвращает ok: true;
        checkResponseCreateCourier(response);
        //успешный запрос возвращает id
        checkResponseLoginCourier(loginCourier(courier));
        //удаляем курьера
        deleteCourier(courier);
    }

    // Тест 5. Нельзя авторизоваться без логина или пароля
    @Test
    @DisplayName("Яндекс Самокат. Нельзя авторизоваться без логина или пароля")
    @Description("Если какого-то поля нет, запрос возвращает ошибку 400")
    public void CheckLoginCourierWithoutLogin(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13","1234","Courier");
        response = createCourier(courier);
        //успешный запрос возвращает ok: true;
        checkResponseCreateCourier(response);

        //Если какого-то поля нет, запрос возвращает ошибку 400
        Courier fakeCourier = new Courier("", "1234");
        checkResponseStatusCode(loginCourier(fakeCourier),400);

        //удаляем курьера
        deleteCourier(courier);
    }

    // Тест 6. Нельзя авторизоваться под несуществующим пользователем
    @Test
    @DisplayName("Яндекс Самокат. Нельзя авторизоваться под несуществующим пользователем")
    @Description("Если неправильно указать логин или пароль, запрос возвращает ошибку 404")
    public void CheckLoginFakeCourier(){
        //создаем курьера
        Courier courier = new Courier("testCourier_ssa13","1234","Courier");
        response = createCourier(courier);
        //успешный запрос возвращает ok: true;
        checkResponseCreateCourier(response);

        //Если неправильно указать логин или пароль, запрос возвращает ошибку 404
        Courier fakeCourier = new Courier("testCourier_ssa13", "123456");
        checkResponseStatusCode(loginCourier(fakeCourier),404);

        //удаляем курьера
        deleteCourier(courier);
    }

    @Step("Создать курьера")
    public Response createCourier(Courier courier) {

        return
                given()
                        .header("Content-type", "application/json")
                        .and()
                        //.body(json)
                        .body(courier)
                        .when()
                        .post("/api/v1/courier");
    }

    @Step("Успешный запрос возвращает OK")
    public void checkResponseCreateCourier(Response response) {
        response.then().assertThat().body("ok", equalTo(true));
    }

    @Step("Запрос возвращает правильный код ответа")
    public void checkResponseStatusCode(Response response, int statusCode) {
        response.then().assertThat().statusCode(statusCode);
    }

    @Step("Курьер может авторизоваться")
    public Response loginCourier(Courier courier){
        return  given()
                        .header("Content-type", "application/json")
                        .and()
                .body(courier)
                        .when()
                        .post("/api/v1/courier/login");
    }
    @Step("Успешный запрос возвращает id")
    public void checkResponseLoginCourier(Response response) {
        response.then().assertThat().body("id", notNullValue());
    }

    @Step("Удалить курьера")
    public void deleteCourier(Courier courier) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("/api/v1/courier/" + loginCourier(courier).jsonPath().getString("id"))
                .then().assertThat().body("ok", equalTo(true));
    }
}
