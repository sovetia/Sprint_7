import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderTest {
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    private String firstName;
    private String lastName;
    private String address;
    private String metroStation;
    private String phone;
    private int rentTime;
    private String deliveryDate;
    private String comment;
    private List<String> color;
    private Response createOrderResponse;

    public OrderTest(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, List color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }

    @Parameterized.Parameters(name = "{7}")
    public static Object[][] getData() {
        return new Object[][]{
                {"Иван", "Иванов", "Улица 1", "Бульвар Рокоссовского", "+70000000000", 1, "10.12.2022", "можно указать один из цветов", Arrays.asList("BLACK")},
                {"Петр", "Петров", "Улица 2", "Черкизовская", "80000000000", 2, "10.12.2022", "можно указать оба цвета", Arrays.asList("BLACK", "GREY")},
                {"Соломон", "Сидоров", "Улица 3", "Преображенская площадь", "+70000000000", 3, "10.12.2022", "можно совсем не указывать цвет", null},
        };
    }

    @Test
    @DisplayName("Create order")
    @Description("Проверка Создания заказа")
    public void checkOrderCreation() {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
        createOrderResponse = given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then()
                .extract().response();

        createOrderResponse.then().assertThat().body("track", notNullValue())
                .and()
                .statusCode(201);
    }

    @After
    @Step("Проверка списка заказов")
    public void checkOrderList() {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .get("api/v1/orders/track?t="+createOrderResponse.jsonPath().getString("track"))
                .then()
                .extract().response();
        response.then().assertThat().body("order", notNullValue());
    }
    @Step("Отмена заказа")
    public void cancelOrder() {
        given()
                .header("Content-type", "application/json")
                .when()
                .put("/api/v1/orders/cancel?track=" + createOrderResponse.jsonPath().getString("track"))
                .then().assertThat().body("ok", equalTo(true));
    }
}
